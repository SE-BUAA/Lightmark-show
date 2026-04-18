<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { pingModule } from '@/api/placeholder'

const props = defineProps<{
  moduleId: string
  title: string
}>()

const message = ref('')
const errorMessage = ref('')

onMounted(async () => {
  try {
    const data = await pingModule(props.moduleId)
    message.value = JSON.stringify(data)
  } catch (error: unknown) {
    errorMessage.value = error instanceof Error ? error.message : '当前模块暂未开放'
  }
})
</script>

<template>
  <section>
    <h2>{{ title }}</h2>
    <p class="hint">当前模块保留 API 占位，后续迭代直接替换实现。</p>
    <p v-if="message" class="hint">{{ message }}</p>
    <p v-if="errorMessage" class="error">{{ errorMessage }}</p>
  </section>
</template>

