import { useState } from 'react'
import { NavLink, useLocation } from 'react-router-dom'
import { ChevronDown, LogOut } from 'lucide-react'
import { cn } from '@/lib/utils'
import { NAV_ITEMS, type NavItem } from '@/components/common/navItems'
import { useLogout } from '@/api/auth'

const Sidebar = () => {
  const location = useLocation()
  const { mutate: logout, isPending: isLoggingOut } = useLogout()

  const initialOpen = NAV_ITEMS.reduce<Record<string, boolean>>((acc, item) => {
    if (item.children) {
      acc[item.path] = item.children.some(c => location.pathname.startsWith(c.path))
    }
    return acc
  }, {})

  const [openGroups, setOpenGroups] = useState<Record<string, boolean>>(initialOpen)

  const toggleGroup = (path: string) => {
    setOpenGroups(prev => ({ ...prev, [path]: !prev[path] }))
  }

  const renderItem = (item: NavItem) => {
    const Icon = item.icon
    const hasChildren = Boolean(item.children?.length)
    const isGroupOpen = openGroups[item.path] ?? false
    const isChildActive = item.children?.some(c => location.pathname.startsWith(c.path)) ?? false

    if (hasChildren) {
      return (
        <li key={item.path}>
          <button
            type="button"
            onClick={() => toggleGroup(item.path)}
            className={cn(
              'w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors',
              isChildActive
                ? 'bg-blue-50 text-blue-600'
                : 'text-slate-500 hover:bg-slate-100 hover:text-slate-700'
            )}
          >
            <Icon size={20} strokeWidth={isChildActive ? 2.5 : 1.8} aria-hidden="true" />
            <span className="flex-1 text-left">{item.label}</span>
            <ChevronDown
              size={16}
              className={cn('transition-transform duration-200', isGroupOpen ? 'rotate-180' : '')}
              aria-hidden="true"
            />
          </button>
          {isGroupOpen && (
            <ul className="mt-1 ml-4 flex flex-col gap-1 border-l border-slate-200 pl-3">
              {item.children!.map(child => {
                const ChildIcon = child.icon
                return (
                  <li key={child.path}>
                    <NavLink
                      to={child.path}
                      className={({ isActive }) =>
                        cn(
                          'flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors',
                          isActive
                            ? 'text-blue-600 font-semibold'
                            : 'text-slate-500 hover:bg-slate-100 hover:text-slate-700'
                        )
                      }
                    >
                      {({ isActive }) => (
                        <>
                          <ChildIcon size={16} strokeWidth={isActive ? 2.5 : 1.8} aria-hidden="true" />
                          <span>{child.label}</span>
                        </>
                      )}
                    </NavLink>
                  </li>
                )
              })}
            </ul>
          )}
        </li>
      )
    }

    return (
      <li key={item.path}>
        <NavLink
          to={item.path}
          end={item.path === '/'}
          className={({ isActive }) =>
            cn(
              'flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors',
              isActive
                ? 'bg-blue-50 text-blue-600 border-l-4 border-blue-600 pl-2'
                : 'text-slate-500 hover:bg-slate-100 hover:text-slate-700'
            )
          }
        >
          {({ isActive }) => (
            <>
              <Icon size={20} strokeWidth={isActive ? 2.5 : 1.8} aria-hidden="true" />
              <span>{item.label}</span>
            </>
          )}
        </NavLink>
      </li>
    )
  }

  return (
    <aside className="hidden md:flex flex-col w-60 min-h-screen bg-white border-r border-slate-200 fixed top-0 left-0 z-40">
      <div className="flex items-center h-16 px-6 border-b border-slate-200">
        <span className="text-xl font-bold text-blue-600 tracking-wide">FIMS</span>
      </div>
      <nav className="flex-1 py-4 overflow-y-auto">
        <ul className="flex flex-col gap-1 px-3">
          {NAV_ITEMS.map(renderItem)}
        </ul>
      </nav>
      <div className="border-t border-slate-200 px-3 py-4">
        <button
          type="button"
          onClick={() => logout()}
          disabled={isLoggingOut}
          className="w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium text-red-500 hover:bg-red-50 hover:text-red-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <LogOut size={20} aria-hidden="true" />
          <span>로그아웃</span>
        </button>
      </div>
    </aside>
  )
}

export default Sidebar
