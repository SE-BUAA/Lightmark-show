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
  return http.get<ChatContextDTO>(
    `/chat/context/${encodeURIComponent(sessionId)}`
  );
};

export const resetContext = (sessionId: string): Promise<boolean> => {
  return http.post<boolean>(
    `/chat/context/${encodeURIComponent(sessionId)}/reset`
  );
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
  const baseUrl = (process.env.VUE_APP_API_BASE_URL || "/api").replace(
    /\/+$/,
    ""
  );
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
        if (line.startsWith("event:"))
          event = line.slice("event:".length).trim();
        if (line.startsWith("data:")) data += line.slice("data:".length).trim();
      }
      emit(event, data);
    }
  }
};
