import { useEffect, useState } from "react"
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
} from "@/components/ui/sheet"
import type { QuoteFilters, QuoteStatus } from "@/api/quote"

const STATUS_OPTIONS: { value: QuoteStatus; label: string }[] = [
  { value: "PENDING", label: "견적 대기" },
  { value: "IN_PROGRESS", label: "시공 중" },
  { value: "COMPLETED", label: "시공 완료" },
  { value: "CANCELLED", label: "취소" },
]

interface Props {
  filters: QuoteFilters
  onApply: (filters: QuoteFilters) => void
  open: boolean
  onOpenChange: (open: boolean) => void
}

const QuoteFilterPopup = ({ filters, onApply, open, onOpenChange }: Props) => {
  const [local, setLocal] = useState<QuoteFilters>(filters)

  useEffect(() => {
    if (open) setLocal(filters)
  }, [open, filters])

  const handleApply = () => {
    onApply(local)
    onOpenChange(false)
  }

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent side="left" className="flex flex-col w-80 max-w-full p-0">
        <SheetHeader className="px-5 py-4 border-b border-slate-200">
          <SheetTitle className="text-base font-semibold text-slate-800">
            조회 조건
          </SheetTitle>
        </SheetHeader>

        {/* Body */}
        <div className="flex-1 overflow-y-auto px-5 py-6 space-y-5">
          {/* Contract date range */}
          <fieldset>
            <legend className="text-sm font-medium text-slate-700 mb-2">
              계약기간
            </legend>
            <div className="flex flex-col gap-2">
              <div className="flex items-center gap-2">
                <label
                  htmlFor="filter-start-date"
                  className="text-xs text-slate-500 w-10 shrink-0"
                >
                  시작일
                </label>
                <input
                  id="filter-start-date"
                  type="date"
                  value={local.startDate}
                  onChange={(e) =>
                    setLocal((prev) => ({ ...prev, startDate: e.target.value }))
                  }
                  className="flex-1 border border-slate-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>
              <div className="flex items-center gap-2">
                <label
                  htmlFor="filter-end-date"
                  className="text-xs text-slate-500 w-10 shrink-0"
                >
                  종료일
                </label>
                <input
                  id="filter-end-date"
                  type="date"
                  value={local.endDate}
                  onChange={(e) =>
                    setLocal((prev) => ({ ...prev, endDate: e.target.value }))
                  }
                  className="flex-1 border border-slate-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>
            </div>
          </fieldset>

          {/* Status multi-select */}
          <fieldset>
            <legend className="text-sm font-medium text-slate-700 mb-2">
              진행상태
            </legend>
            <div className="flex flex-col gap-2">
              {STATUS_OPTIONS.map(({ value, label }) => {
                const checked = (local.statuses ?? []).includes(value)
                return (
                  <label
                    key={value}
                    className="flex items-center gap-2 cursor-pointer"
                  >
                    <input
                      type="checkbox"
                      checked={checked}
                      onChange={() => {
                        setLocal((prev) => {
                          const current = prev.statuses ?? []
                          const next = checked
                            ? current.filter((s) => s !== value)
                            : [...current, value]
                          return {
                            ...prev,
                            statuses: next.length > 0 ? next : undefined,
                          }
                        })
                      }}
                      className="w-4 h-4 rounded border-slate-300 text-blue-600 focus:ring-blue-500"
                    />
                    <span className="text-sm text-slate-700">{label}</span>
                  </label>
                )
              })}
            </div>
          </fieldset>

          {/* Contractor name */}
          <div>
            <label
              htmlFor="filter-contractor"
              className="block text-sm font-medium text-slate-700 mb-2"
            >
              계약자명
            </label>
            <input
              id="filter-contractor"
              type="text"
              placeholder="계약자명 검색"
              value={local.contractorName ?? ""}
              onChange={(e) =>
                setLocal((prev) => ({
                  ...prev,
                  contractorName: e.target.value || undefined,
                }))
              }
              className="w-full border border-slate-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
        </div>

        {/* Footer */}
        <div className="px-5 py-4 border-t border-slate-200">
          <button
            type="button"
            onClick={handleApply}
            className="w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold rounded-lg py-2.5 text-sm transition-colors"
          >
            검색
          </button>
        </div>
      </SheetContent>
    </Sheet>
  )
}

export default QuoteFilterPopup
