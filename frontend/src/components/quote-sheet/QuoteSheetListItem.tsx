import { FileSpreadsheet } from "lucide-react"
import { cn } from "@/lib/utils"
import type { QuoteSheetSummary } from "@/api/quote-sheet"

interface QuoteSheetListItemProps {
  item: QuoteSheetSummary
  onSelect: (quoteId: number) => void
}

const QuoteSheetListItem = ({ item, onSelect }: QuoteSheetListItemProps) => {
  return (
    <button
      type="button"
      onClick={() => onSelect(item.quoteId)}
      className={cn(
        "w-full text-left bg-white rounded border border-slate-200 px-3 py-2.5 shadow-sm",
        "hover:border-blue-300 hover:bg-blue-50/40 transition-colors",
        "focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-500 focus-visible:ring-offset-1",
        "min-h-[44px]"
      )}
      aria-label={`${item.contractorName} 견적서 상세 보기`}
    >
      <div className="flex items-center justify-between gap-3">
        <div className="min-w-0 flex-1">
          <div className="flex items-center gap-1.5 mb-0.5">
            <span className="text-xs text-slate-400 font-mono">{item.quoteNumber}</span>
            {item.hasGoogleSheet && (
              <span
                className="inline-flex items-center gap-0.5 text-xs font-medium text-green-700 bg-green-100 px-1.5 py-0.5 rounded"
                title="구글 시트 연결됨"
                aria-label="구글 시트 연결됨"
              >
                <FileSpreadsheet className="h-3 w-3" />
                시트
              </span>
            )}
          </div>
          <p className="text-sm font-semibold text-slate-800 leading-tight">
            {item.contractorName}
          </p>
          <p className="text-xs text-slate-400 leading-tight mt-0.5">
            {item.quoteDate}
          </p>
        </div>
        <div className="shrink-0 text-right">
          <p className="text-sm font-bold text-blue-600">
            {item.totalAmount.toLocaleString("ko-KR")}원
          </p>
        </div>
      </div>
    </button>
  )
}

export default QuoteSheetListItem
