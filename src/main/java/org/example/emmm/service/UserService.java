package org.example.emmm.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import lombok.RequiredArgsConstructor;
import org.example.emmm.domain.User;
import org.example.emmm.domain.UserRoom;
import org.example.emmm.dto.RoomDto;
import org.example.emmm.repository.RoomRepository;
import org.example.emmm.repository.UserRepository;
import org.example.emmm.repository.UserRoomRepository;
import org.example.emmm.security.GoogleIdTokenVerifierService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserRoomRepository userRoomRepository;
    private final GoogleIdTokenVerifierService googleIdTokenVerifierService;

    /**
     * id_token 검증 후,
     * - googleSub로 기존 유저 찾기
     * - 이메일로 기존 유저 찾고 googleSub 연결
     * - 없으면 신규 생성
     */
    public User loginOrCreateByGoogleIdToken(String idTokenString) {
        GoogleIdToken.Payload payload = googleIdTokenVerifierService.verifyAndGetPayload(idTokenString);

        String sub = payload.getSubject();
        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String picture = (String) payload.get("picture");

        // 1) googleSub로 찾기
        User u = userRepository.findByGoogleSub(sub).orElse(null);
        if (u != null) {
            u.setName(name != null ? name : u.getName());
            u.setProfileUrl(picture);
            return userRepository.save(u);
        }

        // 2) email로 기존 유저 있으면 googleSub 연결
        u = userRepository.findByEmail(email).orElse(null);
        if (u != null) {
            u.setGoogleSub(sub);
            u.setName(name != null ? name : u.getName());
            u.setProfileUrl(picture);
            return userRepository.save(u);
        }

        // 3) 신규 생성
        User newUser = new User();
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setEmail(email);
        newUser.setName(name != null ? name : "google-user");
        newUser.setGoogleSub(sub);
        newUser.setProfileUrl(picture);

        return userRepository.save(newUser);
    }

    public List<RoomDto.DetailRoomResDto> getRunningRooms(Long userId) {
        User u =  userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<UserRoom> urs = userRoomRepository.findAllByUserId(u.getId());

        List<RoomDto.DetailRoomResDto> result = new ArrayList<>();

        for(UserRoom ur : urs){
            if(ur.getRoom().getState().equals("running")){
                result.add(RoomDto.DetailRoomResDto.from(ur.getRoom(), ur));
            }
        }

        return result;
    }

    public List<RoomDto.DetailRoomResDto> getCompleteRooms(Long userId) {
        User u =  userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<UserRoom> urs = userRoomRepository.findAllByUserId(u.getId());

        List<RoomDto.DetailRoomResDto> result = new ArrayList<>();

        for(UserRoom ur : urs){
            if(ur.getRoom().getState().equals("complete")){
                result.add(RoomDto.DetailRoomResDto.from(ur.getRoom(), ur));
            }
        }

        return result;
    }

}
