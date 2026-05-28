<template>
  <div class="ai-float">
    <button class="ai-fab" @click="toggleOpen" :aria-expanded="open">AI</button>

    <div v-if="open" class="ai-panel" role="dialog" aria-label="AI 客服">
      <div class="ai-panel-header">
        <div class="ai-title">AI 客服</div>
        <div class="ai-actions">
          <button class="ai-btn" @click="handleReset" :disabled="loading">
            清空
          </button>
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
        <button
          class="ai-send"
          type="submit"
          :disabled="loading || !draft.trim()"
        >
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

const systemPrompt =
  "你是一个乐于助人的旅游网站助手，你的所有回答尽量以中文进行";

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
  background: linear-gradient(
    135deg,
    rgba(201, 149, 61, 0.12),
    rgba(255, 255, 255, 0)
  );
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
