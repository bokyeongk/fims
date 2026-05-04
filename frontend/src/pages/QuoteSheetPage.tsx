import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Loader2 } from "lucide-react"
import { useQuoteSheetList } from "@/api/quote-sheet"
import QuoteSheetListItem from "@/components/quote-sheet/QuoteSheetListItem"
import QuoteSheetCreateModal from "@/components/quote-sheet/QuoteSheetCreateModal"
import QuoteSheetDetailModal from "@/components/quote-sheet/QuoteSheetDetailModal"

const PAGE_SIZE = 20

const QuoteSheetPage = () => {
  const [page, setPage] = useState(0)
  const [selectedId, setSelectedId] = useState<number | null>(null)
  const [showCreate, setShowCreate] = useState(false)

  const { data, isLoading } = useQuoteSheetList(page, PAGE_SIZE)

  const totalPages = data?.totalPages ?? 0

  return (
    <div className="flex flex-col h-full">
      {/* Header */}
      <div className="flex items-center justify-between px-4 py-3 border-b border-slate-200 bg-white">
        <h1 className="text-base font-bold text-slate-800">견적서 목록</h1>
        <Button
          onClick={() => setShowCreate(true)}
          className="bg-blue-600 hover:bg-blue-700 text-sm px-3 py-1.5 h-auto min-h-[44px]"
          aria-label="견적서 작성"
        >
          + 견적서 작성
        </Button>
      </div>

      {/* List */}
      <div className="flex-1 overflow-y-auto px-3 py-3 space-y-2">
        {isLoading ? (
          <div className="flex justify-center py-12">
            <Loader2 className="h-6 w-6 animate-spin text-slate-400" />
          </div>
        ) : !data || data.content.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-16 gap-2">
            <p className="text-sm text-slate-400">견적서가 없습니다.</p>
            <Button
              variant="outline"
              size="sm"
              onClick={() => setShowCreate(true)}
              className="mt-2"
            >
              + 첫 견적서 작성
            </Button>
          </div>
        ) : (
          data.content.map((item) => (
            <QuoteSheetListItem
              key={item.quoteId}
              item={item}
              onSelect={setSelectedId}
            />
          ))
        )}
      </div>

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex items-center justify-center gap-2 px-4 py-3 border-t border-slate-200 bg-white">
          <Button
            variant="outline"
            size="sm"
            onClick={() => setPage((p) => Math.max(0, p - 1))}
            disabled={page === 0}
            className="h-9 px-3 text-xs"
            aria-label="이전 페이지"
          >
            이전
          </Button>
          <span className="text-xs text-slate-500 min-w-[60px] text-center">
            {page + 1} / {totalPages}
          </span>
          <Button
            variant="outline"
            size="sm"
            onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
            disabled={page >= totalPages - 1}
            className="h-9 px-3 text-xs"
            aria-label="다음 페이지"
          >
            다음
          </Button>
        </div>
      )}

      <QuoteSheetCreateModal
        open={showCreate}
        onClose={() => setShowCreate(false)}
      />

      <QuoteSheetDetailModal
        id={selectedId}
        onClose={() => setSelectedId(null)}
      />
    </div>
  )
}

export default QuoteSheetPage
