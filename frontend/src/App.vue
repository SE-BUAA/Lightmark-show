<template>
  <template v-if="isAdminRoute">
    <router-view />
  </template>
  <template v-else>
    <AppHeader />
    <main class="main-content">
      <router-view v-slot="{ Component }">
        <component :is="Component" />
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
