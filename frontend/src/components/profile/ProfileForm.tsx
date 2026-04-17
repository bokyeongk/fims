import { useState, useEffect } from 'react'
import type { ProfileResponse } from '@/api/profile'
import { useUpdateProfile } from '@/api/profile'
import { formatPhone } from '@/utils/phone'
import { cn } from '@/lib/utils'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import EmailVerification from './EmailVerification'

interface ProfileFormProps {
  profile: ProfileResponse
  onSaved: () => void
}

const ProfileForm = ({ profile, onSaved }: ProfileFormProps) => {
  const [isEditMode, setIsEditMode] = useState(false)
  const [name, setName] = useState(profile.name)
  const [phone, setPhone] = useState(profile.phone ?? '')
  const [email, setEmail] = useState(profile.email)
  const [phoneError, setPhoneError] = useState('')
  const [verifiedEmail, setVerifiedEmail] = useState<string | null>(null)

  const updateProfile = useUpdateProfile()

  useEffect(() => {
    if (!isEditMode) {
      setName(profile.name)
      setPhone(profile.phone ?? '')
      setEmail(profile.email)
      setPhoneError('')
      setVerifiedEmail(null)
    }
  }, [profile, isEditMode])

  const isEmailChanged = email !== profile.email
  const canSave = isEmailChanged ? verifiedEmail === email : true

  const handlePhoneChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setPhone(formatPhone(e.target.value))
    setPhoneError('')
  }

  const handlePhoneBlur = () => {
    if (phone && !phone.startsWith('010')) {
      setPhoneError('010으로 시작하는 전화번호만 입력 가능합니다.')
    } else {
      setPhoneError('')
    }
  }

  const handleCancel = () => {
    setIsEditMode(false)
    setName(profile.name)
    setPhone(profile.phone ?? '')
    setEmail(profile.email)
    setPhoneError('')
    setVerifiedEmail(null)
  }

  const handleSave = async () => {
    if (phoneError) return
    try {
      await updateProfile.mutateAsync({ name, phone, email })
      setIsEditMode(false)
      onSaved()
    } catch {
      // 에러는 API 인터셉터에서 처리
    }
  }

  const readonlyClass =
    'w-full rounded border border-transparent bg-slate-50 px-2 py-1 text-sm leading-tight text-slate-700'

  const inputOverride =
    'h-auto rounded border-slate-300 px-2 py-1 text-sm leading-tight focus-visible:ring-1 focus-visible:ring-slate-500 focus-visible:ring-offset-0'

  const Row = ({ label, htmlFor, children }: { label: string; htmlFor: string; children: React.ReactNode }) => (
    <div className="flex items-start gap-2">
      <Label htmlFor={htmlFor} className="w-20 shrink-0 pt-1 text-xs leading-tight text-slate-500">
        {label}
      </Label>
      <div className="flex-1">{children}</div>
    </div>
  )

  return (
    <div className="space-y-2">
      <Row label="이름" htmlFor="profile-name">
        {isEditMode ? (
          <Input
            id="profile-name"
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            className={inputOverride}
            placeholder="이름을 입력하세요"
          />
        ) : (
          <p className={readonlyClass}>{profile.name}</p>
        )}
      </Row>

      <Row label="전화번호" htmlFor="profile-phone">
        {isEditMode ? (
          <>
            <Input
              id="profile-phone"
              type="tel"
              value={phone}
              onChange={handlePhoneChange}
              onBlur={handlePhoneBlur}
              onKeyDown={(e) => {
                const allowed = ['Backspace', 'Delete', 'Tab', 'ArrowLeft', 'ArrowRight', '-']
                if (!allowed.includes(e.key) && !/^\d$/.test(e.key)) e.preventDefault()
              }}
              className={cn(
                inputOverride,
                phoneError && 'border-red-400 focus-visible:ring-red-500',
              )}
              placeholder="010-0000-0000"
              inputMode="numeric"
            />
            {phoneError && (
              <p className="mt-0.5 text-xs leading-tight text-red-500" role="alert">{phoneError}</p>
            )}
          </>
        ) : (
          <p className={readonlyClass}>{profile.phone ?? '—'}</p>
        )}
      </Row>

      <Row label="이메일" htmlFor="profile-email">
        {isEditMode ? (
          <div className="space-y-1">
            <Input
              id="profile-email"
              type="email"
              value={email}
              onChange={(e) => { setEmail(e.target.value); setVerifiedEmail(null) }}
              className={inputOverride}
              placeholder="이메일을 입력하세요"
            />
            {isEmailChanged && (
              <EmailVerification currentEmail={email} onVerified={setVerifiedEmail} />
            )}
          </div>
        ) : (
          <p className={readonlyClass}>{profile.email}</p>
        )}
      </Row>

      <div className="flex gap-1 pt-0.5">
        {isEditMode ? (
          <>
            <Button
              type="button"
              variant="outline"
              onClick={handleCancel}
              className="flex-1 rounded border-slate-300 px-3 py-1 text-sm leading-tight h-auto"
            >
              취소
            </Button>
            <Button
              type="button"
              onClick={handleSave}
              disabled={!canSave || updateProfile.isPending}
              className="flex-1 rounded bg-slate-800 hover:bg-slate-700 px-3 py-1 text-sm leading-tight h-auto"
            >
              {updateProfile.isPending ? '저장 중...' : '저장'}
            </Button>
          </>
        ) : (
          <Button
            type="button"
            variant="outline"
            onClick={() => setIsEditMode(true)}
            className="rounded border-slate-300 px-3 py-1 text-sm leading-tight h-auto"
          >
            수정
          </Button>
        )}
      </div>
    </div>
  )
}

export default ProfileForm
