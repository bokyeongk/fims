type Provider = 'google' | 'naver' | 'kakao'

interface SocialLoginButtonProps {
  provider: Provider
  label: string
}

const providerStyles: Record<Provider, { bg: string; text: string; border?: string }> = {
  google: { bg: 'bg-white', text: 'text-gray-800', border: 'border border-gray-300' },
  kakao: { bg: 'bg-[#FEE500]', text: 'text-[#191919]' },
  naver: { bg: 'bg-[#03C75A]', text: 'text-white' },
}

const GoogleIcon = () => (
  <svg width="20" height="20" viewBox="0 0 48 48" aria-hidden="true">
    <path fill="#EA4335" d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z" />
    <path fill="#4285F4" d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z" />
    <path fill="#FBBC05" d="M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.27-3.14.76-4.59l-7.98-6.19C.92 16.46 0 20.12 0 24c0 3.88.92 7.54 2.56 10.78l7.97-6.19z" />
    <path fill="#34A853" d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.15 1.45-4.92 2.3-8.16 2.3-6.26 0-11.57-4.22-13.47-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z" />
  </svg>
)

const KakaoIcon = () => (
  <svg width="20" height="20" viewBox="0 0 40 40" aria-hidden="true">
    <path
      fill="#191919"
      d="M20 4C10.06 4 2 10.27 2 18c0 5.01 3.18 9.41 8 12.07L8.5 36l6.6-4.38C16.33 31.87 18.13 32 20 32c9.94 0 18-6.27 18-14S29.94 4 20 4z"
    />
  </svg>
)

const NaverIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" aria-hidden="true">
    <path
      fill="#ffffff"
      d="M16.273 12.845L7.376 0H0v24h7.727V11.155L16.624 24H24V0h-7.727z"
    />
  </svg>
)

const iconMap: Record<Provider, React.ReactElement> = {
  google: <GoogleIcon />,
  kakao: <KakaoIcon />,
  naver: <NaverIcon />,
}

const SocialLoginButton = ({ provider, label }: SocialLoginButtonProps) => {
  const styles = providerStyles[provider]
  const borderClass = styles.border ?? ''

  const handleClick = () => {
    window.location.href = `http://localhost:8080/oauth2/authorization/${provider}`
  }

  return (
    <button
      type="button"
      onClick={handleClick}
      className={`relative flex w-full items-center justify-center gap-3 rounded-lg px-4 py-3 h-12 font-medium transition-opacity hover:opacity-90 active:opacity-75 ${styles.bg} ${styles.text} ${borderClass}`}
      aria-label={`${label}로 로그인`}
    >
      <span className="absolute left-4 flex items-center">{iconMap[provider]}</span>
      <span>{label}</span>
    </button>
  )
}

export default SocialLoginButton
