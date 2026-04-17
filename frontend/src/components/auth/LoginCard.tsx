import SocialLoginButton from '@/components/auth/SocialLoginButton'

const LoginCard = () => {
  return (
    <div className="w-full rounded-2xl bg-white p-8 shadow-sm">
      <div className="mb-8 text-center">
        <h1 className="text-2xl font-bold text-slate-800">FIMS</h1>
        <p className="mt-1 text-base font-medium text-slate-600">인테리어 필름 시공 관리</p>
        <p className="mt-2 text-sm text-slate-400">견적부터 시공 현장까지 스마트하게</p>
      </div>

      <div className="flex flex-col gap-3">
        <SocialLoginButton provider="kakao" label="카카오로 로그인" />
        <SocialLoginButton provider="naver" label="네이버로 로그인" />
        <SocialLoginButton provider="google" label="Google로 로그인" />
      </div>
    </div>
  )
}

export default LoginCard
