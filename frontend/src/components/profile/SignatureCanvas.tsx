import { useRef, useState, useEffect, useCallback } from 'react'
import { cn } from '@/lib/utils'
import { Button } from '@/components/ui/button'

const MAX_SIGNATURE_BYTES = 279_552

interface SignatureCanvasProps {
  existingSignature: string | null
  onSave: (data: string) => void
  onDelete: () => void
}

const SignatureCanvas = ({ existingSignature, onSave, onDelete }: SignatureCanvasProps) => {
  const canvasRef = useRef<HTMLCanvasElement>(null)
  const [isDrawing, setIsDrawing] = useState(false)
  const [isEditMode, setIsEditMode] = useState(!existingSignature)
  const [isEmpty, setIsEmpty] = useState(true)

  useEffect(() => {
    if (existingSignature) setIsEditMode(false)
  }, [existingSignature])

  const getCanvas = useCallback(() => canvasRef.current, [])

  const getContext = useCallback(() => {
    const canvas = getCanvas()
    if (!canvas) return null
    const ctx = canvas.getContext('2d')
    if (!ctx) return null
    ctx.strokeStyle = '#1e293b'
    ctx.lineWidth = 1.5
    ctx.lineCap = 'round'
    ctx.lineJoin = 'round'
    return ctx
  }, [getCanvas])

  const getRelativePos = useCallback(
    (clientX: number, clientY: number, canvas: HTMLCanvasElement) => {
      const rect = canvas.getBoundingClientRect()
      return {
        x: (clientX - rect.left) * (canvas.width / rect.width),
        y: (clientY - rect.top) * (canvas.height / rect.height),
      }
    },
    [],
  )

  const clearCanvas = useCallback(() => {
    const canvas = getCanvas(); const ctx = getContext()
    if (!canvas || !ctx) return
    ctx.clearRect(0, 0, canvas.width, canvas.height)
    setIsEmpty(true)
  }, [getCanvas, getContext])

  const handleMouseDown = useCallback((e: React.MouseEvent<HTMLCanvasElement>) => {
    const canvas = getCanvas(); const ctx = getContext()
    if (!canvas || !ctx) return
    setIsDrawing(true); setIsEmpty(false)
    const pos = getRelativePos(e.clientX, e.clientY, canvas)
    ctx.beginPath(); ctx.moveTo(pos.x, pos.y)
  }, [getCanvas, getContext, getRelativePos])

  const handleMouseMove = useCallback((e: React.MouseEvent<HTMLCanvasElement>) => {
    if (!isDrawing) return
    const canvas = getCanvas(); const ctx = getContext()
    if (!canvas || !ctx) return
    const pos = getRelativePos(e.clientX, e.clientY, canvas)
    ctx.lineTo(pos.x, pos.y); ctx.stroke()
  }, [isDrawing, getCanvas, getContext, getRelativePos])

  const handleMouseUp = useCallback(() => setIsDrawing(false), [])

  const handleTouchStart = useCallback((e: React.TouchEvent<HTMLCanvasElement>) => {
    e.preventDefault()
    const canvas = getCanvas(); const ctx = getContext()
    if (!canvas || !ctx) return
    const touch = e.touches[0]
    setIsDrawing(true); setIsEmpty(false)
    const pos = getRelativePos(touch.clientX, touch.clientY, canvas)
    ctx.beginPath(); ctx.moveTo(pos.x, pos.y)
  }, [getCanvas, getContext, getRelativePos])

  const handleTouchMove = useCallback((e: React.TouchEvent<HTMLCanvasElement>) => {
    e.preventDefault()
    if (!isDrawing) return
    const canvas = getCanvas(); const ctx = getContext()
    if (!canvas || !ctx) return
    const touch = e.touches[0]
    const pos = getRelativePos(touch.clientX, touch.clientY, canvas)
    ctx.lineTo(pos.x, pos.y); ctx.stroke()
  }, [isDrawing, getCanvas, getContext, getRelativePos])

  const handleTouchEnd = useCallback((e: React.TouchEvent<HTMLCanvasElement>) => {
    e.preventDefault(); setIsDrawing(false)
  }, [])

  const handleSave = useCallback(() => {
    const canvas = getCanvas()
    if (!canvas) return
    const dataUrl = canvas.toDataURL('image/png')
    if (new Blob([dataUrl]).size > MAX_SIGNATURE_BYTES) {
      alert('서명 크기가 너무 큽니다. 간단하게 다시 서명해 주세요. (최대 273KB)')
      return
    }
    onSave(dataUrl)
  }, [getCanvas, onSave])

  useEffect(() => { if (isEditMode) clearCanvas() }, [isEditMode, clearCanvas])

  if (!isEditMode && existingSignature) {
    return (
      <div className="space-y-1.5">
        <div className="rounded border border-slate-200 bg-slate-50 p-1.5">
          <img src={existingSignature} alt="저장된 서명" className="mx-auto max-h-16 w-full object-contain" />
        </div>
        <div className="flex gap-1">
          <Button
            type="button"
            variant="outline"
            onClick={() => { setIsEditMode(true); setIsEmpty(true) }}
            className="flex-1 rounded border-slate-300 px-3 py-1 text-sm leading-tight h-auto"
          >
            다시 서명
          </Button>
          <Button
            type="button"
            variant="outline"
            onClick={onDelete}
            className="flex-1 rounded border-red-200 px-3 py-1 text-sm leading-tight h-auto text-red-600 hover:bg-red-50 hover:text-red-600"
          >
            삭제
          </Button>
        </div>
      </div>
    )
  }

  return (
    <div className="space-y-1.5">
      <canvas
        ref={canvasRef}
        width={600}
        height={140}
        className={cn(
          'w-full touch-none rounded border-2 bg-white',
          isDrawing ? 'border-slate-400' : 'border-slate-200',
        )}
        style={{ cursor: 'crosshair' }}
        onMouseDown={handleMouseDown}
        onMouseMove={handleMouseMove}
        onMouseUp={handleMouseUp}
        onMouseLeave={handleMouseUp}
        onTouchStart={handleTouchStart}
        onTouchMove={handleTouchMove}
        onTouchEnd={handleTouchEnd}
        aria-label="서명 입력 캔버스"
        role="img"
      />
      <p className="text-center text-xs leading-tight text-slate-400">이 영역에 서명해 주세요</p>
      <div className="flex gap-1">
        <Button
          type="button"
          variant="outline"
          onClick={clearCanvas}
          className="flex-1 rounded border-slate-300 px-3 py-1 text-sm leading-tight h-auto"
        >
          지우기
        </Button>
        <Button
          type="button"
          onClick={handleSave}
          disabled={isEmpty}
          className="flex-1 rounded bg-slate-800 hover:bg-slate-700 px-3 py-1 text-sm leading-tight h-auto"
        >
          저장
        </Button>
      </div>
    </div>
  )
}

export default SignatureCanvas
