import { BrowserRouter, Routes, Route } from 'react-router-dom'
import AppLayout from '@/components/common/AppLayout'
import ProtectedRoute from '@/components/common/ProtectedRoute'
import HomePage from '@/pages/HomePage'
import SchedulePage from '@/pages/SchedulePage'
import QuotePage from '@/pages/QuotePage'
import QuoteSheetPage from '@/pages/QuoteSheetPage'
import ConstructionPage from '@/pages/ConstructionPage'
import WorkersPage from '@/pages/WorkersPage'
import SettingsProfilePage from '@/pages/SettingsProfilePage'
import LoginPage from '@/pages/LoginPage'
import OAuth2CallbackPage from '@/pages/OAuth2CallbackPage'

const QuoteDetailPage = () => (
  <div className="flex items-center justify-center h-full p-6">
    <p className="text-slate-500">견적서 상세 (준비 중)</p>
  </div>
)

const App = () => {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/oauth2/callback" element={<OAuth2CallbackPage />} />
        <Route element={<ProtectedRoute />}>
          <Route element={<AppLayout />}>
            <Route index element={<HomePage />} />
            <Route path="/schedule" element={<SchedulePage />} />
            <Route path="/quote" element={<QuotePage />} />
            <Route path="/quote/:id" element={<QuoteDetailPage />} />
            <Route path="/quote-sheets" element={<QuoteSheetPage />} />
            <Route path="/construction" element={<ConstructionPage />} />
            <Route path="/workers" element={<WorkersPage />} />
            <Route path="/settings/profile" element={<SettingsProfilePage />} />
          </Route>
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default App
