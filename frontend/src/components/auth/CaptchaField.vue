<script setup lang="ts">
const model = defineModel<string>({ default: "" });

defineProps<{
  imageUrl: string;
}>();

const emit = defineEmits<{
  refresh: [];
}>();
</script>

<template>
  <div class="captcha-field">
    <el-input v-model="model" placeholder="请输入图形验证码" />
    <img class="captcha-image" :src="imageUrl" alt="captcha" @click="emit('refresh')" />
    <button class="captcha-refresh" type="button" @click="emit('refresh')">
      看不清？换一张
    </button>
  </div>
</template>

<style scoped>
.captcha-field {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 120px;
  gap: 12px;
  align-items: center;
}

.captcha-image {
  width: 120px;
  height: 42px;
  border-radius: 12px;
  cursor: pointer;
  background: #fff;
  object-fit: cover;
}

.captcha-refresh {
  grid-column: 1 / -1;
  justify-self: end;
  background: transparent;
  border: none;
  color: rgba(255, 255, 255, 0.72);
  cursor: pointer;
  padding: 0;
}
</style>
