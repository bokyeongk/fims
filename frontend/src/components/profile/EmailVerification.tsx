import { useState } from 'react'
import { useCheckEmail, useSendVerifyEmail, useConfirmVerifyEmail } from '@/api/profile'
import { cn } from '@/lib/utils'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'

interface EmailVerificationProps {
  currentEmail: string
  onVerified: (email: string) => void
}

const EmailVerification = ({ currentEmail, onVerified }: EmailVerificationProps) => {
  const [inputEmail, setInputEmail] = useState(currentEmail)
  const [isDuplicateChecked, setIsDuplicateChecked] = useState(false)
  const [isAvailable, setIsAvailable] = useState(false)
  const [isVerified, setIsVerified] = useState(false)
  const [codeSent, setCodeSent] = useState(false)
  const [code, setCode] = useState('')
  const [errorMessage, setErrorMessage] = useState('')

  const checkEmail = useCheckEmail()
  const sendVerifyEmail = useSendVerifyEmail()
  const confirmVerifyEmail = useConfirmVerifyEmail()

  const handleEmailChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setInputEmail(e.target.value)
    setIsDuplicateChecked(false)
    setIsAvailable(false)
    setIsVerified(false)
    setCodeSent(false)
    setCode('')
    setErrorMessage('')
  }

  const handleCheckDuplicate = async () => {
    setErrorMessage('')
    try {
      const result = await checkEmail.mutateAsync(inputEmail)
      setIsDuplicateChecked(true)
      setIsAvailable(result.available)
      if (!result.available) setErrorMessage('이미 사용 중인 이메일입니다.')
    } catch {
      setErrorMessage('중복 확인 중 오류가 발생했습니다.')
    }
  }

  const handleSendCode = async () => {
    setErrorMessage('')
    try {
      await sendVerifyEmail.mutateAsync({ email: inputEmail })
      setCodeSent(true)
    } catch {
      setErrorMessage('인증 코드 발송에 실패했습니다.')
    }
  }

  const handleConfirmCode = async () => {
    setErrorMessage('')
    try {
      await confirmVerifyEmail.mutateAsync({ email: inputEmail, code })
      setIsVerified(true)
      onVerified(inputEmail)
    } catch {
      setErrorMessage('인증 코드가 올바르지 않거나 만료되었습니다.')
    }
  }

  const inputOverride =
    'h-auto rounded border-slate-300 px-2 py-1 text-sm leading-tight focus-visible:ring-1 focus-visible:ring-slate-500 focus-visible:ring-offset-0 disabled:bg-slate-100'

  return (
    <div className="space-y-1.5 rounded border border-blue-200 bg-blue-50 p-2">
      <p className="text-xs font-medium leading-tight text-blue-700">이메일 변경 인증</p>

      {/* 이메일 입력 + 중복확인 */}
      <div className="flex gap-1">
        <Input
          type="email"
          value={inputEmail}
          onChange={handleEmailChange}
          disabled={isVerified}
          placeholder="새 이메일 입력"
          className={inputOverride}
          aria-label="새 이메일"
        />
        <Button
          type="button"
          onClick={handleCheckDuplicate}
          disabled={!inputEmail || isVerified || checkEmail.isPending}
          className="rounded bg-slate-800 hover:bg-slate-700 px-2 py-1 text-sm leading-tight h-auto whitespace-nowrap"
        >
          {checkEmail.isPending ? '확인 중...' : '중복확인'}
        </Button>
      </div>

      {/* 중복확인 결과 + 코드 발송 */}
      {isDuplicateChecked && isAvailable && !isVerified && (
        <div className="space-y-1">
          <p className="text-xs leading-tight text-green-600">사용 가능한 이메일입니다.</p>
          {!codeSent ? (
            <Button
              type="button"
              onClick={handleSendCode}
              disabled={sendVerifyEmail.isPending}
              className={cn(
                'w-full rounded bg-blue-600 hover:bg-blue-700 px-3 py-1 text-sm leading-tight h-auto',
              )}
            >
              {sendVerifyEmail.isPending ? '발송 중...' : '인증코드 발송'}
            </Button>
          ) : (
            <p className="text-xs leading-tight text-blue-600">인증 코드를 발송했습니다. 이메일을 확인해 주세요.</p>
          )}
        </div>
      )}

      {/* 코드 입력 + 확인 */}
      {codeSent && !isVerified && (
        <div className="flex gap-1">
          <Input
            type="text"
            value={code}
            onChange={(e) => setCode(e.target.value)}
            placeholder="인증 코드 입력"
            maxLength={10}
            className={inputOverride}
            aria-label="인증 코드"
          />
          <Button
            type="button"
            onClick={handleConfirmCode}
            disabled={!code || confirmVerifyEmail.isPending}
            className="rounded bg-blue-600 hover:bg-blue-700 px-2 py-1 text-sm leading-tight h-auto whitespace-nowrap"
          >
            {confirmVerifyEmail.isPending ? '확인 중...' : '확인'}
          </Button>
        </div>
      )}

      {isVerified && (
        <p className="text-xs font-medium leading-tight text-green-600">이메일 인증이 완료되었습니다.</p>
      )}

      {errorMessage && (
        <p className="text-xs leading-tight text-red-500" role="alert">{errorMessage}</p>
      )}
    </div>
  )
}

export default EmailVerification
