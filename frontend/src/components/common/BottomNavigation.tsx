import { useState } from 'react'
import { NavLink, useNavigate, useLocation } from 'react-router-dom'
import { X, LogOut } from 'lucide-react'
import { cn } from '@/lib/utils'
import { NAV_ITEMS, type NavItem } from '@/components/common/navItems'
import { useLogout } from '@/api/auth'

const BottomNavigation = () => {
  const [openItem, setOpenItem] = useState<NavItem | null>(null)
  const navigate = useNavigate()
  const location = useLocation()
  const { mutate: logout, isPending: isLoggingOut } = useLogout()

  const handleItemClick = (item: NavItem) => {
    if (item.children?.length) {
      setOpenItem(prev => (prev?.path === item.path ? null : item))
    } else {
      setOpenItem(null)
      navigate(item.path)
    }
  }

  const isItemActive = (item: NavItem): boolean => {
    if (item.children?.length) {
      return item.children.some(c => location.pathname.startsWith(c.path))
    }
    return item.path === '/'
      ? location.pathname === '/'
      : location.pathname.startsWith(item.path)
  }

  return (
    <>
      {/* children 오버레이 패널 */}
      {openItem?.children && (
        <>
          <div
            className="md:hidden fixed inset-0 z-40 bg-black/30"
            onClick={() => setOpenItem(null)}
            aria-hidden="true"
          />
          <div className="md:hidden fixed bottom-16 left-0 right-0 z-50 bg-white border-t border-slate-200 rounded-t-2xl shadow-lg">
            <div className="flex items-center justify-between px-5 py-3 border-b border-slate-100">
              <span className="text-sm font-semibold text-slate-700">{openItem.label}</span>
              <button
                type="button"
                onClick={() => setOpenItem(null)}
                className="text-slate-400 hover:text-slate-600"
                aria-label="닫기"
              >
                <X size={18} />
              </button>
            </div>
            <ul className="py-2">
              {openItem.children.map(child => {
                const ChildIcon = child.icon
                const isActive = location.pathname === child.path
                return (
                  <li key={child.path}>
                    <NavLink
                      to={child.path}
                      onClick={() => setOpenItem(null)}
                      className={cn(
                        'flex items-center gap-3 px-5 py-3 text-sm font-medium transition-colors',
                        isActive
                          ? 'text-blue-600 bg-blue-50'
                          : 'text-slate-600 hover:bg-slate-50'
                      )}
                    >
                      <ChildIcon size={18} strokeWidth={isActive ? 2.5 : 1.8} aria-hidden="true" />
                      <span>{child.label}</span>
                    </NavLink>
                  </li>
                )
              })}
            </ul>
          </div>
        </>
      )}

      {/* 하단 탭 바 */}
      <nav className="md:hidden fixed bottom-0 left-0 right-0 z-50 bg-white border-t border-slate-200 pb-safe">
        <ul className="flex items-center justify-around h-16">
          {NAV_ITEMS.map(item => {
            const Icon = item.icon
            const isActive = isItemActive(item)
            return (
              <li key={item.path} className="flex-1">
                <button
                  type="button"
                  onClick={() => handleItemClick(item)}
                  className={cn(
                    'flex flex-col items-center justify-center gap-0.5 w-full h-16 text-xs font-medium transition-colors',
                    isActive || openItem?.path === item.path
                      ? 'text-blue-600'
                      : 'text-slate-400 hover:text-slate-600'
                  )}
                >
                  <Icon
                    size={22}
                    strokeWidth={isActive || openItem?.path === item.path ? 2.5 : 1.8}
                    aria-hidden="true"
                  />
                  <span>{item.label}</span>
                </button>
              </li>
            )
          })}
          <li className="flex-1">
            <button
              type="button"
              onClick={() => logout()}
              disabled={isLoggingOut}
              className={cn(
                'flex flex-col items-center justify-center gap-0.5 w-full h-16 text-xs font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed',
                'text-red-400 hover:text-red-600'
              )}
            >
              <LogOut size={22} strokeWidth={1.8} aria-hidden="true" />
              <span>로그아웃</span>
            </button>
          </li>
        </ul>
      </nav>
    </>
  )
}

export default BottomNavigation
