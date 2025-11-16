package project.backend.chat;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.backend.chat.dto.ChatRoomDTO;
import project.backend.chat.entity.ChatRoom;
import project.backend.chat.repository.ChatRoomRepository;
import project.backend.user.UserRepository;
import project.backend.user.entity.User;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    /**
     * 1:1 채팅방 생성 또는 조회
     */
    @Transactional
    public ChatRoomDTO createOrGetRoom(Long currentUserId, Long matchedUserId) {
        User user1 = userRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("Current user not found: " + currentUserId));
        User user2 = userRepository.findById(matchedUserId)
                .orElseThrow(() -> new EntityNotFoundException("Matched user not found: " + matchedUserId));

        ChatRoom room = chatRoomRepository.findByParticipants(user1, user2)
                .orElseGet(() -> {
                    ChatRoom newRoom = new ChatRoom(user1, user2);
                    return chatRoomRepository.save(newRoom);
                });

        return ChatRoomDTO.fromEntity(room, user1);
    }

    /**
     * 내 채팅방 목록 조회
     */
    public List<ChatRoomDTO> getUserChatRooms(Long currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("Current user not found: " + currentUserId));

        List<ChatRoom> rooms = chatRoomRepository.findAllByUser(user);

        return rooms.stream()
                .map(room -> ChatRoomDTO.fromEntity(room, user))
                .collect(Collectors.toList());
    }

    /**
     * 채팅방 나가기 (채팅방 및 메시지 삭제)
     */
    @Transactional
    public void deleteChatRoom(Long currentUserId, Long roomId) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("Current user not found: " + currentUserId));

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("ChatRoom not found: " + roomId));

        // 요청한 유저가 해당 채팅방의 참여자인지 확인 (인가)
        if (!room.getParticipants().contains(currentUser)) {
            throw new AccessDeniedException("User is not a participant of this chat room.");
        }

        chatRoomRepository.delete(room);
    }
}