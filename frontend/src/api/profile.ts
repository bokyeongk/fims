import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import apiClient from './client'

interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
}

export interface SocialAccount {
  provider: 'GOOGLE' | 'NAVER' | 'KAKAO'
  email: string | null
}

export interface ProfileResponse {
  id: number
  name: string
  phone: string | null
  email: string
  signatureData: string | null
  socialAccounts: SocialAccount[]
}

interface UpdateProfileRequest {
  name: string
  phone: string
  email: string
}

interface CheckEmailResponse {
  available: boolean
}

interface SaveSignatureRequest {
  signatureData: string
}

interface SaveSignatureResponse {
  signatureData: string
}

const PROFILE_QUERY_KEY = ['profile']

// GET /api/v1/users/profile
export const useProfile = () =>
  useQuery<ProfileResponse>({
    queryKey: PROFILE_QUERY_KEY,
    queryFn: () =>
      apiClient
        .get<ApiResponse<ProfileResponse>>('/v1/users/profile')
        .then((res) => res.data.data),
  })

// PUT /api/v1/users/profile
export const useUpdateProfile = () => {
  const queryClient = useQueryClient()
  return useMutation<ProfileResponse, Error, UpdateProfileRequest>({
    mutationFn: (data) =>
      apiClient
        .put<ApiResponse<ProfileResponse>>('/v1/users/profile', data)
        .then((res) => res.data.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: PROFILE_QUERY_KEY })
    },
  })
}

// GET /api/v1/users/check-email?email=
export const useCheckEmail = () =>
  useMutation<CheckEmailResponse, Error, string>({
    mutationFn: (email) =>
      apiClient
        .get<ApiResponse<CheckEmailResponse>>('/v1/users/check-email', { params: { email } })
        .then((res) => res.data.data),
  })

// POST /api/v1/users/verify-email
export const useSendVerifyEmail = () =>
  useMutation<void, Error, { email: string }>({
    mutationFn: (data) =>
      apiClient.post('/v1/users/verify-email', data).then(() => undefined),
  })

// POST /api/v1/users/verify-email/confirm
export const useConfirmVerifyEmail = () =>
  useMutation<void, Error, { email: string; code: string }>({
    mutationFn: (data) =>
      apiClient.post('/v1/users/verify-email/confirm', data).then(() => undefined),
  })

// PUT /api/v1/users/profile/signature
export const useSaveSignature = () => {
  const queryClient = useQueryClient()
  return useMutation<SaveSignatureResponse, Error, SaveSignatureRequest>({
    mutationFn: (data) =>
      apiClient
        .put<ApiResponse<SaveSignatureResponse>>('/v1/users/profile/signature', data)
        .then((res) => res.data.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: PROFILE_QUERY_KEY })
    },
  })
}

// DELETE /api/v1/users/profile/signature
export const useDeleteSignature = () => {
  const queryClient = useQueryClient()
  return useMutation<void, Error, void>({
    mutationFn: () =>
      apiClient.delete('/v1/users/profile/signature').then(() => undefined),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: PROFILE_QUERY_KEY })
    },
  })
}
