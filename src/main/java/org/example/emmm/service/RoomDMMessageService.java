package org.example.emmm.service;

import lombok.RequiredArgsConstructor;
import org.example.emmm.domain.RoomDMMessage;
import org.example.emmm.domain.UserRoom;
import org.example.emmm.dto.RoomDMMessageDto;
import org.example.emmm.repository.RoomDMMessageRepository;
import org.example.emmm.repository.UserRoomRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomDMMessageService {

    private final RoomDMMessageRepository roomDMMessageRepository;
    private final UserRoomRepository userRoomRepository;

    public RoomDMMessageDto.CreateDmResDto createDm(RoomDMMessageDto.CreateDmReqDto req){
        UserRoom userRoom = userRoomRepository.findById(req.getUserRoomId()).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if ("host".equals(userRoom.getRole())){
            throw new IllegalStateException("호스트는 Dm을 보낼 수 없습니다");
        }

        RoomDMMessage roomDMMessage = RoomDMMessage.builder()
                .room(userRoom.getRoom())
                .userRoom(userRoom)
                .content(req.getContent())
                .createdAt(LocalDateTime.now())
                .build();

        RoomDMMessage saved = roomDMMessageRepository.save(roomDMMessage);

        return RoomDMMessageDto.CreateDmResDto.builder()
                .message(RoomDMMessageDto.Message.from(saved))
                .build();

    }

    public RoomDMMessageDto.DetailDmResDto getDmList (Long roomId, Long userRoomId){
        //(호스트or참여자)역할 확인, 그동안 했던 대화 불러오기
        UserRoom userRoom = userRoomRepository.findById(userRoomId).orElseThrow(()->new IllegalArgumentException("존재하지 않는 사용자입니다."));
        List<RoomDMMessage> messageList;
        if("host".equalsIgnoreCase(userRoom.getRole())){
            messageList = roomDMMessageRepository.findByRoomId(roomId);
        }
        else{
            messageList = roomDMMessageRepository.findByRoomIdAndUserRoomId(roomId,userRoomId);
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



