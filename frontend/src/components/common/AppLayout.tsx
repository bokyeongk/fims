import { Outlet } from 'react-router-dom'
import BottomNavigation from '@/components/common/BottomNavigation'
import Sidebar from '@/components/common/Sidebar'

const AppLayout = () => {
  return (
    <div className="flex min-h-screen bg-slate-50">
      {/* Desktop: Left sidebar */}
      <Sidebar />

      {/* Content area */}
      <div className="flex flex-col flex-1 md:ml-60">
        <main className="flex-1 pb-16 md:pb-0">
          <Outlet />
        </main>

        {/* Mobile: Bottom navigation */}
        <BottomNavigation />
      </div>
    </div>
  )
}

export default AppLayout
