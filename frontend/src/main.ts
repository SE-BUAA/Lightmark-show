import { createApp } from "vue";
import { createPinia } from "pinia";
import ElementPlus from "element-plus";
import "element-plus/dist/index.css";
import App from "./App.vue";
import router from "./router";
import "@/assets/styles/global.css";
import "@/assets/styles/admin.css";

// 创建Vue应用实例
const app = createApp(App);

// 使用Pinia状态管理插件
app.use(createPinia());
// 使用Element Plus UI组件库
app.use(ElementPlus);
// 使用路由插件
app.use(router);

// 将应用挂载到DOM元素上
app.mount("#app");
