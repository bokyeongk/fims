import { BrowserRouter, Routes, Route } from 'react-router-dom'
import AppLayout from '@/components/common/AppLayout'
import HomePage from '@/pages/HomePage'
import SchedulePage from '@/pages/SchedulePage'
import QuotePage from '@/pages/QuotePage'
import ConstructionPage from '@/pages/ConstructionPage'
import WorkersPage from '@/pages/WorkersPage'

const QuoteDetailPage = () => (
  <div className="flex items-center justify-center h-full p-6">
    <p className="text-slate-500">견적서 상세 (준비 중)</p>
  </div>
)

const App = () => {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<AppLayout />}>
          <Route index element={<HomePage />} />
          <Route path="/schedule" element={<SchedulePage />} />
          <Route path="/quote" element={<QuotePage />} />
          <Route path="/quote/:id" element={<QuoteDetailPage />} />
          <Route path="/construction" element={<ConstructionPage />} />
          <Route path="/workers" element={<WorkersPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default App
