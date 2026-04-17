import { Link } from "react-router-dom"
import { cn } from "@/lib/utils"
import type { QuoteListItem as QuoteListItemType, QuoteStatus } from "@/api/quote"

interface Props {
  item: QuoteListItemType
}

const statusConfig: Record<QuoteStatus, { label: string; className: string }> = {
  PENDING:     { label: "견적 대기", className: "bg-amber-100 text-amber-700" },
  IN_PROGRESS: { label: "시공 중",   className: "bg-blue-100 text-blue-700"   },
  COMPLETED:   { label: "시공 완료", className: "bg-green-100 text-green-700" },
  CANCELLED:   { label: "취소",      className: "bg-slate-100 text-slate-600" },
}

const QuoteListItem = ({ item }: Props) => {
  const { label, className } = statusConfig[item.status]

  return (
    <div className="bg-white rounded border border-slate-200 px-3 py-2 shadow-sm">
      <div className="flex items-center justify-between gap-2">
        <div className="flex items-center gap-2 min-w-0">
          <span
            className={cn(
              "shrink-0 inline-block text-xs font-semibold px-1.5 py-0.5 rounded",
              className
            )}
          >
            {label}
          </span>
          <div className="min-w-0">
            <p className="text-sm font-semibold leading-tight text-slate-800">
              {item.contractorName}
            </p>
            <p className="text-xs leading-tight text-slate-400 truncate">
              {item.contractDate} · {item.constructionLocation}
            </p>
          </div>
        </div>
        <Link
          to={`/quote/${item.id}`}
          className="shrink-0 text-xs font-medium text-blue-600 hover:text-blue-700 whitespace-nowrap"
          aria-label={`${item.contractorName} 견적서 상세 보기`}
        >
          상세 &gt;
        </Link>
      </div>
    </div>
  )
}

export default QuoteListItem
