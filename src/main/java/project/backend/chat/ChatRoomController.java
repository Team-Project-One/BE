package project.backend.chat;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import project.backend.chat.dto.ChatMessageDTO;
import project.backend.chat.dto.ChatRoomDTO;
import project.backend.chat.dto.CreateChatRoomRequest;
import project.backend.kakaoLogin.KakaoUser;
import project.backend.pythonapi.dto.SajuResponse;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "채팅 API (REST)", description = "채팅방 생성, 목록 조회, 메시지 내역 조회")
@SecurityRequirement(name = "bearerAuth")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;

    @Operation(summary = "1:1 채팅방 생성 또는 조회")
    @PostMapping("/room")
    public ResponseEntity<ChatRoomDTO> createOrGetRoom(
            @AuthenticationPrincipal KakaoUser kakaoUser,
            @RequestBody CreateChatRoomRequest request) {

        if (kakaoUser.getUser() == null) {
            return ResponseEntity.status(403).build(); // 회원가입 미완료
        }
        Long currentUserId = kakaoUser.getUser().getId();

        ChatRoomDTO room = chatRoomService.createOrGetRoom(currentUserId, request.getMatchedUserId());
        return ResponseEntity.ok(room);
    }

    @Operation(summary = "내 채팅방 목록 조회")
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomDTO>> getMyChatRooms(@AuthenticationPrincipal KakaoUser kakaoUser) {

        Long currentUserId = kakaoUser.getUser().getId();
        List<ChatRoomDTO> rooms = chatRoomService.getUserChatRooms(currentUserId);
        return ResponseEntity.ok(rooms);
    }

    @Operation(summary = "특정 채팅방 메시지 내역 조회")
    @GetMapping("/room/{roomId}/messages")
    public ResponseEntity<List<ChatMessageDTO>> getChatMessages(
            @PathVariable Long roomId,
            @AuthenticationPrincipal KakaoUser kakaoUser) {

        // TODO: (선택) kakaoUser.getUser().getId()가 이 roomId에 접근 권한이 있는지 확인

        List<ChatMessageDTO> messages = chatMessageService.getChatMessages(roomId);
        return ResponseEntity.ok(messages);
    }

    @Operation(summary = "채팅방 나가기 (채팅방 및 대화 내역 삭제)",
            description = "채팅방을 나갑니다. 1:1 채팅이므로 방 자체가 삭제되며, 상대방에게도 목록에서 사라집니다.")
    @DeleteMapping("/room/{roomId}")
    public ResponseEntity<Void> leaveChatRoom(
            @AuthenticationPrincipal KakaoUser kakaoUser,
            @PathVariable Long roomId) {

        if (kakaoUser.getUser() == null) {
            return ResponseEntity.status(403).build(); // Forbidden
        }
        Long currentUserId = kakaoUser.getUser().getId();

        try {
            chatRoomService.deleteChatRoom(currentUserId, roomId);
            return ResponseEntity.ok().build(); // 성공 (200 OK)
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build(); // 방이 없음 (404 Not Found)
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).build(); // 권한 없음 (403 Forbidden)
        }
    }

    @Operation(summary = "채팅방 궁합 점수 조회", description = "채팅방 ID를 통해 저장된 두 사람의 사주 궁합 결과를 조회")
    @GetMapping("/room/{roomId}/saju")
    public ResponseEntity<SajuResponse> getSajuInfo(
            @PathVariable Long roomId,
            @AuthenticationPrincipal KakaoUser kakaoUser) {

        if (kakaoUser.getUser() == null) {
            return ResponseEntity.status(403).build();
        }
        Long currentUserId = kakaoUser.getUser().getId();

        SajuResponse response = chatRoomService.getSajuInfoInRoom(roomId, currentUserId);

        return ResponseEntity.ok(response);
    }
}