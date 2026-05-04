package com.hubilon.google.modules.quotesheet.application.service;

import com.hubilon.google.common.exception.code.ErrorCode;
import com.hubilon.google.common.exception.custom.ServiceException;
import com.hubilon.google.modules.quotesheet.application.port.in.CreateQuoteSheetCommand;
import com.hubilon.google.modules.quotesheet.application.port.in.CreateQuoteSheetUseCase;
import com.hubilon.google.modules.quotesheet.application.port.in.GetQuoteSheetDetailUseCase;
import com.hubilon.google.modules.quotesheet.application.port.in.GetQuoteSheetListUseCase;
import com.hubilon.google.modules.quotesheet.application.port.out.QuoteSheetRepository;
import com.hubilon.google.modules.quotesheet.domain.model.QuoteItem;
import com.hubilon.google.modules.quotesheet.domain.model.QuoteSheet;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuoteSheetService implements CreateQuoteSheetUseCase, GetQuoteSheetListUseCase, GetQuoteSheetDetailUseCase {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final QuoteSheetRepository quoteSheetRepository;

    @Override
    @Transactional
    public QuoteSheet create(CreateQuoteSheetCommand command) {
        String quoteNumber = generateQuoteNumber();

        // totalAmount 선계산
        BigDecimal totalAmount = command.items().stream()
                .map(itemCmd -> itemCmd.unitPrice().multiply(BigDecimal.valueOf(itemCmd.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // QuoteSheet 먼저 저장 (items 비어있는 상태)
        QuoteSheet quoteSheet = QuoteSheet.builder()
                .userId(command.userId())
                .quoteNumber(quoteNumber)
                .quoteDate(command.quoteDate())
                .contractorName(command.contractorName())
                .totalAmount(totalAmount)
                .note(command.note())
                .build();

        QuoteSheet saved = quoteSheetRepository.save(quoteSheet);

        // QuoteItem 생성 후 QuoteSheet에 추가 (CascadeType.ALL로 함께 저장)
        command.items().forEach(itemCmd -> {
            BigDecimal amount = itemCmd.unitPrice().multiply(BigDecimal.valueOf(itemCmd.quantity()));
            QuoteItem item = QuoteItem.builder()
                    .quoteSheet(saved)
                    .itemName(itemCmd.itemName())
                    .spec(itemCmd.spec())
                    .category(itemCmd.category())
                    .quantity(itemCmd.quantity())
                    .unit(itemCmd.unit())
                    .unitPrice(itemCmd.unitPrice())
                    .amount(amount)
                    .build();
            saved.getItems().add(item);
        });

        return quoteSheetRepository.save(saved);
    }

    @Override
    public Page<QuoteSheet> getList(Long userId, Pageable pageable) {
        return quoteSheetRepository.findByUserId(userId, pageable);
    }

    @Override
    public QuoteSheet getDetail(Long id, Long userId) {
        QuoteSheet quoteSheet = quoteSheetRepository.findById(id)
                .orElseThrow(() -> new ServiceException(ErrorCode.QUOTE_SHEET_NOT_FOUND));

        if (!quoteSheet.getUserId().equals(userId)) {
            throw new ServiceException(ErrorCode.QUOTE_SHEET_FORBIDDEN);
        }

        return quoteSheet;
    }

    private String generateQuoteNumber() {
        LocalDate today = LocalDate.now();
        long count = quoteSheetRepository.countByCreateDate(today);
        String datePart = today.format(DATE_FORMATTER);
        return String.format("Q-%s-%03d", datePart, count + 1);
    }
}
