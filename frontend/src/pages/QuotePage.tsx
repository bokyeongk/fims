import { useCallback, useEffect, useRef, useState } from "react"
import { defaultFilters, useQuoteList } from "@/api/quote"
import type { QuoteFilters } from "@/api/quote"
import { Button } from "@/components/ui/button"
import QuoteFilterPopup from "@/components/quote/QuoteFilterPopup"
import QuoteSortDropdown from "@/components/quote/QuoteSortDropdown"
import QuoteListItem from "@/components/quote/QuoteListItem"
import QuoteEmptyState from "@/components/quote/QuoteEmptyState"

const QuotePage = () => {
  const [filters, setFilters] = useState<QuoteFilters>(defaultFilters)
  const [filterOpen, setFilterOpen] = useState(false)

  const { data, fetchNextPage, hasNextPage, isFetchingNextPage, isLoading } =
    useQuoteList(filters)

  const sentinelRef = useRef<HTMLDivElement>(null)

  const handleObserver = useCallback(
    (entries: IntersectionObserverEntry[]) => {
      const [entry] = entries
      if (entry.isIntersecting && hasNextPage && !isFetchingNextPage) {
        fetchNextPage()
      }
    },
    [fetchNextPage, hasNextPage, isFetchingNextPage]
  )

  useEffect(() => {
    const el = sentinelRef.current
    if (!el) return
    const observer = new IntersectionObserver(handleObserver, { threshold: 0.1 })
    observer.observe(el)
    return () => observer.disconnect()
  }, [handleObserver])

  const allItems = data?.pages.flatMap((page) => page.content) ?? []

  return (
    <div className="flex flex-col h-full">
      {/* Header */}
      <div className="flex items-center justify-between px-2 py-1.5 border-b border-slate-200 bg-white">
        <Button
          variant="outline"
          onClick={() => setFilterOpen(true)}
          className="flex items-center gap-1 rounded border-slate-300 px-2 py-1 text-sm leading-tight h-auto"
          aria-label="조회 조건 열기"
        >
          <svg
            className="w-3.5 h-3.5"
            fill="none"
            stroke="currentColor"
            strokeWidth={2}
            viewBox="0 0 24 24"
            aria-hidden="true"
          >
            <path strokeLinecap="round" strokeLinejoin="round" d="M3 4h18M7 12h10M11 20h2" />
          </svg>
          조회 조건
        </Button>

        <Button
          className="rounded bg-blue-600 hover:bg-blue-700 px-2 py-1 text-sm font-semibold leading-tight h-auto"
          aria-label="견적서 추가"
        >
          + 견적서 추가
        </Button>
      </div>

      {/* Sort row */}
      <div className="flex items-center px-2 py-1 bg-white border-b border-slate-100">
        <QuoteSortDropdown
          value={filters.sort}
          onChange={(sort) => setFilters((prev) => ({ ...prev, sort }))}
        />
      </div>

      {/* List */}
      <div className="flex-1 overflow-y-auto px-2 py-1.5 space-y-1.5">
        {isLoading ? (
          <div className="flex justify-center py-10">
            <span className="text-sm text-slate-400">불러오는 중...</span>
          </div>
        ) : allItems.length === 0 ? (
          <QuoteEmptyState />
        ) : (
          allItems.map((item) => <QuoteListItem key={item.id} item={item} />)
        )}

        <div ref={sentinelRef} className="h-1" aria-hidden="true" />

        {isFetchingNextPage && (
          <div className="flex justify-center py-2">
            <span className="text-sm text-slate-400">불러오는 중...</span>
          </div>
        )}
      </div>

      <QuoteFilterPopup
        filters={filters}
        onApply={setFilters}
        open={filterOpen}
        onOpenChange={setFilterOpen}
      />
    </div>
  )
}

export default QuotePage
