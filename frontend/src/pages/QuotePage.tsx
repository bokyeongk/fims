import { useCallback, useEffect, useRef, useState } from "react"
import { defaultFilters, useQuoteList } from "@/api/quote"
import type { QuoteFilters } from "@/api/quote"
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

  const handleApplyFilters = (newFilters: QuoteFilters) => {
    setFilters(newFilters)
  }

  return (
    <div className="flex flex-col h-full">
      {/* Header */}
      <div className="flex items-center justify-between px-4 py-3 border-b border-slate-200 bg-white">
        <button
          type="button"
          onClick={() => setFilterOpen(true)}
          className="flex items-center gap-1.5 text-sm font-medium text-slate-700 border border-slate-300 rounded-lg px-3 py-1.5 hover:bg-slate-50"
          aria-label="조회 조건 열기"
        >
          <svg
            className="w-4 h-4"
            fill="none"
            stroke="currentColor"
            strokeWidth={2}
            viewBox="0 0 24 24"
            aria-hidden="true"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              d="M3 4h18M7 12h10M11 20h2"
            />
          </svg>
          조회 조건
        </button>

        <button
          type="button"
          className="flex items-center gap-1 text-sm font-semibold text-white bg-blue-600 hover:bg-blue-700 rounded-lg px-3 py-1.5 transition-colors"
          aria-label="견적서 추가"
        >
          + 견적서 추가
        </button>
      </div>

      {/* Sort row */}
      <div className="flex items-center px-4 py-2 bg-white border-b border-slate-100">
        <QuoteSortDropdown
          value={filters.sort}
          onChange={(sort) => setFilters((prev) => ({ ...prev, sort }))}
        />
      </div>

      {/* List */}
      <div className="flex-1 overflow-y-auto px-4 py-3 space-y-3">
        {isLoading ? (
          <div className="flex justify-center py-20">
            <span className="text-sm text-slate-400">불러오는 중...</span>
          </div>
        ) : allItems.length === 0 ? (
          <QuoteEmptyState />
        ) : (
          allItems.map((item) => <QuoteListItem key={item.id} item={item} />)
        )}

        {/* Infinite scroll sentinel */}
        <div ref={sentinelRef} className="h-1" aria-hidden="true" />

        {isFetchingNextPage && (
          <div className="flex justify-center py-4">
            <span className="text-sm text-slate-400">불러오는 중...</span>
          </div>
        )}
      </div>

      {/* Filter popup */}
      <QuoteFilterPopup
        filters={filters}
        onApply={handleApplyFilters}
        open={filterOpen}
        onOpenChange={setFilterOpen}
      />
    </div>
  )
}

export default QuotePage
