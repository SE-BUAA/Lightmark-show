# M2 User Center + Floating AI Assistant Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 完成 M2“用户中心 + 悬浮 AI 客服”前后端闭环：用户资料/出行人/积分等级/订单/改密可用，AI 走后端 DeepSeek 代理并支持会话历史与重置。

**Architecture:** 后端以 `UserController` + `ConversationController` 为入口，补齐会话管理与密码修改真实逻辑；前端在 `App.vue` 全局挂载悬浮客服组件，并增强个人中心页面对接用户中心 API。

**Tech Stack:** Spring Boot 3 + Spring AI DeepSeek + JUnit 5；Vue 3 + Vue Router + Pinia + Element Plus + Axios。

---

## Files To Touch

### Backend

- Modify: `c:/Users/tangs/OneDrive/Desktop/Lightmark-main/backend/src/main/java/top/ortus/timemark/backend/controller/ConversationController.java`
- Modify: `c:/Users/tangs/OneDrive/Desktop/Lightmark-main/backend/src/main/java/top/ortus/timemark/backend/service/ConversationService.java`
- Create: `c:/Users/tangs/OneDrive/Desktop/Lightmark-main/backend/src/main/java/top/ortus/timemark/backend/dto/chat/ChatMessageDTO.java`
- Create: `c:/Users/tangs/OneDrive/Desktop/Lightmark-main/backend/src/main/java/top/ortus/timemark/backend/dto/chat/ChatContextDTO.java`
- Modify: `c:/Users/tangs/OneDrive/Desktop/Lightmark-main/backend/src/main/java/top/ortus/timemark/backend/service/UserService.java`
- Modify: `c:/Users/tangs/OneDrive/Desktop/Lightmark-main/backend/src/main/java/top/ortus/timemark/backend/service/UserServiceImpl.java`
- Modify: `c:/Users/tangs/OneDrive/Desktop/Lightmark-main/backend/src/main/java/top/ortus/timemark/backend/controller/UserController.java`
- Create: `c:/Users/tangs/OneDrive/Desktop/Lightmark-main/backend/src/test/java/top/ortus/timemark/backend/controller/ConversationControllerTest.java`
- Create: `c:/Users/tangs/OneDrive/Desktop/Lightmark-main/backend/src/test/java/top/ortus/timemark/backend/controller/UserPasswordControllerTest.java`

### Frontend

- Create: `c:/Users/tangs/OneDrive/Desktop/Lightmark-main/frontend/src/api/chat.ts`
- Create: `c:/Users/tangs/OneDrive/Desktop/Lightmark-main/frontend/src/components/ai/FloatingAssistant.vue`
- Modify: `c:/Users/tangs/OneDrive/Desktop/Lightmark-main/frontend/src/App.vue`
- Modify: `c:/Users/tangs/OneDrive/Desktop/Lightmark-main/frontend/src/views/module/UserCenterView.vue`

---

## Task 1: Backend Chat Context + systemPrompt

**Files:**
- Create: `backend/src/main/java/top/ortus/timemark/backend/dto/chat/ChatMessageDTO.java`
- Create: `backend/src/main/java/top/ortus/timemark/backend/dto/chat/ChatContextDTO.java`
- Modify: `backend/src/main/java/top/ortus/timemark/backend/service/ConversationService.java`
- Modify: `backend/src/main/java/top/ortus/timemark/backend/controller/ConversationController.java`

- [ ] **Step 1: Write failing tests for context/reset + systemPrompt passthrough**

Create `backend/src/test/java/top/ortus/timemark/backend/controller/ConversationControllerTest.java`:

```java
package top.ortus.timemark.backend.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.ortus.timemark.backend.BackendApplication;
import top.ortus.timemark.backend.common.ApiResponse;
import top.ortus.timemark.backend.dto.AiDTO;
import top.ortus.timemark.backend.dto.chat.ChatContextDTO;
import top.ortus.timemark.backend.dto.chat.ChatRequest;

import java.util.List;

@SpringBootTest(classes = BackendApplication.class)
public class ConversationControllerTest {

    @Autowired
    private ConversationController conversationController;

    @Test
    public void testContextAndReset() {
        String sessionId = "test-session";

        ChatRequest req = new ChatRequest();
        req.setSessionId(sessionId);
        req.setMessage("你好");
        req.setSystemPrompt("你只回答“OK”");

        ApiResponse<AiDTO> chatResp = conversationController.chat(req);
        Assertions.assertEquals(0, chatResp.getCode());

        ApiResponse<ChatContextDTO> ctx = conversationController.getContext(sessionId);
        Assertions.assertEquals(0, ctx.getCode());
        Assertions.assertNotNull(ctx.getData());
        Assertions.assertEquals("你只回答“OK”", ctx.getData().getSystemPrompt());

        List<?> messages = ctx.getData().getMessages();
        Assertions.assertNotNull(messages);
        Assertions.assertTrue(messages.size() >= 2);

        ApiResponse<Boolean> reset = conversationController.resetContext(sessionId);
        Assertions.assertEquals(0, reset.getCode());
        Assertions.assertTrue(Boolean.TRUE.equals(reset.getData()));

        ApiResponse<ChatContextDTO> ctx2 = conversationController.getContext(sessionId);
        Assertions.assertEquals(0, ctx2.getCode());
        Assertions.assertNotNull(ctx2.getData());
        Assertions.assertTrue(ctx2.getData().getMessages().isEmpty());
        Assertions.assertTrue(ctx2.getData().getSystemPrompt() == null || ctx2.getData().getSystemPrompt().isEmpty());
    }
}
```

- [ ] **Step 2: Run tests (should fail)**

Run:

```powershell
cd c:\Users\tangs\OneDrive\Desktop\Lightmark-main\backend
.\mvnw.cmd -q -DskipTests=false test
```

Expected: FAIL，原因包含 `getContext/resetContext` 不存在（编译失败）或断言不通过。

- [ ] **Step 3: Add chat DTOs**

Create `backend/src/main/java/top/ortus/timemark/backend/dto/chat/ChatMessageDTO.java`:

```java
package top.ortus.timemark.backend.dto.chat;

public class ChatMessageDTO {
    private String role;
    private String content;

    public ChatMessageDTO() {
    }

    public ChatMessageDTO(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
```

Create `backend/src/main/java/top/ortus/timemark/backend/dto/chat/ChatContextDTO.java`:

```java
package top.ortus.timemark.backend.dto.chat;

import java.util.ArrayList;
import java.util.List;

public class ChatContextDTO {
    private String systemPrompt;
    private List<ChatMessageDTO> messages = new ArrayList<>();

    public ChatContextDTO() {
    }

    public ChatContextDTO(String systemPrompt, List<ChatMessageDTO> messages) {
        this.systemPrompt = systemPrompt;
        this.messages = messages == null ? new ArrayList<>() : messages;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public List<ChatMessageDTO> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessageDTO> messages) {
        this.messages = messages == null ? new ArrayList<>() : messages;
    }
}
```

- [ ] **Step 4: Implement ConversationService support for systemPrompt + get/reset context**

Update `backend/src/main/java/top/ortus/timemark/backend/service/ConversationService.java`:

```java
package top.ortus.timemark.backend.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import top.ortus.timemark.backend.dto.AiDTO;
import top.ortus.timemark.backend.dto.chat.ChatContextDTO;
import top.ortus.timemark.backend.dto.chat.ChatMessageDTO;
import top.ortus.timemark.backend.tools.UpdateEmailTool;
import top.ortus.timemark.backend.tools.UpdateNicknameTool;
import top.ortus.timemark.backend.tools.WebSearchTool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConversationService {
    private static final int MAX_HISTORY_SIZE = 30;

    private final ConcurrentHashMap<String, List<Message>> conversations = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> systemPrompts = new ConcurrentHashMap<>();

    private final DeepSeekChatModel chatModel;
    private final ChatClient chatClient;
    private final DeepSeekChatOptions defaultOptions;

    public ConversationService(DeepSeekChatModel chatModel,
                               ChatClient.Builder chatClientBuilder,
                               UpdateNicknameTool updateNicknameTool,
                               UpdateEmailTool updateEmailTool,
                               WebSearchTool webSearchTool) {
        this.chatModel = chatModel;
        this.defaultOptions = new DeepSeekChatOptions.Builder()
                .temperature(0.7)
                .internalToolExecutionEnabled(true)
                .build();
        this.chatClient = chatClientBuilder
                .defaultTools(updateNicknameTool, updateEmailTool, webSearchTool)
                .build();
    }

    public AiDTO chat(String sessionId, String userMessage, String systemPrompt) {
        List<Message> history = conversations.computeIfAbsent(sessionId, id -> new ArrayList<>());
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            systemPrompts.put(sessionId, systemPrompt);
        }
        history.add(new UserMessage(userMessage));
        trimHistory(history, MAX_HISTORY_SIZE);

        String effectiveSystemPrompt = systemPrompts.get(sessionId);

        String aiMessage = chatClient.prompt()
                .system(effectiveSystemPrompt == null ? "" : effectiveSystemPrompt)
                .messages(history)
                .options(defaultOptions)
                .call()
                .content();

        if (aiMessage != null) {
            history.add(new AssistantMessage(aiMessage));
            trimHistory(history, MAX_HISTORY_SIZE);
        }

        AiDTO aiDTO = new AiDTO();
        aiDTO.setContent(aiMessage);
        aiDTO.setModel(chatModel.getDefaultOptions().getModel());
        return aiDTO;
    }

    public ChatContextDTO getContext(String sessionId) {
        List<Message> history = conversations.getOrDefault(sessionId, List.of());
        List<ChatMessageDTO> items = new ArrayList<>();
        for (Message m : history) {
            String role = m.getMessageType() == null ? "" : m.getMessageType().name().toLowerCase();
            items.add(new ChatMessageDTO(role, m.getContent()));
        }
        return new ChatContextDTO(systemPrompts.getOrDefault(sessionId, ""), items);
    }

    public boolean resetContext(String sessionId) {
        conversations.remove(sessionId);
        systemPrompts.remove(sessionId);
        return true;
    }

    private void trimHistory(List<Message> history, int maxSize) {
        while (history.size() > maxSize) {
            history.remove(0);
        }
    }

    public AiDTO regionComplete(String sessionId, String regionText, String surroundingText) {
        List<Message> history = conversations.computeIfAbsent(sessionId, id -> new ArrayList<>());

        String systemPrompt = "请仅返回对指定区域的替换文本，不要输出多余说明。返回结果只包含替换后的区域内容。";
        String userPrompt = "Region to replace:\n" + regionText + "\nContext:\n" + surroundingText;

        String aiMessage = chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .options(defaultOptions)
                .call()
                .content();

        if (aiMessage != null) {
            history.add(new AssistantMessage(aiMessage));
            trimHistory(history, MAX_HISTORY_SIZE);
        }

        AiDTO aiDTO = new AiDTO();
        aiDTO.setContent(aiMessage == null ? "" : aiMessage);
        aiDTO.setModel(chatModel.getDefaultOptions().getModel());
        return aiDTO;
    }

    public void streamChat(String sessionId, String userMessage, SseEmitter emitter) {
        new Thread(() -> {
            try {
                List<Message> history = conversations.computeIfAbsent(sessionId, id -> new ArrayList<>());
                history.add(new UserMessage(userMessage));
                trimHistory(history, MAX_HISTORY_SIZE);

                String effectiveSystemPrompt = systemPrompts.get(sessionId);

                String aiMessage = chatClient.prompt()
                        .system(effectiveSystemPrompt == null ? "" : effectiveSystemPrompt)
                        .messages(history)
                        .options(defaultOptions)
                        .call()
                        .content();

                if (aiMessage != null) {
                    int chunkSize = 100;
                    int len = aiMessage.length();
                    for (int start = 0; start < len; start += chunkSize) {
                        int end = Math.min(len, start + chunkSize);
                        String piece = aiMessage.substring(start, end);
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("partial")
                                    .data(piece));
                        } catch (Exception e) {
                            break;
                        }
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException ignored) {
                        }
                    }
                    emitter.send(SseEmitter.event().name("complete").data("__complete__"));
                    history.add(new AssistantMessage(aiMessage));
                    trimHistory(history, MAX_HISTORY_SIZE);
                } else {
                    emitter.send(SseEmitter.event().name("complete").data(""));
                }

                emitter.complete();
            } catch (Exception ex) {
                try {
                    emitter.send(SseEmitter.event().name("error").data("AI error: " + ex.getMessage()));
                } catch (Exception ignore) {
                }
                emitter.completeWithError(ex);
            }
        }).start();
    }
}
```

- [ ] **Step 5: Add controller endpoints (context/reset) and pass systemPrompt**

Update `backend/src/main/java/top/ortus/timemark/backend/controller/ConversationController.java`:

```java
package top.ortus.timemark.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import top.ortus.timemark.backend.common.ApiResponse;
import top.ortus.timemark.backend.dto.AiDTO;
import top.ortus.timemark.backend.dto.chat.ChatContextDTO;
import top.ortus.timemark.backend.dto.chat.ChatRequest;
import top.ortus.timemark.backend.dto.chat.RegionCompleteRequest;
import top.ortus.timemark.backend.dto.chat.StreamChatRequest;
import top.ortus.timemark.backend.service.ConversationService;

@RestController
@RequestMapping("/api/chat")
public class ConversationController {

    private final ConversationService conversationService;

    @Autowired
    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @PostMapping("")
    public ApiResponse<AiDTO> chat(@RequestBody ChatRequest req) {
        AiDTO aiDTO = conversationService.chat(req.getSessionId(), req.getMessage(), req.getSystemPrompt());
        return ApiResponse.ok(aiDTO);
    }

    @PostMapping("/region/complete")
    public ApiResponse<AiDTO> regionComplete(@RequestBody RegionCompleteRequest req) {
        AiDTO aiDTO = conversationService.regionComplete(req.getSessionId(), req.getRegionText(), req.getSurroundingText());
        return ApiResponse.ok(aiDTO);
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestBody StreamChatRequest req) {
        SseEmitter emitter = new SseEmitter(0L);
        conversationService.streamChat(req.getSessionId(), req.getMessage(), emitter);
        return emitter;
    }

    @GetMapping("/context/{sessionId}")
    public ApiResponse<ChatContextDTO> getContext(@PathVariable String sessionId) {
        return ApiResponse.ok(conversationService.getContext(sessionId));
    }

    @PostMapping("/context/{sessionId}/reset")
    public ApiResponse<Boolean> resetContext(@PathVariable String sessionId) {
        return ApiResponse.ok(conversationService.resetContext(sessionId));
    }
}
```

- [ ] **Step 6: Run backend tests (should pass)**

Run:

```powershell
cd c:\Users\tangs\OneDrive\Desktop\Lightmark-main\backend
.\mvnw.cmd -q test
```

Expected: PASS

- [ ] **Step 7: Commit**

```powershell
cd c:\Users\tangs\OneDrive\Desktop\Lightmark-main
git add backend/src/main/java/top/ortus/timemark/backend/service/ConversationService.java `
  backend/src/main/java/top/ortus/timemark/backend/controller/ConversationController.java `
  backend/src/main/java/top/ortus/timemark/backend/dto/chat/ChatMessageDTO.java `
  backend/src/main/java/top/ortus/timemark/backend/dto/chat/ChatContextDTO.java `
  backend/src/test/java/top/ortus/timemark/backend/controller/ConversationControllerTest.java
git commit -m "feat(m2): add chat context/reset and system prompt support"
```

---

## Task 2: Backend Password Update (real)

**Files:**
- Modify: `backend/src/main/java/top/ortus/timemark/backend/service/UserService.java`
- Modify: `backend/src/main/java/top/ortus/timemark/backend/service/UserServiceImpl.java`
- Modify: `backend/src/main/java/top/ortus/timemark/backend/controller/UserController.java`
- Create: `backend/src/test/java/top/ortus/timemark/backend/controller/UserPasswordControllerTest.java`

- [ ] **Step 1: Write failing test for password update**

Create `backend/src/test/java/top/ortus/timemark/backend/controller/UserPasswordControllerTest.java`:

```java
package top.ortus.timemark.backend.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import top.ortus.timemark.backend.BackendApplication;
import top.ortus.timemark.backend.JwtTokenService;
import top.ortus.timemark.backend.common.ApiResponse;
import top.ortus.timemark.backend.dao.UserRepositoryImpl;
import top.ortus.timemark.backend.dto.user.UserPasswordUpdateRequest;
import top.ortus.timemark.backend.security.UserIdentity;

@SpringBootTest(classes = BackendApplication.class)
public class UserPasswordControllerTest {

    @Autowired
    private UserController userController;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private UserRepositoryImpl userRepository;

    @Test
    public void testUpdatePassword() {
        String token = jwtTokenService.createToken(2L, "普通用户", UserIdentity.USER);
        String authorization = "Bearer " + token;

        UserPasswordUpdateRequest req = new UserPasswordUpdateRequest();
        req.setOldPassword("password");
        req.setNewPassword("new-password-123");

        ApiResponse<Boolean> resp = userController.updatePassword(authorization, req);
        Assertions.assertEquals(0, resp.getCode());
        Assertions.assertTrue(Boolean.TRUE.equals(resp.getData()));

        String hashed = userRepository.findById("2").getPassword();
        Assertions.assertTrue(new BCryptPasswordEncoder().matches("new-password-123", hashed));
    }
}
```

- [ ] **Step 2: Run tests (should fail)**

```powershell
cd c:\Users\tangs\OneDrive\Desktop\Lightmark-main\backend
.\mvnw.cmd -q test
```

Expected: FAIL（旧逻辑始终返回 true，但不会更新 DB 密码，断言不通过）。

- [ ] **Step 3: Add service API for password update**

Update `backend/src/main/java/top/ortus/timemark/backend/service/UserService.java`:

```java
package top.ortus.timemark.backend.service;

import top.ortus.timemark.backend.dto.UserDTO;
import top.ortus.timemark.backend.dto.user.UserCreateRequest;
import top.ortus.timemark.backend.dto.user.UserUpdateRequest;

import java.util.List;

public interface UserService {
    List<UserDTO> findAll();

    UserDTO findById(String id);

    UserDTO findByEmail(String email);

    UserDTO findByPhone(String phone);

    UserDTO create(UserCreateRequest request);

    UserDTO update(String id, UserUpdateRequest request);

    boolean delete(String id);

    boolean updatePassword(String userId, String oldPassword, String newPassword);
}
```

- [ ] **Step 4: Implement password update in UserServiceImpl**

Update `backend/src/main/java/top/ortus/timemark/backend/service/UserServiceImpl.java` (新增 imports 与方法)：

```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import top.ortus.timemark.backend.exception.ApiException;
```

在类中新增字段：

```java
private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
```

新增实现方法：

```java
@Override
public boolean updatePassword(String id, String oldPassword, String newPassword) {
    if (id == null || id.isBlank()) {
        throw new ApiException(401, "unauthorized");
    }
    if (oldPassword == null || oldPassword.isBlank() || newPassword == null || newPassword.isBlank()) {
        throw new ApiException(400, "invalid request");
    }
    User user = userRepositoryImpl.findById(id);
    if (user == null) {
        throw new ApiException(404, "user not found");
    }
    if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
        throw new ApiException(400, "old password incorrect");
    }
    UserUpdateRequest req = new UserUpdateRequest();
    req.setPassword(passwordEncoder.encode(newPassword));
    update(id, req);
    return true;
}
```

- [ ] **Step 5: Wire controller endpoint to service**

Update `backend/src/main/java/top/ortus/timemark/backend/controller/UserController.java` 替换 `updatePassword` 方法体：

```java
@PutMapping("/password")
public ApiResponse<Boolean> updatePassword(@RequestHeader("Authorization") String authorization,
                                           @RequestBody UserPasswordUpdateRequest request) {
    String userId = resolveUserId(authorization);
    if (request == null) {
        return ApiResponse.error(400, "invalid request");
    }
    return ApiResponse.ok(userService.updatePassword(userId, request.getOldPassword(), request.getNewPassword()));
}
```

- [ ] **Step 6: Run backend tests (should pass)**

```powershell
cd c:\Users\tangs\OneDrive\Desktop\Lightmark-main\backend
.\mvnw.cmd -q test
```

Expected: PASS

- [ ] **Step 7: Commit**

```powershell
cd c:\Users\tangs\OneDrive\Desktop\Lightmark-main
git add backend/src/main/java/top/ortus/timemark/backend/service/UserService.java `
  backend/src/main/java/top/ortus/timemark/backend/service/UserServiceImpl.java `
  backend/src/main/java/top/ortus/timemark/backend/controller/UserController.java `
  backend/src/test/java/top/ortus/timemark/backend/controller/UserPasswordControllerTest.java
git commit -m "feat(m2): implement real password update"
```

---

## Task 3: Frontend Chat API Module

**Files:**
- Create: `frontend/src/api/chat.ts`

- [ ] **Step 1: Add chat API wrapper**

Create `frontend/src/api/chat.ts`:

```ts
import { http } from "@/utils/request";

export interface ChatRequest {
  sessionId: string;
  message: string;
  systemPrompt?: string;
}

export interface AiDTO {
  content: string;
  model: string;
}

export interface ChatMessageDTO {
  role: string;
  content: string;
}

export interface ChatContextDTO {
  systemPrompt: string;
  messages: ChatMessageDTO[];
}

export const chat = (data: ChatRequest): Promise<AiDTO> => {
  return http.post<AiDTO>("/chat", data);
};

export const getContext = (sessionId: string): Promise<ChatContextDTO> => {
  return http.get<ChatContextDTO>(`/chat/context/${encodeURIComponent(sessionId)}`);
};

export const resetContext = (sessionId: string): Promise<boolean> => {
  return http.post<boolean>(`/chat/context/${encodeURIComponent(sessionId)}/reset`);
};

type StreamHandlers = {
  onPartial?: (text: string) => void;
  onComplete?: () => void;
  onError?: (error: string) => void;
};

export const streamChat = async (
  sessionId: string,
  message: string,
  handlers: StreamHandlers = {},
  signal?: AbortSignal
) => {
  const baseUrl = (process.env.VUE_APP_API_BASE_URL || "/api").replace(/\/+$/, "");
  const resp = await fetch(`${baseUrl}/chat/stream`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ sessionId, message }),
    signal,
  });

  if (!resp.ok || !resp.body) {
    handlers.onError?.(`HTTP ${resp.status}`);
    return;
  }

  const reader = resp.body.getReader();
  const decoder = new TextDecoder("utf-8");
  let buffer = "";

  const emit = (event: string, data: string) => {
    if (event === "partial") handlers.onPartial?.(data);
    if (event === "complete") handlers.onComplete?.();
    if (event === "error") handlers.onError?.(data);
  };

  while (true) {
    const { value, done } = await reader.read();
    if (done) break;
    buffer += decoder.decode(value, { stream: true });

    while (true) {
      const idx = buffer.indexOf("\n\n");
      if (idx === -1) break;
      const raw = buffer.slice(0, idx);
      buffer = buffer.slice(idx + 2);

      const lines = raw.split("\n");
      let event = "message";
      let data = "";
      for (const line of lines) {
        if (line.startsWith("event:")) event = line.slice("event:".length).trim();
        if (line.startsWith("data:")) data += line.slice("data:".length).trim();
      }
      emit(event, data);
    }
  }
};
```

- [ ] **Step 2: Commit**

```powershell
cd c:\Users\tangs\OneDrive\Desktop\Lightmark-main
git add frontend/src/api/chat.ts
git commit -m "feat(m2): add chat api client"
```

---

## Task 4: Floating AI Assistant Component (Global)

**Files:**
- Create: `frontend/src/components/ai/FloatingAssistant.vue`
- Modify: `frontend/src/App.vue`

- [ ] **Step 1: Create floating assistant component**

Create `frontend/src/components/ai/FloatingAssistant.vue`:

```vue
<template>
  <div class="ai-float">
    <button class="ai-fab" @click="toggleOpen" :aria-expanded="open">
      AI
    </button>

    <div v-if="open" class="ai-panel" role="dialog" aria-label="AI 客服">
      <div class="ai-panel-header">
        <div class="ai-title">AI 客服</div>
        <div class="ai-actions">
          <button class="ai-btn" @click="handleReset" :disabled="loading">清空</button>
          <button class="ai-btn" @click="toggleOpen">关闭</button>
        </div>
      </div>

      <div class="ai-messages" ref="listEl">
        <div
          v-for="(m, idx) in messages"
          :key="idx"
          class="ai-msg"
          :class="m.role === 'user' ? 'from-user' : 'from-ai'"
        >
          <div class="ai-bubble">{{ m.content }}</div>
        </div>
        <div v-if="loading" class="ai-msg from-ai">
          <div class="ai-bubble">正在回复...</div>
        </div>
      </div>

      <form class="ai-input" @submit.prevent="handleSend">
        <input
          v-model="draft"
          class="ai-text"
          type="text"
          placeholder="输入你的问题"
          :disabled="loading"
        />
        <button class="ai-send" type="submit" :disabled="loading || !draft.trim()">
          发送
        </button>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from "vue";
import { ElMessage } from "element-plus";
import { useAuthStore } from "@/stores/auth";
import { chat, getContext, resetContext } from "@/api/chat";

type UiMessage = { role: "user" | "assistant"; content: string };

const authStore = useAuthStore();
const open = ref(false);
const loading = ref(false);
const draft = ref("");
const messages = ref<UiMessage[]>([]);
const listEl = ref<HTMLElement | null>(null);

const systemPrompt = "你是一个乐于助人的旅游网站助手，你的所有回答尽量以中文进行";

const ensureGuestSessionId = () => {
  const key = "lightmark_guest_ai_session";
  const existing = localStorage.getItem(key);
  if (existing) return existing;
  const created = `guest_${Date.now()}_${Math.random().toString(16).slice(2)}`;
  localStorage.setItem(key, created);
  return created;
};

const sessionId = computed(() => authStore.userId || ensureGuestSessionId());

const scrollToBottom = async () => {
  await nextTick();
  if (listEl.value) {
    listEl.value.scrollTop = listEl.value.scrollHeight;
  }
};

const loadHistory = async () => {
  try {
    const ctx = await getContext(sessionId.value);
    const mapped = (ctx.messages || []).map((m) => {
      const role = m.role === "user" ? "user" : "assistant";
      return { role, content: m.content } as UiMessage;
    });
    messages.value = mapped;
    await scrollToBottom();
  } catch {
    messages.value = [];
  }
};

const toggleOpen = async () => {
  open.value = !open.value;
  if (open.value) {
    await loadHistory();
  }
};

const handleSend = async () => {
  const text = draft.value.trim();
  if (!text) return;

  messages.value.push({ role: "user", content: text });
  draft.value = "";
  loading.value = true;
  await scrollToBottom();

  try {
    const res = await chat({
      sessionId: sessionId.value,
      message: text,
      systemPrompt,
    });
    const content = (res?.content || "").trim() || "未收到回复";
    messages.value.push({ role: "assistant", content });
  } catch {
    ElMessage.error("发送失败，请稍后重试");
  } finally {
    loading.value = false;
    await scrollToBottom();
  }
};

const handleReset = async () => {
  loading.value = true;
  try {
    await resetContext(sessionId.value);
    messages.value = [];
    ElMessage.success("已清空会话");
  } catch {
    ElMessage.error("清空失败");
  } finally {
    loading.value = false;
  }
};

watch(
  () => authStore.userId,
  async () => {
    if (open.value) await loadHistory();
  }
);

onMounted(() => {
  authStore.hydrate();
});
</script>

<style scoped>
.ai-float {
  position: fixed;
  right: 18px;
  bottom: 18px;
  z-index: 1200;
}

.ai-fab {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--gold-500), var(--accent-hover));
  color: var(--white);
  font-weight: 800;
  letter-spacing: 0.5px;
  box-shadow: 0 12px 30px rgba(10, 22, 40, 0.18);
}

.ai-panel {
  position: absolute;
  right: 0;
  bottom: 64px;
  width: 360px;
  height: 520px;
  background: var(--white);
  border: 1px solid var(--slate-100);
  border-radius: var(--radius-md);
  overflow: hidden;
  box-shadow: 0 18px 50px rgba(10, 22, 40, 0.22);
  display: flex;
  flex-direction: column;
}

.ai-panel-header {
  padding: 12px 14px;
  border-bottom: 1px solid var(--slate-100);
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, rgba(201, 149, 61, 0.12), rgba(255, 255, 255, 0));
}

.ai-title {
  font-weight: 800;
  color: var(--navy-900);
}

.ai-actions {
  display: flex;
  gap: 8px;
}

.ai-btn {
  font-size: 12px;
  padding: 6px 10px;
  border-radius: 8px;
  border: 1px solid var(--slate-200);
  background: var(--cream-50);
  color: var(--slate-700);
}

.ai-btn:disabled {
  opacity: 0.6;
}

.ai-messages {
  flex: 1;
  overflow: auto;
  padding: 12px;
  background: var(--cream-50);
}

.ai-msg {
  display: flex;
  margin-bottom: 10px;
}

.ai-msg.from-user {
  justify-content: flex-end;
}

.ai-msg.from-ai {
  justify-content: flex-start;
}

.ai-bubble {
  max-width: 78%;
  padding: 10px 12px;
  border-radius: 14px;
  font-size: 14px;
  line-height: 1.45;
  white-space: pre-wrap;
  word-break: break-word;
}

.from-user .ai-bubble {
  background: rgba(201, 149, 61, 0.15);
  color: var(--navy-900);
  border: 1px solid rgba(201, 149, 61, 0.25);
}

.from-ai .ai-bubble {
  background: var(--white);
  color: var(--navy-900);
  border: 1px solid var(--slate-200);
}

.ai-input {
  padding: 10px;
  border-top: 1px solid var(--slate-100);
  display: flex;
  gap: 8px;
  background: var(--white);
}

.ai-text {
  flex: 1;
  padding: 10px 12px;
  border-radius: 10px;
  border: 1.5px solid var(--slate-200);
  outline: none;
  background: var(--cream-50);
}

.ai-send {
  padding: 10px 12px;
  border-radius: 10px;
  background: var(--accent);
  color: var(--white);
  font-weight: 700;
}

.ai-send:disabled {
  opacity: 0.6;
}

@media (max-width: 480px) {
  .ai-panel {
    width: calc(100vw - 24px);
    height: min(560px, calc(100vh - 120px));
    right: -6px;
  }
}
</style>
```

- [ ] **Step 2: Mount component in App.vue**

Update `frontend/src/App.vue`:

```vue
<template>
  <template v-if="isAdminRoute">
    <router-view />
  </template>
  <template v-else>
    <AppHeader />
    <main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="page" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
    <FloatingAssistant />
    <AppFooter />
  </template>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useRoute } from "vue-router";
import AppHeader from "@/components/AppHeader.vue";
import AppFooter from "@/components/AppFooter.vue";
import FloatingAssistant from "@/components/ai/FloatingAssistant.vue";

const route = useRoute();
const isAdminRoute = computed(() => route.path.startsWith("/admin"));
</script>

<style scoped>
.main-content {
  flex: 1;
  padding-top: 64px;
}
</style>
```

- [ ] **Step 3: Frontend smoke check**

Run:

```powershell
cd c:\Users\tangs\OneDrive\Desktop\Lightmark-main\frontend
npm run lint
npm run serve
```

Manual: 打开首页，确认右下角出现 “AI” 悬浮按钮，可打开面板并发送消息。

- [ ] **Step 4: Commit**

```powershell
cd c:\Users\tangs\OneDrive\Desktop\Lightmark-main
git add frontend/src/components/ai/FloatingAssistant.vue frontend/src/App.vue
git commit -m "feat(m2): add floating ai assistant"
```

---

## Task 5: User Center Page Enhancements (M2 core)

**Files:**
- Modify: `frontend/src/views/module/UserCenterView.vue`

- [ ] **Step 1: Extend user center with travelers/points/orders/password**

Update `frontend/src/views/module/UserCenterView.vue`（在现有“个人信息/快捷操作”基础上新增分区；保持现有风格与 Element Plus 提示方式）：

1) 在 `<script setup>` 增加 imports：

```ts
import {
  addTraveler,
  deleteTraveler,
  getLevelUpgradeInfo,
  getPointsLogs,
  getTravelers,
  getUserOrders,
  updateTraveler,
  changePassword,
} from "@/api/user";
```

2) 增加状态与拉取函数（可直接追加到现有脚本中）：

```ts
const travelers = ref<any[]>([]);
const orders = ref<any[]>([]);
const pointsLogs = ref<any[]>([]);
const upgradeInfo = ref<any | null>(null);

const travelerDialogOpen = ref(false);
const travelerSaving = ref(false);
const travelerForm = ref({ id: 0, name: "", idCard: "", phone: "" });

const passwordForm = ref({ oldPassword: "", newPassword: "", confirmPassword: "" });
const passwordSaving = ref(false);

const fetchTravelers = async () => {
  try {
    travelers.value = await getTravelers();
  } catch {
    travelers.value = [];
  }
};

const fetchPointsLogs = async () => {
  try {
    const res = await getPointsLogs();
    pointsLogs.value = res.list || [];
  } catch {
    pointsLogs.value = [];
  }
};

const fetchUpgradeInfo = async () => {
  try {
    upgradeInfo.value = await getLevelUpgradeInfo();
  } catch {
    upgradeInfo.value = null;
  }
};

const fetchOrders = async () => {
  try {
    const res = await getUserOrders();
    orders.value = res.list || [];
  } catch {
    orders.value = [];
  }
};
```

3) 增加出行人 CRUD：

```ts
const openCreateTraveler = () => {
  travelerForm.value = { id: 0, name: "", idCard: "", phone: "" };
  travelerDialogOpen.value = true;
};

const openEditTraveler = (t: any) => {
  travelerForm.value = { id: t.id || 0, name: t.name || "", idCard: t.idCard || "", phone: t.phone || "" };
  travelerDialogOpen.value = true;
};

const saveTraveler = async () => {
  if (!travelerForm.value.name.trim() || !travelerForm.value.idCard.trim()) {
    ElMessage.warning("请填写姓名与证件号");
    return;
  }
  travelerSaving.value = true;
  try {
    if (travelerForm.value.id) {
      await updateTraveler(Number(travelerForm.value.id), {
        id: travelerForm.value.id,
        name: travelerForm.value.name,
        idCard: travelerForm.value.idCard,
        phone: travelerForm.value.phone,
      });
    } else {
      await addTraveler({
        name: travelerForm.value.name,
        idCard: travelerForm.value.idCard,
        phone: travelerForm.value.phone,
      });
    }
    travelerDialogOpen.value = false;
    await fetchTravelers();
    ElMessage.success("保存成功");
  } catch {
    ElMessage.error("保存失败");
  } finally {
    travelerSaving.value = false;
  }
};

const removeTraveler = async (t: any) => {
  try {
    await ElMessageBox.confirm("确定删除该出行人吗？", "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    });
    await deleteTraveler(Number(t.id));
    await fetchTravelers();
    ElMessage.success("已删除");
  } catch {
  }
};
```

4) 增加修改密码：

```ts
const handleChangePassword = async () => {
  if (!passwordForm.value.oldPassword || !passwordForm.value.newPassword) {
    ElMessage.warning("请填写旧密码与新密码");
    return;
  }
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    ElMessage.warning("两次新密码不一致");
    return;
  }
  passwordSaving.value = true;
  try {
    await changePassword({
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword,
    });
    passwordForm.value = { oldPassword: "", newPassword: "", confirmPassword: "" };
    ElMessage.success("密码修改成功");
  } catch {
    ElMessage.error("密码修改失败");
  } finally {
    passwordSaving.value = false;
  }
};
```

5) `onMounted` 中追加拉取：

```ts
onMounted(() => {
  fetchUserProfile();
  fetchTravelers();
  fetchPointsLogs();
  fetchUpgradeInfo();
  fetchOrders();
});
```

6) 在 `<template>` 的 `.profile-body` 中，现有 `.profile-grid` 后追加 4 个卡片区块（示例结构，按现有卡片样式复用 class）：

```vue
<div class="container profile-body">
  <div class="profile-grid">
    <!-- 原有两张卡片不变 -->
  </div>

  <div class="profile-grid profile-grid-2">
    <div class="profile-card">
      <div class="card-header">
        <h3>常用出行人</h3>
        <button class="btn-text" @click="openCreateTraveler">新增</button>
      </div>
      <div class="card-body">
        <div v-if="!travelers.length" class="empty">暂无出行人</div>
        <div v-else class="list">
          <div class="list-item" v-for="t in travelers" :key="t.id">
            <div class="list-main">
              <div class="list-title">{{ t.name }}</div>
              <div class="list-sub">{{ t.idCard }} {{ t.phone || "" }}</div>
            </div>
            <div class="list-actions">
              <button class="btn-text" @click="openEditTraveler(t)">编辑</button>
              <button class="btn-text danger" @click="removeTraveler(t)">删除</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="profile-card">
      <div class="card-header">
        <h3>积分与会员</h3>
      </div>
      <div class="card-body">
        <div class="info-row" v-if="upgradeInfo">
          <span class="info-label">当前等级</span>
          <span class="info-value">Lv.{{ upgradeInfo.currentLevel }}</span>
        </div>
        <div class="info-row" v-if="upgradeInfo">
          <span class="info-label">当前积分</span>
          <span class="info-value">{{ upgradeInfo.currentPoints }}</span>
        </div>
        <div class="info-row" v-if="upgradeInfo">
          <span class="info-label">距离下一等级</span>
          <span class="info-value">{{ upgradeInfo.pointsToNext }}</span>
        </div>
        <div class="divider"></div>
        <div v-if="!pointsLogs.length" class="empty">暂无积分明细</div>
        <div v-else class="list">
          <div class="list-item" v-for="p in pointsLogs" :key="p.id">
            <div class="list-main">
              <div class="list-title">{{ p.type }} {{ p.points }}</div>
              <div class="list-sub">{{ p.remark }} {{ p.createTime }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="profile-card">
      <div class="card-header">
        <h3>我的订单</h3>
      </div>
      <div class="card-body">
        <div v-if="!orders.length" class="empty">暂无订单</div>
        <div v-else class="list">
          <div class="list-item" v-for="o in orders" :key="o.orderNo">
            <div class="list-main">
              <div class="list-title">{{ o.productName }}</div>
              <div class="list-sub">{{ o.orderType }} ¥{{ o.payAmount }} {{ o.createTime }}</div>
            </div>
            <div class="list-actions">
              <span class="status">状态: {{ o.status }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="profile-card">
      <div class="card-header">
        <h3>安全设置</h3>
      </div>
      <div class="card-body">
        <div class="form-field">
          <label>旧密码</label>
          <input v-model="passwordForm.oldPassword" type="password" class="form-input" placeholder="请输入旧密码" />
        </div>
        <div class="form-field">
          <label>新密码</label>
          <input v-model="passwordForm.newPassword" type="password" class="form-input" placeholder="请输入新密码" />
        </div>
        <div class="form-field">
          <label>确认新密码</label>
          <input v-model="passwordForm.confirmPassword" type="password" class="form-input" placeholder="请再次输入新密码" />
        </div>
        <button class="btn btn-primary btn-save" @click="handleChangePassword" :disabled="passwordSaving">
          {{ passwordSaving ? "提交中..." : "修改密码" }}
        </button>
      </div>
    </div>
  </div>

  <el-dialog v-model="travelerDialogOpen" title="出行人" width="420px">
    <div class="form-field">
      <label>姓名</label>
      <input v-model="travelerForm.name" type="text" class="form-input" placeholder="请输入姓名" />
    </div>
    <div class="form-field">
      <label>证件号</label>
      <input v-model="travelerForm.idCard" type="text" class="form-input" placeholder="请输入证件号" />
    </div>
    <div class="form-field">
      <label>手机号</label>
      <input v-model="travelerForm.phone" type="text" class="form-input" placeholder="可选" />
    </div>
    <template #footer>
      <button class="btn btn-secondary" @click="travelerDialogOpen = false">取消</button>
      <button class="btn btn-primary" @click="saveTraveler" :disabled="travelerSaving">
        {{ travelerSaving ? "保存中..." : "保存" }}
      </button>
    </template>
  </el-dialog>
</div>
```

7) 在 `<style scoped>` 追加必要样式（直接追加到文件末尾）：

```css
.profile-grid-2 {
  margin-top: 24px;
  grid-template-columns: 1fr 1fr;
}

.empty {
  color: var(--text-secondary);
  font-size: 13px;
  padding: 10px 0;
}

.list {
  display: grid;
  gap: 10px;
}

.list-item {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 12px;
  border-radius: 12px;
  border: 1px solid var(--slate-200);
  background: var(--white);
}

.list-title {
  font-weight: 700;
  color: var(--navy-900);
  font-size: 14px;
}

.list-sub {
  margin-top: 4px;
  font-size: 12px;
  color: var(--text-secondary);
}

.list-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.btn-text.danger {
  color: var(--cta);
}

.divider {
  height: 1px;
  background: var(--slate-100);
  margin: 14px 0;
}

.status {
  font-size: 12px;
  color: var(--text-secondary);
}

@media (max-width: 768px) {
  .profile-grid-2 {
    grid-template-columns: 1fr;
  }
}
```

- [ ] **Step 2: Frontend lint & manual verification**

```powershell
cd c:\Users\tangs\OneDrive\Desktop\Lightmark-main\frontend
npm run lint
npm run serve
```

Manual:

- 进入 `/user-center`，确认能加载：出行人、积分明细、等级信息、订单
- 出行人新增/编辑/删除成功后能刷新列表
- 修改密码成功/失败都有提示

- [ ] **Step 3: Commit**

```powershell
cd c:\Users\tangs\OneDrive\Desktop\Lightmark-main
git add frontend/src/views/module/UserCenterView.vue
git commit -m "feat(m2): enhance user center with travelers points orders and password"
```

---

## Self-Review Checklist

- 覆盖 spec 中：`/api/chat` systemPrompt 生效、context/reset、密码修改、悬浮客服、用户中心四大区块（资料/出行人/积分订单/安全设置）
- 计划内无 “TODO/TBD” 占位
- 所有新增文件/修改文件路径明确且可直接执行命令验证

---

## Execution Handoff

Plan complete and saved to `docs/superpowers/plans/2026-05-26-m2-user-center-ai-assistant.md`. Two execution options:

1. Inline Execution - Execute tasks in this session using superpowers:executing-plans, with review checkpoints
2. Subagent-Driven - Dispatch a fresh subagent per task and review between tasks

Which approach?

