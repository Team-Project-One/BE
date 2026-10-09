package project.backend.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import project.backend.chat.dto.SendChatMessageRequest;
import project.backend.chat.dto.TypingStatusRequest;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageService chatMessageService;

    @MessageMapping("/chat/message")
    public void handleMessage(SendChatMessageRequest messageRequest, Principal principal) {
        // StompAuthChannelInterceptor가 'User.id' (String)를 넣어줌
        String senderUserIdStr = principal.getName();
        Long senderUserId = Long.parseLong(senderUserIdStr);

        log.info("Message received from User.id {}: roomId={}, content={}",
                senderUserId, messageRequest.getRoomId(), messageRequest.getContent());

        chatMessageService.sendMessage(senderUserId, messageRequest);
    }

    @MessageMapping("/chat/typing")
    public void handleTyping(TypingStatusRequest typingRequest, Principal principal) {
        String senderUserIdStr = principal.getName();
        Long senderUserId = Long.parseLong(senderUserIdStr);

        log.debug("Typing status from User.id {}: roomId={}, typing={}",
                senderUserId, typingRequest.getRoomId(), typingRequest.isTyping());

        chatMessageService.sendTypingStatus(senderUserId, typingRequest);
    }
}