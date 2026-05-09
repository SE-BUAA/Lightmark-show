export interface AuthStateSnapshot {
  token: string;
  userId: string;
  nickname: string;
  avatar: string;
  isAdmin: boolean;
  roles: string[];
}

const AUTH_STORAGE_KEY = "lightmark_auth";

const EMPTY_SNAPSHOT: AuthStateSnapshot = {
  token: "",
  userId: "",
  nickname: "",
  avatar: "",
  isAdmin: false,
  roles: [],
};

const safeParse = (raw: string | null): AuthStateSnapshot => {
  if (!raw) {
    return { ...EMPTY_SNAPSHOT };
  }

  try {
    const parsed = JSON.parse(raw) as Partial<AuthStateSnapshot>;
    return {
      token: parsed.token ?? "",
      userId: parsed.userId ?? "",
      nickname: parsed.nickname ?? "",
      avatar: parsed.avatar ?? "",
      isAdmin: Boolean(parsed.isAdmin),
      roles: Array.isArray(parsed.roles) ? parsed.roles : [],
    };
  } catch {
    return { ...EMPTY_SNAPSHOT };
  }
};

export const getAuthSnapshot = (): AuthStateSnapshot => {
  return safeParse(localStorage.getItem(AUTH_STORAGE_KEY));
};

export const setAuthSnapshot = (snapshot: AuthStateSnapshot): void => {
  localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(snapshot));
};

export const clearAuthSnapshot = (): void => {
  localStorage.removeItem(AUTH_STORAGE_KEY);
};

export const parseAdminFlag = (payload: Record<string, unknown>): boolean => {
  // 优先检查 identity 枚举（新 API 主键）
  if (payload.identity === "ADMIN") return true;

  const byBoolean =
    payload.isAdmin === true ||
    payload.admin === true ||
    payload.is_admin === true;

  const roleField = payload.role;
  const rolesField = payload.roles;

  const byRole =
    (typeof roleField === "string" && roleField.toUpperCase() === "ADMIN") ||
    (Array.isArray(rolesField) &&
      rolesField.some(
        (item) => typeof item === "string" && item.toUpperCase() === "ADMIN"
      ));

  return byBoolean || byRole;
};

export const extractRoles = (payload: Record<string, unknown>): string[] => {
  const rolesField = payload.roles;
  const roleField = payload.role;

  if (Array.isArray(rolesField)) {
    return rolesField.filter(
      (item): item is string => typeof item === "string"
    );
  }

  // 后备：从 identity 推导 roles
  if (typeof roleField === "string") {
    return [roleField];
  }

  if (payload.identity === "ADMIN") return ["ADMIN"];
  if (payload.identity === "USER") return ["USER"];

  return [];
};
