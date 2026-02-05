package org.example.emmm.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.emmm.domain.*;
import org.example.emmm.dto.AgendaDto;
import org.example.emmm.dto.RoomDto;
import org.example.emmm.repository.AgendaRepository;
import org.example.emmm.repository.RoomRepository;
import org.example.emmm.repository.UserRepository;
import org.example.emmm.repository.UserRoomRepository;
import org.example.emmm.util.RoomCodeGenerator;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final UserRoomRepository userRoomRepository;
    private final AgendaRepository agendaRepository;

    @Transactional
    public RoomDto.CreateResDto create(RoomDto.CreateReqDto req, Long hostUserId) {

        User host = userRepository.findByIdAndDeletedFalse(hostUserId)
                .orElseThrow(() -> new IllegalArgumentException("host user not found"));

        for (int attempt = 0; attempt < 10; attempt++) {
            String code = RoomCodeGenerator.generate(10);

            Room room = Room.builder()
                    .createdAt(LocalDateTime.now())
                    .roomName(req.getRoomName())
                    .password(req.getPassword())
                    .currentAgendaSequence(1)
                    .code(code)
                    .state("running")
                    .build();

            try {
                Room savedRoom = roomRepository.save(room);

                UserRoom hostMapping = UserRoom.builder()
                        .room(savedRoom)
                        .user(host)
                        .role("host")
                        .state("active")
                        .build();

                userRoomRepository.save(hostMapping);

                return new RoomDto.CreateResDto(savedRoom.getId(), savedRoom.getCode(), savedRoom.getRoomName());

            } catch (DataIntegrityViolationException e) {
                // room.code UNIQUE 충돌 -> 재시도
            }
        }

        throw new IllegalStateException("Failed to generate unique room code");
    }

    @Transactional
    public RoomDto.ParticipateCreateResDto createParticipate(RoomDto.ParticipateCreateReqDto req, Long userId) {
        User u = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        Room r = roomRepository.findByCodeAndDeletedFalse(req.getCode())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 코드입니다."));

        UserRoom ur = UserRoom.builder()
                .room(r)
                .user(u)
                .role("member")
                .state("active")
                .build();

        userRoomRepository.save(ur);

        return new RoomDto.ParticipateCreateResDto(r.getId(), ur.getId(), ur.getRole());
    }

    public RoomDto.DetailResDto getRoom(Long roomId, Long userId) {
        Room room = roomRepository.findByIdAndDeletedFalse(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방입니다."));

        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        UserRoom userRoom = userRoomRepository.findByUserAndRoom(user, room)
                .orElseThrow(() -> new IllegalArgumentException("이 방에 참여하지 않은 유저입니다."));

        return RoomDto.DetailResDto.from(room, userRoom);
    }

    @Transactional
    public void delete(Long roomId, Long userId) {
        Room room = roomRepository.findByIdAndDeletedFalse(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방입니다."));

        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        UserRoom userRoom = userRoomRepository.findByUserAndRoom(user, room)
                .orElseThrow(() -> new IllegalArgumentException("이 방에 참여하지 않은 유저입니다."));

        if ("host".equals(userRoom.getRole())) {
            room.setDeleted(true);

            List<UserRoom> allMembers = userRoomRepository.findAllByRoom(room);
            for (UserRoom ur : allMembers) {
                ur.setDeleted(true);
            }

        } else {
            userRoom.setDeleted(true);
        }
    }

    @Transactional
    public List<AgendaDto.DetailAgendaResDto> getAgendas(Long roomId) {
        roomRepository.findByIdAndDeletedFalse(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방입니다."));

        List<Agenda> agendas = agendaRepository.findByRoomId(roomId);

        List<AgendaDto.DetailAgendaResDto> res = new ArrayList<>();
        for(Agenda a : agendas) {
            AgendaConfig ac = a.getConfig();
            res.add(AgendaDto.DetailAgendaResDto.from(a, ac));
        }

        return res;
    }
}
