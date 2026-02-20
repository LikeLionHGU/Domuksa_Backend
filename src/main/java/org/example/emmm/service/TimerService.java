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

import java.time.LocalDateTime;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Transactional
public class TimerService {
    private final TimerRepository timerRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final UserRoomRepository userRoomRepository;

    @Transactional(readOnly = true)
    public TimerDto.DetailTimerResDto getTimer(Long roomId) {
        Room r = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));

        Timer t = timerRepository.findByRoomAndDeletedFalse(r)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Timer not found"));

        Long resTime = 0L;
        if ("play".equals(t.getStatus())) {
            LocalDateTime finishTime = t.getStartTime().plusSeconds(t.getTime());
            Duration duration = Duration.between(LocalDateTime.now(), finishTime);

            resTime = (long) Math.ceil(duration.toMillis() / 1000.0);
            if (resTime < 0) resTime = 0L;
        } else if ("stop".equals(t.getStatus())) {
            resTime = t.getTime();
        }

        return TimerDto.DetailTimerResDto.builder()
                .timerId(t.getId())
                .roomId(t.getRoom().getId())
                .time(resTime)
                .state(t.getStatus())
                .build();
    }

    @Transactional
    public TimerDto.UpdateTimeResDto updateTime(Long roomId, TimerDto.UpdateTimeReqDto req, Long userId) {
        Room r = roomRepository.findByIdAndDeletedFalse(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));

        Timer t = timerRepository.findByIdAndDeletedFalse(r.getTimer().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Timer not found"));

        userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        UserRoom ur = userRoomRepository.findActiveUserRoom(userId, roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "UserRoom not found"));

        if (!"host".equals(ur.getRole())) {
            throw new IllegalArgumentException("호스트가 아닙니다.");
        }

        if ("play".equals(t.getStatus())) {
            t.setStartTime(LocalDateTime.now());
        }

        t.setTime(req.getTime());
        return TimerDto.UpdateTimeResDto.from(t);
    }

    @Transactional
    public TimerDto.UpdateStateResDto updateState(Long roomId, TimerDto.UpdateStateReqDto req, Long userId) {
        Room r = roomRepository.findByIdAndDeletedFalse(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));

        Timer t = timerRepository.findByIdAndDeletedFalse(r.getTimer().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Timer not found"));

        userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        UserRoom ur = userRoomRepository.findActiveUserRoom(userId, roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "UserRoom not found"));

        if (!"host".equals(ur.getRole())) {
            throw new IllegalArgumentException("호스트가 아닙니다.");
        }

        if ("stop".equals(t.getStatus()) && "play".equals(req.getState())) {
            t.setStatus(req.getState());
            t.setStartTime(LocalDateTime.now());
        } else if ("play".equals(t.getStatus()) && "stop".equals(req.getState())) {
            t.setStatus(req.getState());
            LocalDateTime finishTime = t.getStartTime().plusSeconds(t.getTime());
            Duration duration = Duration.between(LocalDateTime.now(), finishTime);

            long resTime = (long) Math.ceil(duration.toMillis() / 1000.0);
            t.setTime(Math.max(0, resTime));
            t.setStartTime(null);
        } else {
            throw new IllegalArgumentException("이미 해당 상태이거나 잘못된 요청입니다.");
        }

        return TimerDto.UpdateStateResDto.from(t);
    }
}