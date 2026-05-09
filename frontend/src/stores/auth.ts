import { defineStore } from "pinia";
import {
  AuthStateSnapshot,
  clearAuthSnapshot,
  extractRoles,
  getAuthSnapshot,
  parseAdminFlag,
  setAuthSnapshot,
} from "@/utils/auth";

type AuthStoreState = AuthStateSnapshot;

export const useAuthStore = defineStore("auth", {
  state: (): AuthStoreState => getAuthSnapshot(),
  getters: {
    isLoggedIn: (state) => Boolean(state.token),
  },
  actions: {
    /** 更新本地用户资料并持久化到 localStorage */
    updateLocalProfile(profile: { nickname?: string; avatar?: string }) {
      if (profile.nickname !== undefined) {
        this.nickname = profile.nickname;
      }
      if (profile.avatar !== undefined) {
        this.avatar = profile.avatar;
      }
      // 同步到 localStorage
      const snapshot = getAuthSnapshot();
      snapshot.nickname = this.nickname;
      snapshot.avatar = this.avatar;
      setAuthSnapshot(snapshot);
    },
    hydrate() {
      const snapshot = getAuthSnapshot();
      this.token = snapshot.token;
      this.userId = snapshot.userId;
      this.nickname = snapshot.nickname;
      this.avatar = snapshot.avatar;
      this.isAdmin = snapshot.isAdmin;
      this.roles = snapshot.roles;
    },
    setSession(payload: Record<string, unknown>, explicitIsAdmin?: boolean) {
      const token = String(payload.token ?? "");
      const userId = String(payload.userId ?? payload.id ?? "");
      const nickname = String(payload.nickname ?? payload.name ?? "");
      const avatar = String(payload.avatar ?? "");

      const isAdmin =
        explicitIsAdmin !== undefined
          ? explicitIsAdmin
          : parseAdminFlag(payload);
      const roles = extractRoles(payload);

      const snapshot: AuthStateSnapshot = {
        token,
        userId,
        nickname,
        avatar,
        isAdmin,
        roles,
      };

      Object.assign(this, snapshot);
      setAuthSnapshot(snapshot);
    },
    clearSession() {
      this.token = "";
      this.userId = "";
      this.nickname = "";
      this.avatar = "";
      this.isAdmin = false;
      this.roles = [];
      clearAuthSnapshot();
    },
  },
});
