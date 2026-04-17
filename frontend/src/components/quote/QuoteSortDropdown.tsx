import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import type { QuoteSortType } from "@/api/quote"

interface Props {
  value: QuoteSortType
  onChange: (value: QuoteSortType) => void
}

const options: { value: QuoteSortType; label: string }[] = [
  { value: "LATEST", label: "최근 순" },
  { value: "OLDEST", label: "오래된 순" },
]

const QuoteSortDropdown = ({ value, onChange }: Props) => {
  return (
    <Select value={value} onValueChange={(v) => onChange(v as QuoteSortType)}>
      <SelectTrigger className="h-auto border border-slate-300 rounded px-2 py-1 text-sm leading-tight text-slate-700 bg-white hover:bg-slate-50 focus:ring-1 focus:ring-blue-500 w-auto min-w-[90px]">
        <SelectValue />
      </SelectTrigger>
      <SelectContent>
        {options.map((opt) => (
          <SelectItem key={opt.value} value={opt.value}>
            {opt.label}
          </SelectItem>
        ))}
      </SelectContent>
    </Select>
  )
}

export default QuoteSortDropdown
