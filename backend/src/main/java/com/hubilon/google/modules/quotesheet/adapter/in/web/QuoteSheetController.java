package com.hubilon.google.modules.quotesheet.adapter.in.web;

import com.hubilon.google.common.exception.code.ErrorCode;
import com.hubilon.google.common.exception.custom.ServiceException;
import com.hubilon.google.common.response.ApiResponse;
import com.hubilon.google.common.response.PageResponse;
import com.hubilon.google.config.multiLanguage.MessageProvider;
import com.hubilon.google.modules.quotesheet.application.port.in.CreateQuoteSheetCommand;
import com.hubilon.google.modules.quotesheet.application.port.in.CreateQuoteSheetUseCase;
import com.hubilon.google.modules.quotesheet.application.port.in.GenerateGoogleSheetUseCase;
import com.hubilon.google.modules.quotesheet.application.port.in.GetQuoteSheetDetailUseCase;
import com.hubilon.google.modules.quotesheet.application.port.in.GetQuoteSheetListUseCase;
import com.hubilon.google.modules.quotesheet.domain.model.QuoteSheet;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/quote-sheets")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
@Tag(name = "QuoteSheet", description = "견적서 관리 API")
@SecurityRequirement(name = "bearerAuth")
public class QuoteSheetController {

    private final CreateQuoteSheetUseCase createQuoteSheetUseCase;
    private final GetQuoteSheetListUseCase getQuoteSheetListUseCase;
    private final GetQuoteSheetDetailUseCase getQuoteSheetDetailUseCase;
    private final GenerateGoogleSheetUseCase generateGoogleSheetUseCase;
    private final MessageProvider messageProvider;

    @PostMapping
    @Operation(summary = "견적서 생성")
    public ResponseEntity<ApiResponse<QuoteSheetResponse>> createQuoteSheet(
            @Valid @RequestBody CreateQuoteSheetRequest request) {

        Long userId = resolveUserId();

        List<CreateQuoteSheetCommand.QuoteItemCommand> itemCommands = request.items().stream()
                .map(item -> new CreateQuoteSheetCommand.QuoteItemCommand(
                        item.itemName(),
                        item.spec(),
                        item.category(),
                        item.quantity(),
                        item.unit(),
                        item.unitPrice()
                ))
                .toList();

        CreateQuoteSheetCommand command = new CreateQuoteSheetCommand(
                userId,
                request.quoteDate(),
                request.contractorName(),
                itemCommands,
                request.note()
        );

        QuoteSheet created = createQuoteSheetUseCase.create(command);
        String message = messageProvider.getMessage("quote.sheet.create.success");

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(message, QuoteSheetResponse.from(created)));
    }

    @GetMapping
    @Operation(summary = "견적서 목록 조회")
    public ResponseEntity<ApiResponse<PageResponse<QuoteSheetSummaryResponse>>> getList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Long userId = resolveUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createDate"));

        Page<QuoteSheet> result = getQuoteSheetListUseCase.getList(userId, pageable);

        PageResponse<QuoteSheetSummaryResponse> response = PageResponse.of(
                result.map(QuoteSheetSummaryResponse::from)
        );

        String message = messageProvider.getMessage("quote.sheet.list.success");
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "견적서 상세 조회")
    public ResponseEntity<ApiResponse<QuoteSheetDetailResponse>> getDetail(
            @PathVariable Long id) {

        Long userId = resolveUserId();
        QuoteSheet quoteSheet = getQuoteSheetDetailUseCase.getDetail(id, userId);

        String message = messageProvider.getMessage("quote.sheet.detail.success");
        return ResponseEntity.ok(ApiResponse.success(message, QuoteSheetDetailResponse.from(quoteSheet)));
    }

    @PostMapping("/{id}/google-sheet")
    @Operation(summary = "Google 시트 생성 또는 URL 조회")
    public ResponseEntity<ApiResponse<GoogleSheetUrlResponse>> generateGoogleSheet(
            @PathVariable Long id) {

        Long userId = resolveUserId();
        String sheetUrl = generateGoogleSheetUseCase.generateOrGetSheetUrl(id, userId);

        String message = messageProvider.getMessage("quote.sheet.google.success");
        return ResponseEntity.ok(ApiResponse.success(message, GoogleSheetUrlResponse.of(sheetUrl)));
    }

    private Long resolveUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ServiceException(ErrorCode.UNAUTHORIZED);
        }
        return Long.parseLong((String) authentication.getPrincipal());
    }
}
