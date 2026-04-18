import http from './http'

export function pingModule(moduleId: string): Promise<unknown> {
  return http.get(`/api/${moduleId}/ping`) as unknown as Promise<unknown>
}


