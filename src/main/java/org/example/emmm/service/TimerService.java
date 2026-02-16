package org.example.emmm.service;

import lombok.RequiredArgsConstructor;
import org.example.emmm.domain.*;
import org.example.emmm.dto.TimerDto;
import org.example.emmm.repository.RoomRepository;
import org.example.emmm.repository.TimerRepository;
import org.example.emmm.repository.UserRepository;
import org.example.emmm.repository.UserRoomRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
public class TimerService {

    private final TimerRepository timerRepository;
    private final RoomRepository roomRepository;
    private final UserRoomRepository userRoomRepository;
    private final UserRepository userRepository;


    public TimerDto.TimerResDto get(Long roomId, Long userId) {

        long now = System.currentTimeMillis();

        Timer timer = timerRepository.findByRoomIdAndDeletedFalse(roomId)
                .orElseGet(() -> createAndSaveDefaultTimer(roomId));

        // RUNNING 상태에서 시간이 끝났으면 상태 정리(REST만 쓸 때 특히 중요)
        if (timer.getStatus() == TimerStatus.RUNNING && timer.getEndAtEpochMs() != null) {
            long remaining = Timer.calcRemainingSeconds(timer.getEndAtEpochMs(), now);
            if (remaining == 0L) {
                timer.setRemainingSeconds(0L);
                timer.setEndAtEpochMs(null);
                timer.setStatus(TimerStatus.STOPPED);
                timerRepository.save(timer);
            }
        }

        return TimerDto.TimerResDto.from(timer, now);
    }

    // POST /timer/{roomId}/set (host only)
    public TimerDto.TimerResDto set(Long roomId, Long requesterUserId, TimerDto.TimerReqDto req) {

        requireHost(roomId, requesterUserId);

        if (req.getTotalSeconds() < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "TOTAL_SECONDS_MIN_1");
        }

        Room room = getRoomOrThrow(roomId);

        Timer timer = timerRepository.findByRoomIdAndDeletedFalse(roomId)
                .orElseGet(() -> createTimer(room));

        if (timer.getStatus() == TimerStatus.RUNNING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "TIMER_RUNNING_CANNOT_SET");
        }

        timer.setTotalSeconds(req.getTotalSeconds());
        timerRepository.save(timer);

        long now = System.currentTimeMillis();
        return TimerDto.TimerResDto.from(timer, now);
    }

    //POST /timer/{roomId}/start (host only) (STOPPED/PAUSED -> RUNNING)
    public TimerDto.TimerResDto start(Long roomId, Long requesterUserId) {
        requireHost(roomId, requesterUserId);

        Timer timer = timerRepository.findByRoomIdAndDeletedFalse(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "TIMER_NOT_SET"));

        if (timer.getTotalSeconds() == null || timer.getTotalSeconds() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "TIMER_NOT_SET");
        }

        if (timer.getStatus() == TimerStatus.RUNNING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "TIMER_ALREADY_RUNNING");
        }

        if (timer.getRemainingSeconds() == null || timer.getRemainingSeconds() <= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "NO_REMAINING_TIME");
        }

        long now = System.currentTimeMillis();
        timer.start(now);
        timerRepository.save(timer);

        return TimerDto.TimerResDto.from(timer, now);
    }

    //POST /timer/{roomId}/pause (host only)
    //- RUNNING -> PAUSED
    public TimerDto.TimerResDto pause(Long roomId, Long requesterUserId) {
        requireHost(roomId, requesterUserId);

        Timer timer = timerRepository.findByRoomIdAndDeletedFalse(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "TIMER_NOT_FOUND"));

        if (timer.getStatus() != TimerStatus.RUNNING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "TIMER_NOT_RUNNING");
        }

        long now = System.currentTimeMillis();
        timer.pause(now);
        timerRepository.save(timer);

        return TimerDto.TimerResDto.from(timer, now);
    }

    //POST /timer/{roomId}/reset (host only)
    //어떤 상태든 STOPPED로 돌림
    public TimerDto.TimerResDto reset(Long roomId, Long requesterUserId) {
        requireHost(roomId, requesterUserId);

        Timer timer = timerRepository.findByRoomIdAndDeletedFalse(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "TIMER_NOT_FOUND"));

        timer.reset();
        timerRepository.save(timer);

        long now = System.currentTimeMillis();
        return TimerDto.TimerResDto.from(timer, now);
    }

    private void requireHost(Long roomId, Long userId) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

        Room r = roomRepository.findByIdAndDeletedFalse(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ROOM_NOT_FOUND"));

        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
        }

        UserRoom ur = userRoomRepository.findActiveUserRoom(u, r)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "NOT_MEMBER"));


         if (!"host".equalsIgnoreCase(ur.getRole())) {
             throw new ResponseStatusException(HttpStatus.FORBIDDEN, "HOST_ONLY");
         }
    }

    // (선택) 멤버 체크까지 하고 싶으면
    @SuppressWarnings("unused")
    private void requireMember(Long roomId, Long userId) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

        Room r = roomRepository.findByIdAndDeletedFalse(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ROOM_NOT_FOUND"));

        userRoomRepository.findActiveUserRoom(u, r)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "NOT_MEMBER"));
    }

    // ---------------------------
    // 생성/조회 유틸
    // ---------------------------

    private Room getRoomOrThrow(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ROOM_NOT_FOUND"));
    }

    private Timer createTimer(Room room) {
        Timer timer = new Timer();
        timer.setRoom(room);
        timer.setDeleted(false);
        timer.setStatus(TimerStatus.STOPPED);
        timer.setTotalSeconds(0L);
        timer.setRemainingSeconds(0L);
        timer.setEndAtEpochMs(null);
        return timer;
    }

    private Timer createAndSaveDefaultTimer(Long roomId) {
        Room room = getRoomOrThrow(roomId);
        Timer timer = createTimer(room);
        return timerRepository.save(timer);
    }
}

