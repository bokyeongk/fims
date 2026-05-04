import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import { Trash2 } from "lucide-react"
import { cn } from "@/lib/utils"

export interface QuoteItemRow {
  itemName: string
  spec: string
  category: string
  quantity: number
  unit: string
  unitPrice: number
}

interface QuoteItemTableProps {
  items: QuoteItemRow[]
  readOnly?: boolean
  onChange?: (items: QuoteItemRow[]) => void
}

const formatNumber = (n: number) => n.toLocaleString("ko-KR")

const thClass = "text-xs font-semibold text-slate-600 px-2 py-1.5 text-center bg-slate-50 border-b border-slate-200 whitespace-nowrap"
const tdClass = "px-1.5 py-1 text-sm text-slate-700 text-center"

const QuoteItemTable = ({ items, readOnly = false, onChange }: QuoteItemTableProps) => {
  const handleChange = <K extends keyof QuoteItemRow>(
    index: number,
    field: K,
    value: QuoteItemRow[K]
  ) => {
    if (!onChange) return
    const updated = items.map((item, i) =>
      i === index ? { ...item, [field]: value } : item
    )
    onChange(updated)
  }

  const handleAddRow = () => {
    if (!onChange) return
    onChange([
      ...items,
      { itemName: "", spec: "", category: "", quantity: 1, unit: "EA", unitPrice: 0 },
    ])
  }

  const handleRemoveRow = (index: number) => {
    if (!onChange) return
    onChange(items.filter((_, i) => i !== index))
  }

  if (readOnly) {
    return (
      <div className="overflow-x-auto rounded border border-slate-200">
        <table className="min-w-full text-sm border-collapse">
          <thead>
            <tr>
              <th className={thClass}>번호</th>
              <th className={thClass}>품명</th>
              <th className={thClass}>규격</th>
              <th className={thClass}>구분</th>
              <th className={thClass}>수량</th>
              <th className={thClass}>단위</th>
              <th className={thClass}>단가</th>
              <th className={thClass}>금액</th>
            </tr>
          </thead>
          <tbody>
            {items.length === 0 ? (
              <tr>
                <td colSpan={8} className="py-6 text-center text-xs text-slate-400">
                  품목이 없습니다.
                </td>
              </tr>
            ) : (
              items.map((item, i) => (
                <tr key={i} className={cn(i % 2 === 0 ? "bg-white" : "bg-slate-50")}>
                  <td className={tdClass}>{i + 1}</td>
                  <td className={tdClass}>{item.itemName}</td>
                  <td className={tdClass}>{item.spec}</td>
                  <td className={tdClass}>{item.category}</td>
                  <td className={tdClass}>{item.quantity}</td>
                  <td className={tdClass}>{item.unit}</td>
                  <td className={cn(tdClass, "text-right")}>{formatNumber(item.unitPrice)}</td>
                  <td className={cn(tdClass, "text-right font-medium")}>
                    {formatNumber(item.quantity * item.unitPrice)}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    )
  }

  return (
    <div className="space-y-2">
      <div className="overflow-x-auto rounded border border-slate-200">
        <table className="min-w-full text-sm border-collapse">
          <thead>
            <tr>
              <th className={thClass}>품명</th>
              <th className={thClass}>규격</th>
              <th className={thClass}>구분</th>
              <th className={thClass}>수량</th>
              <th className={thClass}>단위</th>
              <th className={thClass}>단가</th>
              <th className={thClass}>금액</th>
              <th className={thClass}></th>
            </tr>
          </thead>
          <tbody>
            {items.length === 0 ? (
              <tr>
                <td colSpan={8} className="py-6 text-center text-xs text-slate-400">
                  아래 버튼으로 품목을 추가하세요.
                </td>
              </tr>
            ) : (
              items.map((item, i) => (
                <tr key={i} className="border-b border-slate-100 last:border-0">
                  <td className="px-1 py-1">
                    <Input
                      value={item.itemName}
                      onChange={(e) => handleChange(i, "itemName", e.target.value)}
                      placeholder="품명"
                      className="h-8 min-w-[80px] text-xs px-1.5"
                      aria-label={`${i + 1}번 품명`}
                    />
                  </td>
                  <td className="px-1 py-1">
                    <Input
                      value={item.spec}
                      onChange={(e) => handleChange(i, "spec", e.target.value)}
                      placeholder="규격"
                      className="h-8 min-w-[80px] text-xs px-1.5"
                      aria-label={`${i + 1}번 규격`}
                    />
                  </td>
                  <td className="px-1 py-1">
                    <Input
                      value={item.category}
                      onChange={(e) => handleChange(i, "category", e.target.value)}
                      placeholder="구분"
                      className="h-8 min-w-[70px] text-xs px-1.5"
                      aria-label={`${i + 1}번 구분`}
                    />
                  </td>
                  <td className="px-1 py-1">
                    <Input
                      type="number"
                      value={item.quantity}
                      min={0}
                      onChange={(e) =>
                        handleChange(i, "quantity", Number(e.target.value))
                      }
                      className="h-8 w-16 text-xs px-1.5 text-right"
                      aria-label={`${i + 1}번 수량`}
                    />
                  </td>
                  <td className="px-1 py-1">
                    <Input
                      value={item.unit}
                      onChange={(e) => handleChange(i, "unit", e.target.value)}
                      placeholder="단위"
                      className="h-8 w-14 text-xs px-1.5"
                      aria-label={`${i + 1}번 단위`}
                    />
                  </td>
                  <td className="px-1 py-1">
                    <Input
                      type="number"
                      value={item.unitPrice}
                      min={0}
                      onChange={(e) =>
                        handleChange(i, "unitPrice", Number(e.target.value))
                      }
                      className="h-8 w-24 text-xs px-1.5 text-right"
                      aria-label={`${i + 1}번 단가`}
                    />
                  </td>
                  <td className="px-2 py-1 text-right text-xs font-medium text-slate-700 whitespace-nowrap">
                    {formatNumber(item.quantity * item.unitPrice)}원
                  </td>
                  <td className="px-1 py-1 text-center">
                    <Button
                      type="button"
                      variant="ghost"
                      size="icon"
                      className="h-8 w-8 text-slate-400 hover:text-red-500"
                      onClick={() => handleRemoveRow(i)}
                      aria-label={`${i + 1}번 품목 삭제`}
                    >
                      <Trash2 className="h-3.5 w-3.5" />
                    </Button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
      <Button
        type="button"
        variant="outline"
        size="sm"
        onClick={handleAddRow}
        className="w-full text-xs border-dashed border-slate-300 text-slate-500 hover:text-slate-700 h-8"
      >
        + 행 추가
      </Button>
    </div>
  )
}

export default QuoteItemTable
