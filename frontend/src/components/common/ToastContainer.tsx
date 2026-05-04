import { cn } from "@/lib/utils"
import type { Toast } from "@/hooks/useToast"
import { X } from "lucide-react"

interface ToastContainerProps {
  toasts: Toast[]
  onRemove: (id: number) => void
}

const variantClass: Record<Toast["type"], string> = {
  success: "bg-green-600 text-white",
  error: "bg-red-600 text-white",
  info: "bg-slate-800 text-white",
}

const ToastContainer = ({ toasts, onRemove }: ToastContainerProps) => {
  if (toasts.length === 0) return null

  return (
    <div
      className="fixed bottom-20 left-1/2 -translate-x-1/2 z-[100] flex flex-col gap-2 w-full max-w-sm px-4 md:bottom-6"
      aria-live="polite"
      aria-atomic="false"
    >
      {toasts.map((toast) => (
        <div
          key={toast.id}
          role="alert"
          className={cn(
            "flex items-center justify-between gap-3 rounded-lg px-4 py-3 shadow-lg text-sm font-medium",
            variantClass[toast.type]
          )}
        >
          <span>{toast.message}</span>
          <button
            type="button"
            onClick={() => onRemove(toast.id)}
            className="shrink-0 opacity-80 hover:opacity-100 focus:outline-none focus:ring-2 focus:ring-white/50 rounded"
            aria-label="토스트 닫기"
          >
            <X className="h-4 w-4" />
          </button>
        </div>
      ))}
    </div>
  )
}

export default ToastContainer
