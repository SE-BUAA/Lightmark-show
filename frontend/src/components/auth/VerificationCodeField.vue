<script setup lang="ts">
const model = defineModel<string>({ default: "" });

defineProps<{
  buttonText: string;
  disabled: boolean;
  loading: boolean;
}>();

const emit = defineEmits<{
  send: [];
}>();
</script>

<template>
  <div class="verification-field">
    <el-input v-model="model" placeholder="请输入收到的验证码" />
    <el-button :disabled="disabled" :loading="loading" @click="emit('send')">
      {{ buttonText }}
    </el-button>
  </div>
</template>

<style scoped>
.verification-field {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 12px;
}
</style>
