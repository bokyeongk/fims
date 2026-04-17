import type { ProfileResponse } from '@/api/profile'
import SocialAccountItem from './SocialAccountItem'

type Provider = 'GOOGLE' | 'NAVER' | 'KAKAO'

const PROVIDERS: Provider[] = ['GOOGLE', 'NAVER', 'KAKAO']

interface SocialAccountListProps {
  socialAccounts: ProfileResponse['socialAccounts']
}

const SocialAccountList = ({ socialAccounts }: SocialAccountListProps) => {
  const getLinkedAccount = (provider: Provider) => {
    const found = socialAccounts?.find((a) => a.provider === provider)
    return found ? { email: found.email } : null
  }

  return (
    <div>
      {PROVIDERS.map((provider) => (
        <SocialAccountItem
          key={provider}
          provider={provider}
          linkedAccount={getLinkedAccount(provider)}
        />
      ))}
    </div>
  )
}

export default SocialAccountList
