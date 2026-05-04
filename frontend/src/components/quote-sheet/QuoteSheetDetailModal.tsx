import { useState } from "react"
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
} from "@/components/ui/sheet"
import { Button } from "@/components/ui/button"
import { Loader2, ExternalLink } from "lucide-react"
import QuoteItemTable from "@/components/quote-sheet/QuoteItemTable"
import type { QuoteItemRow } from "@/components/quote-sheet/QuoteItemTable"
import { useQuoteSheetDetail, useGenerateGoogleSheet } from "@/api/quote-sheet"
import ToastContainer from "@/components/common/ToastContainer"
import { useToast } from "@/hooks/useToast"
import type { AxiosError } from "axios"

interface QuoteSheetDetailModalProps {
  id: number | null
  onClose: () => void
}

interface ApiErrorData {
  errorCode?: string
  message?: string
}

const formatDate = (dateStr: string) => dateStr?.slice(0, 10) ?? ""

const QuoteSheetDetailModal = ({ id, onClose }: QuoteSheetDetailModalProps) => {
  const open = id !== null
  const { data, isLoading } = useQuoteSheetDetail(id)
  const { mutate: generateSheet, isPending: isGenerating } = useGenerateGoogleSheet()
  const { toasts, addToast, removeToast } = useToast()
  const [showReloginBtn, setShowReloginBtn] = useState(false)

  const handleGoogleSheet = () => {
    if (!id) return
    setShowReloginBtn(false)

    generateSheet(id, {
      onSuccess: ({ sheetUrl }) => {
        if (!sheetUrl.startsWith("https://docs.google.com/")) {
          addToast("유효하지 않은 구글 시트 URL입니다.", "error")
          return
        }
        window.open(sheetUrl, "_blank", "noopener,noreferrer")
      },
      onError: (err) => {
        const axiosErr = err as AxiosError<ApiErrorData>
        const errorCode = axiosErr.response?.data?.errorCode
        const status = axiosErr.response?.status

        if (errorCode === "GOOGLE_AUTH_EXPIRED" || status === 401) {
          addToast("Google 인증이 만료되었습니다. 다시 로그인해주세요.", "error")
          setShowReloginBtn(true)
        } else if (errorCode === "GOOGLE_SCOPE_INSUFFICIENT" || status === 403) {
          addToast("Google 시트 권한이 필요합니다.", "error")
        } else {
          addToast("구글 시트 생성에 실패했습니다.", "error")
        }
      },
    })
  }

  const handleRelogin = () => {
    window.location.href = "/login"
  }

  const readOnlyItems: QuoteItemRow[] =
    data?.items.map((item) => ({
      itemName: item.itemName,
      spec: item.spec,
      category: item.category,
      quantity: item.quantity,
      unit: item.unit,
      unitPrice: item.unitPrice,
    })) ?? []

  return (
    <>
      <Sheet open={open} onOpenChange={(o) => { if (!o) onClose() }}>
        <SheetContent
          side="right"
          className="w-full sm:max-w-2xl overflow-y-auto flex flex-col p-0"
          aria-label="견적서 상세"
        >
          <SheetHeader className="px-5 pt-5 pb-3 border-b border-slate-200">
            <SheetTitle className="text-base font-bold">견적서 상세</SheetTitle>
          </SheetHeader>

          <div className="flex-1 overflow-y-auto px-5 py-4 space-y-5">
            {isLoading ? (
              <div className="flex justify-center py-12">
                <Loader2 className="h-6 w-6 animate-spin text-slate-400" />
              </div>
            ) : data ? (
              <>
                {/* 기본 정보 */}
                <div className="grid grid-cols-2 gap-x-6 gap-y-3 rounded-lg border border-slate-200 bg-slate-50 px-4 py-3">
                  <InfoRow label="견적번호" value={data.quoteNumber} />
                  <InfoRow label="계약자" value={data.contractorName} />
                  <InfoRow label="견적일자" value={formatDate(data.quoteDate)} />
                  <InfoRow label="생성일" value={formatDate(data.createdAt)} />
                </div>

                {/* 품목 */}
                <div className="space-y-1">
                  <p className="text-sm font-semibold text-slate-700">품목</p>
                  <QuoteItemTable items={readOnlyItems} readOnly />
                </div>

                {/* 총합 */}
                <div className="flex items-center justify-between rounded-lg bg-slate-50 px-4 py-3">
                  <span className="text-sm font-semibold text-slate-700">합계</span>
                  <span className="text-base font-bold text-blue-600">
                    {data.totalAmount.toLocaleString("ko-KR")}원
                  </span>
                </div>

                {/* 비고 */}
                {data.note && (
                  <div className="space-y-1">
                    <p className="text-sm font-semibold text-slate-700">비고</p>
                    <p className="text-sm text-slate-600 whitespace-pre-wrap rounded-md border border-slate-200 bg-slate-50 px-3 py-2">
                      {data.note}
                    </p>
                  </div>
                )}
              </>
            ) : (
              <p className="py-12 text-center text-sm text-slate-400">
                데이터를 불러올 수 없습니다.
              </p>
            )}
          </div>

          {/* Footer */}
          <div className="border-t border-slate-200 px-5 py-4 flex flex-col gap-2 bg-white">
            {showReloginBtn && (
              <Button
                type="button"
                variant="outline"
                onClick={handleRelogin}
                className="w-full border-red-300 text-red-600 hover:bg-red-50"
              >
                다시 로그인
              </Button>
            )}
            <Button
              type="button"
              onClick={handleGoogleSheet}
              disabled={isGenerating || isLoading || !data}
              className="w-full bg-blue-600 hover:bg-blue-700 min-h-[44px]"
            >
              {isGenerating ? (
                <>
                  <Loader2 className="h-4 w-4 animate-spin" />
                  구글 시트 생성 중...
                </>
              ) : (
                <>
                  <ExternalLink className="h-4 w-4" />
                  구글시트 이동
                </>
              )}
            </Button>
          </div>
        </SheetContent>
      </Sheet>

      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </>
  )
}

const InfoRow = ({ label, value }: { label: string; value: string }) => (
  <div>
    <p className="text-xs text-slate-400 mb-0.5">{label}</p>
    <p className="text-sm font-medium text-slate-800">{value}</p>
  </div>
)

export default QuoteSheetDetailModal
