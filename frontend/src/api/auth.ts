import { useMutation } from '@tanstack/react-query'
import apiClient from './client'

const logout = () => apiClient.post('/v1/auth/logout')

export const useLogout = () =>
  useMutation({
    mutationFn: logout,
    onSuccess: () => {
      // React Query 캐시 및 클라이언트 상태 완전 초기화를 위해 전체 페이지 리로드
      window.location.href = '/login'
    },
  })
