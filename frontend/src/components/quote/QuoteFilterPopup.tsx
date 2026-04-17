import { useEffect, useState } from "react"
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
} from "@/components/ui/sheet"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Checkbox } from "@/components/ui/checkbox"
import type { QuoteFilters, QuoteStatus } from "@/api/quote"

const STATUS_OPTIONS: { value: QuoteStatus; label: string }[] = [
  { value: "PENDING",     label: "견적 대기" },
  { value: "IN_PROGRESS", label: "시공 중"   },
  { value: "COMPLETED",   label: "시공 완료" },
  { value: "CANCELLED",   label: "취소"      },
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
      <SheetContent side="left" className="flex flex-col w-72 max-w-full p-0">
        <SheetHeader className="px-3 py-2 border-b border-slate-200">
          <SheetTitle className="text-sm font-semibold leading-tight text-slate-800">
            조회 조건
          </SheetTitle>
        </SheetHeader>

        {/* Body */}
        <div className="flex-1 overflow-y-auto px-3 py-3 space-y-3">
          {/* Contract date range */}
          <fieldset>
            <legend className="text-xs font-medium text-slate-500 mb-1.5 uppercase tracking-wide">
              계약기간
            </legend>
            <div className="flex flex-col gap-1.5">
              <div className="flex items-center gap-1.5">
                <Label
                  htmlFor="filter-start-date"
                  className="text-xs text-slate-500 w-10 shrink-0 leading-tight"
                >
                  시작일
                </Label>
                <Input
                  id="filter-start-date"
                  type="date"
                  value={local.startDate}
                  onChange={(e) =>
                    setLocal((prev) => ({ ...prev, startDate: e.target.value }))
                  }
                  className="h-auto rounded border-slate-300 px-2 py-1 text-sm leading-tight focus-visible:ring-1 focus-visible:ring-blue-500 focus-visible:ring-offset-0"
                />
              </div>
              <div className="flex items-center gap-1.5">
                <Label
                  htmlFor="filter-end-date"
                  className="text-xs text-slate-500 w-10 shrink-0 leading-tight"
                >
                  종료일
                </Label>
                <Input
                  id="filter-end-date"
                  type="date"
                  value={local.endDate}
                  onChange={(e) =>
                    setLocal((prev) => ({ ...prev, endDate: e.target.value }))
                  }
                  className="h-auto rounded border-slate-300 px-2 py-1 text-sm leading-tight focus-visible:ring-1 focus-visible:ring-blue-500 focus-visible:ring-offset-0"
                />
              </div>
            </div>
          </fieldset>

          {/* Status multi-select */}
          <fieldset>
            <legend className="text-xs font-medium text-slate-500 mb-1.5 uppercase tracking-wide">
              진행상태
            </legend>
            <div className="flex flex-col gap-1">
              {STATUS_OPTIONS.map(({ value, label }) => {
                const checked = (local.statuses ?? []).includes(value)
                return (
                  <div key={value} className="flex items-center gap-1.5">
                    <Checkbox
                      id={`status-${value}`}
                      checked={checked}
                      onCheckedChange={() => {
                        setLocal((prev) => {
                          const current = prev.statuses ?? []
                          const next = checked
                            ? current.filter((s) => s !== value)
                            : [...current, value]
                          return { ...prev, statuses: next.length > 0 ? next : undefined }
                        })
                      }}
                    />
                    <Label
                      htmlFor={`status-${value}`}
                      className="text-sm leading-tight text-slate-700 cursor-pointer"
                    >
                      {label}
                    </Label>
                  </div>
                )
              })}
            </div>
          </fieldset>

          {/* Contractor name */}
          <div>
            <Label
              htmlFor="filter-contractor"
              className="block text-xs font-medium text-slate-500 mb-1.5 uppercase tracking-wide"
            >
              계약자명
            </Label>
            <Input
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
              className="h-auto rounded border-slate-300 px-2 py-1 text-sm leading-tight focus-visible:ring-1 focus-visible:ring-blue-500 focus-visible:ring-offset-0"
            />
          </div>
        </div>

        {/* Footer */}
        <div className="px-3 py-2 border-t border-slate-200">
          <Button
            type="button"
            onClick={handleApply}
            className="w-full rounded bg-blue-600 hover:bg-blue-700 px-3 py-1.5 text-sm font-semibold leading-tight h-auto"
          >
            검색
          </Button>
        </div>
      </SheetContent>
    </Sheet>
  )
}

export default QuoteFilterPopup
