import { Home, Calendar, FileText, ClipboardList, Settings, Tag, Users, UserCircle } from 'lucide-react'
import type { ElementType } from 'react'

export interface NavChild {
  label: string
  path: string
  icon: ElementType
}

export interface NavItem {
  label: string
  path: string
  icon: ElementType
  children?: NavChild[]
}

export const NAV_ITEMS: NavItem[] = [
  { label: '홈', path: '/', icon: Home },
  { label: '일정관리', path: '/schedule', icon: Calendar },
  { label: '견적서관리', path: '/quote', icon: FileText },
  { label: '견적서(시트)', path: '/quote-sheets', icon: FileText },
  { label: '시공내역', path: '/construction', icon: ClipboardList },
  {
    label: '설정',
    path: '/settings',
    icon: Settings,
    children: [
      { label: '카테고리 관리', path: '/settings/categories', icon: Tag },
      { label: '인력 관리', path: '/settings/workers', icon: Users },
      { label: '프로필 관리', path: '/settings/profile', icon: UserCircle },
    ],
  },
]
