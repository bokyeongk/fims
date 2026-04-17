import { useEffect, useState } from 'react'
import { Navigate, Outlet } from 'react-router-dom'
import apiClient from '@/api/client'

type AuthStatus = 'loading' | 'authenticated' | 'unauthenticated'

const ProtectedRoute = () => {
  const [status, setStatus] = useState<AuthStatus>('loading')

  useEffect(() => {
    apiClient
      .get('/v1/auth/me')
      .then(() => setStatus('authenticated'))
      .catch(() => setStatus('unauthenticated'))
  }, [])

  if (status === 'loading') {
    return (
      <div className="flex min-h-screen items-center justify-center bg-slate-50">
        <svg
          className="h-8 w-8 animate-spin text-slate-400"
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 24 24"
          aria-hidden="true"
        >
          <circle
            className="opacity-25"
            cx="12"
            cy="12"
            r="10"
            stroke="currentColor"
            strokeWidth="4"
          />
          <path
            className="opacity-75"
            fill="currentColor"
            d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"
          />
        </svg>
      </div>
    )
  }

  if (status === 'unauthenticated') {
    return <Navigate to="/login" replace />
  }

  return <Outlet />
}

export default ProtectedRoute
