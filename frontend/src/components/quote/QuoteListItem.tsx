import { Link } from "react-router-dom"
import { cn } from "@/lib/utils"
import type { QuoteListItem as QuoteListItemType, QuoteStatus } from "@/api/quote"

interface Props {
  item: QuoteListItemType
}

const statusConfig: Record<
  QuoteStatus,
  { label: string; className: string }
> = {
  PENDING: {
    label: "견적 대기",
    className: "bg-amber-100 text-amber-700",
  },
  IN_PROGRESS: {
    label: "시공 중",
    className: "bg-blue-100 text-blue-700",
  },
  COMPLETED: {
    label: "시공 완료",
    className: "bg-green-100 text-green-700",
  },
  CANCELLED: {
    label: "취소",
    className: "bg-slate-100 text-slate-600",
  },
}

const QuoteListItem = ({ item }: Props) => {
  const { label, className } = statusConfig[item.status]

  return (
    <div className="bg-white rounded-xl border border-slate-200 p-4 shadow-sm">
      <div className="flex items-start justify-between gap-2">
        <div className="flex flex-col gap-1 min-w-0">
          <span
            className={cn(
              "inline-block self-start text-xs font-semibold px-2 py-0.5 rounded-full",
              className
            )}
          >
            {label}
          </span>
          <p className="text-sm text-slate-500 mt-1">{item.contractDate}</p>
          <p className="text-base font-semibold text-slate-800">
            {item.contractorName}
          </p>
          <p className="text-sm text-slate-500 truncate">
            {item.constructionLocation}
          </p>
        </div>
        <Link
          to={`/quote/${item.id}`}
          className="flex-shrink-0 mt-1 text-sm font-medium text-blue-600 hover:text-blue-700 whitespace-nowrap"
          aria-label={`${item.contractorName} 견적서 상세 보기`}
        >
          상세 &gt;
        </Link>
      </div>
    </div>
  )
}

export default QuoteListItem
