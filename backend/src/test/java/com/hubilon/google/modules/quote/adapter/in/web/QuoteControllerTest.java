package com.hubilon.google.modules.quote.adapter.in.web;

import com.hubilon.google.common.response.PageResponse;
import com.hubilon.google.config.multiLanguage.MessageProvider;
import com.hubilon.google.config.security.JwtProvider;
import com.hubilon.google.modules.quote.application.port.in.GetQuoteListQuery;
import com.hubilon.google.modules.quote.application.port.in.GetQuoteListUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QuoteController.class)
@DisplayName("QuoteController - GET /api/v1/quotes")
class QuoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetQuoteListUseCase getQuoteListUseCase;

    @MockBean
    private MessageProvider messageProvider;

    @MockBean
    private JwtProvider jwtProvider;

    @Test
    @WithMockUser
    @DisplayName("기본 파라미터로 견적 목록 조회 시 200 응답과 success:true 반환")
    void shouldReturn200WithSuccessTrueOnDefaultParams() throws Exception {
        PageResponse<QuoteListResponse> emptyPage = PageResponse.<QuoteListResponse>builder()
                .content(List.of())
                .page(0)
                .size(10)
                .totalElements(0)
                .hasNext(false)
                .build();

        given(getQuoteListUseCase.getQuoteList(any(GetQuoteListQuery.class)))
                .willReturn(emptyPage);

        mockMvc.perform(get("/api/v1/quotes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.hasNext").value(false));
    }

    @Test
    @WithMockUser
    @DisplayName("날짜 파라미터를 명시하면 해당 파라미터로 서비스가 호출됨")
    void shouldPassDateParamsToService() throws Exception {
        QuoteListResponse item = new QuoteListResponse(
                1L,
                "PENDING",
                LocalDate.of(2026, 4, 10),
                "홍길동",
                "서울 강남구 역삼동 123-45"
        );

        PageResponse<QuoteListResponse> page = PageResponse.<QuoteListResponse>builder()
                .content(List.of(item))
                .page(0)
                .size(10)
                .totalElements(1)
                .hasNext(false)
                .build();

        given(getQuoteListUseCase.getQuoteList(any(GetQuoteListQuery.class)))
                .willReturn(page);

        mockMvc.perform(get("/api/v1/quotes")
                        .param("startDate", "2026-04-01")
                        .param("endDate", "2026-04-16")
                        .param("sort", "LATEST")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].contractorName").value("홍길동"))
                .andExpect(jsonPath("$.data.content[0].status").value("PENDING"));
    }

    @Test
    @WithMockUser
    @DisplayName("contractorName 파라미터 포함 시 200 응답 반환")
    void shouldReturn200WithContractorNameFilter() throws Exception {
        PageResponse<QuoteListResponse> emptyPage = PageResponse.<QuoteListResponse>builder()
                .content(List.of())
                .page(0)
                .size(10)
                .totalElements(0)
                .hasNext(false)
                .build();

        given(getQuoteListUseCase.getQuoteList(any(GetQuoteListQuery.class)))
                .willReturn(emptyPage);

        mockMvc.perform(get("/api/v1/quotes")
                        .param("contractorName", "김철수"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
