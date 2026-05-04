import { useState } from "react"
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
} from "@/components/ui/sheet"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import QuoteItemTable from "@/components/quote-sheet/QuoteItemTable"
import type { QuoteItemRow } from "@/components/quote-sheet/QuoteItemTable"
import { useCreateQuoteSheet } from "@/api/quote-sheet"

interface QuoteSheetCreateModalProps {
  open: boolean
  onClose: () => void
}

const today = () => new Date().toISOString().slice(0, 10)

const QuoteSheetCreateModal = ({ open, onClose }: QuoteSheetCreateModalProps) => {
  const [quoteDate, setQuoteDate] = useState(today())
  const [contractorName, setContractorName] = useState("")
  const [items, setItems] = useState<QuoteItemRow[]>([])
  const [note, setNote] = useState("")

  const { mutate, isPending } = useCreateQuoteSheet()

  const totalAmount = items.reduce(
    (sum, item) => sum + item.quantity * item.unitPrice,
    0
  )

  const handleClose = () => {
    setQuoteDate(today())
    setContractorName("")
    setItems([])
    setNote("")
    onClose()
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    mutate(
      {
        quoteDate,
        contractorName,
        items,
        note,
      },
      {
        onSuccess: () => {
          handleClose()
        },
      }
    )
  }

  return (
    <Sheet open={open} onOpenChange={(o) => { if (!o) handleClose() }}>
      <SheetContent
        side="right"
        className="w-full sm:max-w-2xl overflow-y-auto flex flex-col p-0"
        aria-label="견적서 작성"
      >
        <SheetHeader className="px-5 pt-5 pb-3 border-b border-slate-200">
          <SheetTitle className="text-base font-bold">견적서 작성</SheetTitle>
        </SheetHeader>

        <form
          id="create-quote-sheet-form"
          onSubmit={handleSubmit}
          className="flex-1 overflow-y-auto px-5 py-4 space-y-5"
        >
          {/* 견적번호 */}
          <div className="space-y-1">
            <Label htmlFor="quoteNumber">견적번호</Label>
            <Input
              id="quoteNumber"
              value=""
              readOnly
              disabled
              placeholder="저장 후 자동생성"
              className="bg-slate-50 text-slate-400 cursor-not-allowed"
            />
          </div>

          {/* 견적일자 */}
          <div className="space-y-1">
            <Label htmlFor="quoteDate">견적일자</Label>
            <Input
              id="quoteDate"
              type="date"
              value={quoteDate}
              onChange={(e) => setQuoteDate(e.target.value)}
              required
              aria-required="true"
            />
          </div>

          {/* 계약자명 */}
          <div className="space-y-1">
            <Label htmlFor="contractorName">계약자명</Label>
            <Input
              id="contractorName"
              value={contractorName}
              onChange={(e) => setContractorName(e.target.value)}
              placeholder="계약자명 입력"
              required
              aria-required="true"
            />
          </div>

          {/* 품목 */}
          <div className="space-y-1">
            <Label>품목</Label>
            <QuoteItemTable items={items} onChange={setItems} />
          </div>

          {/* 총합 */}
          <div className="flex items-center justify-between rounded-lg bg-slate-50 px-4 py-3">
            <span className="text-sm font-semibold text-slate-700">합계</span>
            <span className="text-base font-bold text-blue-600">
              {totalAmount.toLocaleString("ko-KR")}원
            </span>
          </div>

          {/* 비고 */}
          <div className="space-y-1">
            <Label htmlFor="note">비고</Label>
            <textarea
              id="note"
              value={note}
              onChange={(e) => setNote(e.target.value)}
              placeholder="비고 입력"
              rows={3}
              className="flex w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 resize-none"
            />
          </div>
        </form>

        {/* Footer */}
        <div className="border-t border-slate-200 px-5 py-4 flex gap-2 justify-end bg-white">
          <Button
            type="button"
            variant="outline"
            onClick={handleClose}
            disabled={isPending}
          >
            취소
          </Button>
          <Button
            type="submit"
            form="create-quote-sheet-form"
            disabled={isPending}
            className="bg-blue-600 hover:bg-blue-700 min-w-[72px]"
          >
            {isPending ? "저장 중..." : "저장"}
          </Button>
        </div>
      </SheetContent>
    </Sheet>
  )
}

export default QuoteSheetCreateModal
