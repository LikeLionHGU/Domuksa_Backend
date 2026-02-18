package org.example.emmm.service;

import lombok.RequiredArgsConstructor;
import org.example.emmm.domain.Room;
import org.example.emmm.domain.RoomDMMessage;
import org.example.emmm.domain.User;
import org.example.emmm.domain.UserRoom;
import org.example.emmm.dto.RoomDMMessageDto;
import org.example.emmm.repository.RoomDMMessageRepository;
import org.example.emmm.repository.RoomRepository;
import org.example.emmm.repository.UserRepository;
import org.example.emmm.repository.UserRoomRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomDMMessageService {

    private final RoomDMMessageRepository roomDMMessageRepository;
    private final UserRoomRepository userRoomRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public RoomDMMessageDto.CreateDmResDto createDm(RoomDMMessageDto.CreateDmReqDto req, Long roomId, Long userId){
        Room room = roomRepository.findByIdAndDeletedFalse(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 룸입니다."));

        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        UserRoom userRoom = userRoomRepository.findActiveUserRoom(user.getId(), roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        if ("host".equals(userRoom.getRole())){
            throw new IllegalStateException("호스트는 Dm을 보낼 수 없습니다");
        }

        RoomDMMessage roomDMMessage = RoomDMMessage.builder()
                .room(room)
                .userRoom(userRoom)
                .content(req.getContent())
                .createdAt(LocalDateTime.now())
                .build();

        RoomDMMessage saved = roomDMMessageRepository.save(roomDMMessage);

        return RoomDMMessageDto.CreateDmResDto.builder()
                .message(RoomDMMessageDto.Message.from(saved))
                .build();

    }

    public RoomDMMessageDto.DetailDmResDto getDmList (Long roomId, Long userId){
        //(호스트or참여자)역할 확인, 그동안 했던 대화 불러오기
        Room room = roomRepository.findByIdAndDeletedFalse(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 룸입니다."));

        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        UserRoom userRoom = userRoomRepository.findActiveUserRoom(user.getId(), room.getId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
        List<RoomDMMessage> messageList;
        if("host".equalsIgnoreCase(userRoom.getRole())){
            messageList = roomDMMessageRepository.findByRoomId(roomId);
        }
        else{
            messageList = roomDMMessageRepository.findByRoomIdAndUserRoomId(roomId,userRoom.getId());
        }
        List<RoomDMMessageDto.Message> messageDtos = messageList.stream()
                .map(RoomDMMessageDto.Message::from)
                .toList();


        return RoomDMMessageDto.DetailDmResDto.builder()
                .roomId(roomId)
                .messages(messageDtos)
                .build();
    }

    }



