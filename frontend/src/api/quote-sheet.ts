import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import apiClient from "@/api/client"

// ─── Types ────────────────────────────────────────────────────────────────────

export interface QuoteItemRequest {
  itemName: string
  spec: string
  category: string
  quantity: number
  unit: string
  unitPrice: number
}

export interface CreateQuoteSheetRequest {
  quoteDate: string
  contractorName: string
  items: QuoteItemRequest[]
  note: string
}

export interface QuoteSheetSummary {
  quoteId: number
  quoteNumber: string
  contractorName: string
  quoteDate: string
  totalAmount: number
  hasGoogleSheet: boolean
  createdAt: string
}

export interface QuoteItemDetail {
  itemName: string
  spec: string
  category: string
  quantity: number
  unit: string
  unitPrice: number
  amount: number
}

export interface QuoteSheetDetail {
  quoteId: number
  quoteNumber: string
  contractorName: string
  quoteDate: string
  totalAmount: number
  note: string
  sheetUrl: string | null
  createdAt: string
  items: QuoteItemDetail[]
}

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

interface ApiResponse<T> {
  success: boolean
  data: T
}

// ─── API functions ─────────────────────────────────────────────────────────────

export const createQuoteSheet = async (
  data: CreateQuoteSheetRequest
): Promise<QuoteSheetSummary> => {
  const res = await apiClient.post<ApiResponse<QuoteSheetSummary>>(
    "/v1/quote-sheets",
    data
  )
  return res.data.data
}

export const getQuoteSheetList = async (
  page: number,
  size: number
): Promise<PageResponse<QuoteSheetSummary>> => {
  const res = await apiClient.get<ApiResponse<PageResponse<QuoteSheetSummary>>>(
    "/v1/quote-sheets",
    { params: { page, size } }
  )
  return res.data.data
}

export const getQuoteSheetDetail = async (
  id: number
): Promise<QuoteSheetDetail> => {
  const res = await apiClient.get<ApiResponse<QuoteSheetDetail>>(
    `/v1/quote-sheets/${id}`
  )
  return res.data.data
}

export const generateGoogleSheet = async (
  id: number
): Promise<{ sheetUrl: string }> => {
  const res = await apiClient.post<ApiResponse<{ sheetUrl: string }>>(
    `/v1/quote-sheets/${id}/google-sheet`
  )
  return res.data.data
}

// ─── TanStack Query hooks ──────────────────────────────────────────────────────

export const QUOTE_SHEET_KEYS = {
  all: ["quote-sheets"] as const,
  list: (page: number, size: number) =>
    ["quote-sheets", "list", page, size] as const,
  detail: (id: number) => ["quote-sheets", "detail", id] as const,
}

export const useQuoteSheetList = (page: number, size: number) => {
  return useQuery({
    queryKey: QUOTE_SHEET_KEYS.list(page, size),
    queryFn: () => getQuoteSheetList(page, size),
  })
}

export const useQuoteSheetDetail = (id: number | null) => {
  return useQuery({
    queryKey: QUOTE_SHEET_KEYS.detail(id!),
    queryFn: () => getQuoteSheetDetail(id!),
    enabled: id !== null,
  })
}

export const useCreateQuoteSheet = () => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: createQuoteSheet,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: QUOTE_SHEET_KEYS.all })
    },
  })
}

export const useGenerateGoogleSheet = () => {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: generateGoogleSheet,
    onSuccess: (_data, id) => {
      queryClient.invalidateQueries({
        queryKey: QUOTE_SHEET_KEYS.detail(id),
      })
      queryClient.invalidateQueries({ queryKey: QUOTE_SHEET_KEYS.all })
    },
  })
}
