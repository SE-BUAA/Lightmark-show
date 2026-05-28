package top.ortus.timemark.backend.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import top.ortus.timemark.backend.common.ApiResponse;
import top.ortus.timemark.backend.dto.AiDTO;
import top.ortus.timemark.backend.dto.chat.ChatContextDTO;
import top.ortus.timemark.backend.dto.chat.ChatMessageDTO;
import top.ortus.timemark.backend.dto.chat.ChatRequest;
import top.ortus.timemark.backend.service.ConversationService;

import java.util.List;

public class ConversationControllerTest {

    @Test
    public void testContextAndResetAndSystemPromptPassThrough() {
        ConversationService service = Mockito.mock(ConversationService.class);
        ConversationController controller = new ConversationController(service);

        String sessionId = "test-session";

        AiDTO aiDTO = new AiDTO();
        aiDTO.setContent("OK");
        aiDTO.setModel("test");

        Mockito.when(service.chat(Mockito.eq(sessionId), Mockito.eq("你好"), Mockito.eq("你只回答“OK”"))).thenReturn(aiDTO);
        Mockito.when(service.getContext(Mockito.eq(sessionId))).thenReturn(new ChatContextDTO("你只回答“OK”", List.of(
                new ChatMessageDTO("user", "你好"),
                new ChatMessageDTO("assistant", "OK")
        )));
        Mockito.when(service.resetContext(Mockito.eq(sessionId))).thenReturn(true);

        ChatRequest req = new ChatRequest();
        req.setSessionId(sessionId);
        req.setMessage("你好");
        req.setSystemPrompt("你只回答“OK”");

        ApiResponse<AiDTO> chatResp = controller.chat(req);
        Assertions.assertEquals(0, chatResp.getCode());
        Assertions.assertEquals("OK", chatResp.getData().getContent());

        ApiResponse<ChatContextDTO> ctx = controller.getContext(sessionId);
        Assertions.assertEquals(0, ctx.getCode());
        Assertions.assertEquals("你只回答“OK”", ctx.getData().getSystemPrompt());
        Assertions.assertEquals(2, ctx.getData().getMessages().size());

        ApiResponse<Boolean> reset = controller.resetContext(sessionId);
        Assertions.assertEquals(0, reset.getCode());
        Assertions.assertTrue(Boolean.TRUE.equals(reset.getData()));
    }
}

