import { useInfiniteQuery } from "@tanstack/react-query"
import apiClient from "@/api/client"

export type QuoteStatus = "PENDING" | "IN_PROGRESS" | "COMPLETED" | "CANCELLED"
export type QuoteSortType = "LATEST" | "OLDEST"

export interface QuoteListItem {
  id: number
  status: QuoteStatus
  contractDate: string
  contractorName: string
  constructionLocation: string
}

export interface QuoteFilters {
  startDate: string
  endDate: string
  contractorName?: string
  statuses?: QuoteStatus[]
  sort: QuoteSortType
}

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  hasNext: boolean
}

interface ApiResponse<T> {
  success: boolean
  data: T
}

const getDefaultStartDate = (): string => {
  const d = new Date()
  d.setDate(1)
  d.setMonth(d.getMonth() - 1)
  return d.toISOString().slice(0, 10)
}

const getDefaultEndDate = (): string => {
  return new Date().toISOString().slice(0, 10)
}

export const defaultFilters: QuoteFilters = {
  startDate: getDefaultStartDate(),
  endDate: getDefaultEndDate(),
  statuses: ["PENDING", "IN_PROGRESS", "COMPLETED"],
  sort: "LATEST",
}

const fetchQuotes = async (
  filters: QuoteFilters,
  page: number
): Promise<PageResponse<QuoteListItem>> => {
  const params: Record<string, string | number | string[]> = {
    startDate: filters.startDate,
    endDate: filters.endDate,
    sort: filters.sort,
    page,
    size: 10,
  }
  if (filters.contractorName) {
    params.contractorName = filters.contractorName
  }
  if (filters.statuses && filters.statuses.length > 0) {
    params.statuses = filters.statuses
  }
  const res = await apiClient.get<ApiResponse<PageResponse<QuoteListItem>>>(
    "/v1/quotes",
    { params, paramsSerializer: { indexes: null } }
  )
  return res.data.data
}

export const useQuoteList = (filters: QuoteFilters) => {
  return useInfiniteQuery<PageResponse<QuoteListItem>, Error>({
    queryKey: ["quotes", filters],
    queryFn: ({ pageParam }) => fetchQuotes(filters, pageParam as number),
    initialPageParam: 0,
    getNextPageParam: (lastPage) =>
      lastPage.hasNext ? lastPage.page + 1 : undefined,
  })
}
