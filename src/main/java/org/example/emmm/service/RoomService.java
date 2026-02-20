package org.example.emmm.service;

import lombok.RequiredArgsConstructor;
import org.example.emmm.domain.*;
import org.example.emmm.dto.AgendaDto;
import org.example.emmm.dto.RoomDto;
import org.example.emmm.repository.*;
import org.example.emmm.util.RoomCodeGenerator;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static reactor.netty.http.HttpConnectionLiveness.log;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final UserRoomRepository userRoomRepository;
    private final AgendaRepository agendaRepository;

    private final PresenceService presenceService;
    private final TimerRepository timerRepository;


    private Timer createDefaultTimer(Room savedRoom) {
        return Timer.builder()
                .createdAt(LocalDateTime.now())
                .time(0L)          // 초기 시간 0 (단위가 초인지 확인)
                .status("stop")    // 초기 상태 stop
                .room(savedRoom)   // FK
                .build();
    }

    @Transactional
    public RoomDto.CreateRoomResDto create(RoomDto.CreateRoomReqDto req, Long hostUserId) {

        User host = userRepository.findByIdAndDeletedFalse(hostUserId)
                .orElseThrow(() -> new IllegalArgumentException("host user not found"));

        boolean isPassword = StringUtils.hasText(req.getPassword());

        for (int attempt = 0; attempt < 10; attempt++) {
            String code = RoomCodeGenerator.generate(10);

            Room room = Room.builder()
                    .createdAt(LocalDateTime.now())
                    .roomName(req.getRoomName())
                    .password(isPassword ? req.getPassword() : null)
                    .currentAgendaSequence(1)
                    .isPassword(isPassword)
                    .code(code)
                    .state("running")
                    .build();

            try {
                // 1) Room 저장
                Room savedRoom = roomRepository.save(room);

                // 2) Host 매핑 저장
                UserRoom hostMapping = UserRoom.builder()
                        .room(savedRoom)
                        .user(host)
                        .role("host")
                        .state("active")
                        .build();
                userRoomRepository.save(hostMapping);

                // 3) ✅ Timer 기본값으로 생성 후 저장
                Timer t = createDefaultTimer(savedRoom);
                timerRepository.save(t);

                return new RoomDto.CreateRoomResDto(savedRoom.getId(), savedRoom.getCode(), savedRoom.getRoomName());

            } catch (DataIntegrityViolationException e) {
                // ✅ 핵심: 여기서 "진짜 원인"을 반드시 확인해야 함
                Throwable root = NestedExceptionUtils.getMostSpecificCause(e);
                String rootMsg = (root != null ? root.getMessage() : e.getMessage());
                log.error("Room create failed. attempt={}, code={}, root={}", attempt + 1, code, rootMsg, e);

                // ✅ "room.code 유니크 충돌"일 때만 재시도
                // (아래 문자열은 DB/인덱스명에 맞게 조정 필요)
                if (rootMsg != null && (rootMsg.contains("room") && rootMsg.contains("code") && rootMsg.contains("Duplicate"))) {
                    continue;
                }

                // 그 외(타이머 제약/NOT NULL/길이 초과/FK 등)는 재시도해도 계속 실패하므로 바로 throw
                throw e;
            }
        }

        throw new IllegalStateException("Failed to generate unique room code");
    }



    //password 있는 방 참여
    @Transactional
    public RoomDto.ParticipatePasswordCreateResDto createParticipatePassword(RoomDto.ParticipatePasswordCreateReqDto req, Long roomId, Long userId) {
        User u = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        Room r = roomRepository.findByIdAndDeletedFalse(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방입니다."));

        Optional<UserRoom> existingUserRoom = userRoomRepository.findActiveUserRoom(u.getId(), r.getId());

        if (existingUserRoom.isPresent()) {
            UserRoom ur = existingUserRoom.get();
            return new RoomDto.ParticipatePasswordCreateResDto(r.getId(), ur.getId(), ur.getRole());
        }

        if (r.getPassword() != null && r.getPassword().equals(req.getPassword())) {
            UserRoom ur = UserRoom.builder()
                    .room(r)
                    .user(u)
                    .role("member")
                    .state("active")
                    .deleted(false)
                    .build();

            userRoomRepository.save(ur);
            return new RoomDto.ParticipatePasswordCreateResDto(r.getId(), ur.getId(), ur.getRole());
        } else {
            throw new IllegalStateException("비밀번호가 일치하지 않습니다.");
        }
    }

    // password가 없는 방 참여
    @Transactional
    public RoomDto.ParticipateCreateResDto createParticipate(Long roomId, Long userId) {
        User u = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        Room r = roomRepository.findByIdAndDeletedFalse(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방입니다."));

        if (Boolean.TRUE.equals(r.getIsPassword())) {
            throw new IllegalStateException("비밀번호가 필요한 방입니다. 비밀번호 입력 API를 사용하세요.");
        }

        boolean isAlreadyJoined = userRoomRepository.existsByRoomAndUser(r, u);
        if (isAlreadyJoined) {
            throw new IllegalStateException("이미 참여 중인 방입니다.");
        }

        UserRoom ur = UserRoom.builder()
                .room(r)
                .user(u)
                .role("member")
                .state("active")
                .build();

        userRoomRepository.save(ur);
        return new RoomDto.ParticipateCreateResDto(r.getId(), ur.getId(), ur.getRole());
    }

    //code를 주면 이를 확인해서 roomId와 password가 있는지 판단해서 줌
    public RoomDto.CodeResDto getRoomIdAndIsPassword (String code) {
        Room r = roomRepository.findByCodeAndDeletedFalse(code)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방입니다."));
        return RoomDto.CodeResDto.from(r);
    }

    public RoomDto.DetailRoomResDto getRoom(Long roomId, Long userId) {
        Room room = roomRepository.findByIdAndDeletedFalse(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방입니다."));

        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        UserRoom userRoom = userRoomRepository.findActiveUserRoom(user.getId(), room.getId())
                .orElseThrow(() -> new IllegalArgumentException("이 방에 참여하지 않은 유저입니다."));

        return RoomDto.DetailRoomResDto.from(room, userRoom);
    }

    @Transactional
    public RoomDto.UpdateRoomNameResDto updateRoomNameAndPassword(RoomDto.UpdateRoomNameReqDto req, Long roomId, Long userId) {
        User u = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        Room r = roomRepository.findByIdAndDeletedFalse(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방입니다."));

        UserRoom ur = userRoomRepository.findActiveUserRoom(u.getId(), r.getId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저룸입니다."));

        if (!"host".equals(ur.getRole())) {
            throw new IllegalStateException("당신은 호스트가 아닙니다.");
        }

        if(StringUtils.hasText(req.getName())){
            r.setRoomName(req.getName());
        }

        if(StringUtils.hasText(req.getPassword())){
            r.setPassword(req.getPassword());
        }

        return RoomDto.UpdateRoomNameResDto.from(r);
    }

    @Transactional
    public void delete(Long roomId, Long userId) {
        Room room = roomRepository.findByIdAndDeletedFalse(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방입니다."));

        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        UserRoom userRoom = userRoomRepository.findActiveUserRoom(user.getId(), room.getId())
                .orElseThrow(() -> new IllegalArgumentException("이 방에 참여하지 않은 유저입니다."));

        if ("host".equals(userRoom.getRole())) {
            room.setDeleted(true);

            List<UserRoom> allMembers = userRoomRepository.findAllActiveMembersByRoom(room);
            for (UserRoom ur : allMembers) {
                ur.setDeleted(true);
            }

        } else {
            userRoom.setDeleted(true);
        }
    }

    @Transactional
    public List<AgendaDto.DetailListAgendaResDto> getAgendas(Long roomId) {
        roomRepository.findByIdAndDeletedFalse(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방입니다."));

        List<Agenda> agendas = agendaRepository.findAllActiveByRoomId(roomId);

        List<AgendaDto.DetailListAgendaResDto> res = new ArrayList<>();
        for(Agenda a : agendas) {
            AgendaConfig ac = a.getConfig();
            res.add(AgendaDto.DetailListAgendaResDto.from(a, ac));
        }

        return res;
    }

    @Transactional
    public String updateRoomState(Long roomId, RoomDto.UpdateStateReqDto req,Long userId) {

        Room r = roomRepository.findByIdAndDeletedFalse(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방입니다."));

        User u = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        UserRoom ur = userRoomRepository.findActiveUserRoom(u.getId(), r.getId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        if (!"host".equals(ur.getRole())) {
            throw new IllegalStateException("방 상태 변경 권한이 없습니다.");
        }

        if ("running".equals(r.getState()) && req.getState().equals("complete")) {
            r.setState(req.getState());
        } else if ("complete".equals(r.getState()) && req.getState().equals("running")) {
            r.setState(req.getState());
        } else {
            throw new IllegalStateException("request가 running이거나 complete가 아닙니다.");
        }

        roomRepository.save(r);

        return r.getState();
    }

    @Transactional(readOnly = true)
    public List<RoomDto.RoomMemberResDto> getRoomMembers(Long roomId) {
        List<UserRoom> members = userRoomRepository.findAllActiveMembersByRoomId(roomId);

        return members.stream()
                .map(ur -> RoomDto.RoomMemberResDto.builder()
                        .userId(ur.getUser().getId())
                        .name(ur.getUser().getName())
                        .role(ur.getRole())
                        .profileUrl(ur.getUser().getProfileUrl())
                        .isOnline(presenceService.isUserOnline(roomId, ur.getUser().getId()))
                        .build())
                .toList();
    }

}
