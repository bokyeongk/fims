import { useProfile, useSaveSignature, useDeleteSignature } from '@/api/profile'
import { Button } from '@/components/ui/button'
import ProfileForm from '@/components/profile/ProfileForm'
import SocialAccountList from '@/components/profile/SocialAccountList'
import SignatureCanvas from '@/components/profile/SignatureCanvas'

const SectionCard = ({ title, children }: { title: string; children: React.ReactNode }) => (
  <section className="rounded border border-slate-200 bg-white p-2.5 shadow-sm">
    <h2 className="mb-2 text-xs font-semibold uppercase tracking-wide text-slate-500">{title}</h2>
    {children}
  </section>
)

const SettingsProfilePage = () => {
  const { data: profile, isLoading, isError, refetch } = useProfile()
  const saveSignature = useSaveSignature()
  const deleteSignature = useDeleteSignature()

  if (isLoading) {
    return (
      <div className="flex min-h-[60vh] items-center justify-center">
        <svg
          className="h-8 w-8 animate-spin text-slate-400"
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 24 24"
          aria-hidden="true"
        >
          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
          <path
            className="opacity-75"
            fill="currentColor"
            d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"
          />
        </svg>
      </div>
    )
  }

  if (isError || !profile) {
    return (
      <div className="flex min-h-[60vh] flex-col items-center justify-center gap-3">
        <p className="text-sm text-slate-500">프로필 정보를 불러오지 못했습니다.</p>
        <Button
          type="button"
          variant="outline"
          onClick={() => refetch()}
          className="rounded border-slate-300 px-3 py-1 text-sm leading-tight h-auto"
        >
          다시 시도
        </Button>
      </div>
    )
  }

  const handleSaveSignature = async (signatureData: string) => {
    await saveSignature.mutateAsync({ signatureData })
  }

  const handleDeleteSignature = async () => {
    await deleteSignature.mutateAsync()
  }

  return (
    <div className="mx-auto max-w-2xl space-y-2 px-2 py-3">
      <h1 className="text-sm font-bold leading-tight text-slate-900">프로필 관리</h1>

      {/* 기본 정보 */}
      <SectionCard title="기본 정보">
        <ProfileForm profile={profile} onSaved={() => refetch()} />
      </SectionCard>

      {/* 소셜 계정 연동 */}
      <SectionCard title="소셜 계정 연동">
        <SocialAccountList socialAccounts={profile.socialAccounts} />
      </SectionCard>

      {/* 서명 관리 */}
      <SectionCard title="서명">
        <SignatureCanvas
          existingSignature={profile.signatureData}
          onSave={handleSaveSignature}
          onDelete={handleDeleteSignature}
        />
      </SectionCard>
    </div>
  )
}

export default SettingsProfilePage
