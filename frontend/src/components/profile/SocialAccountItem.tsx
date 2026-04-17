import { cn } from '@/lib/utils'
import { Button } from '@/components/ui/button'

type Provider = 'GOOGLE' | 'NAVER' | 'KAKAO'

interface SocialAccountItemProps {
  provider: Provider
  linkedAccount: { email: string | null } | null
}

const PROVIDER_META: Record<Provider, { label: string; bgColor: string; textColor: string; logoChar: string }> = {
  GOOGLE: { label: 'Google', bgColor: 'bg-white border border-slate-200', textColor: 'text-slate-700', logoChar: 'G' },
  NAVER:  { label: '네이버', bgColor: 'bg-[#03C75A]',                   textColor: 'text-white',          logoChar: 'N' },
  KAKAO:  { label: '카카오', bgColor: 'bg-[#FEE500]',                   textColor: 'text-[#3C1E1E]',      logoChar: 'K' },
}

const SocialAccountItem = ({ provider, linkedAccount }: SocialAccountItemProps) => {
  const meta = PROVIDER_META[provider]

  const handleAdd = () => {
    sessionStorage.setItem('oauth2RedirectUri', '/settings/profile')
    window.location.href = `/oauth2/authorization/${provider.toLowerCase()}`
  }

  return (
    <div className="flex items-center justify-between gap-2 border-b border-slate-100 py-1.5 last:border-0">
      <div className="flex items-center gap-1.5">
        <span
          className={cn(
            'flex h-5 w-5 shrink-0 items-center justify-center rounded text-xs font-bold leading-none',
            meta.bgColor,
            meta.textColor,
          )}
          aria-hidden="true"
        >
          {meta.logoChar}
        </span>
        <div>
          <p className="text-sm font-medium leading-tight text-slate-800">{meta.label}</p>
          <p className="text-xs leading-tight text-slate-400">
            {linkedAccount ? (linkedAccount.email ?? '연동됨') : '미연동'}
          </p>
        </div>
      </div>

      {!linkedAccount && (
        <Button
          type="button"
          variant="outline"
          onClick={handleAdd}
          className="rounded border-slate-300 px-2 py-0.5 text-xs leading-tight h-auto"
          aria-label={`${meta.label} 계정 연동 추가`}
        >
          추가
        </Button>
      )}
    </div>
  )
}

export default SocialAccountItem
