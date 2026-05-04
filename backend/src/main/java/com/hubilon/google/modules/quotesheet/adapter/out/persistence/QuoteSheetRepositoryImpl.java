package com.hubilon.google.modules.quotesheet.adapter.out.persistence;

import com.hubilon.google.common.exception.code.ErrorCode;
import com.hubilon.google.common.exception.custom.ServiceException;
import com.hubilon.google.modules.quotesheet.application.port.out.QuoteSheetRepository;
import com.hubilon.google.modules.quotesheet.domain.model.QuoteSheet;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class QuoteSheetRepositoryImpl implements QuoteSheetRepository {

    private final QuoteSheetJpaRepository quoteSheetJpaRepository;

    @Override
    public QuoteSheet save(QuoteSheet quoteSheet) {
        return quoteSheetJpaRepository.save(quoteSheet);
    }

    @Override
    public Optional<QuoteSheet> findById(Long id) {
        return quoteSheetJpaRepository.findById(id);
    }

    @Override
    public Optional<QuoteSheet> findByIdWithLock(Long id) {
        return quoteSheetJpaRepository.findByIdWithLock(id);
    }

    @Override
    public Page<QuoteSheet> findByUserId(Long userId, Pageable pageable) {
        return quoteSheetJpaRepository.findByUserId(userId, pageable);
    }

    @Override
    public QuoteSheet updateSheetUrl(Long id, String sheetUrl) {
        QuoteSheet quoteSheet = quoteSheetJpaRepository.findByIdWithLock(id)
                .orElseThrow(() -> new ServiceException(ErrorCode.NOT_FOUND));
        quoteSheet.updateSheetUrl(sheetUrl);
        return quoteSheetJpaRepository.save(quoteSheet);
    }

    @Override
    public long countByCreateDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
        return quoteSheetJpaRepository.countByCreateDate(startOfDay, endOfDay);
    }
}
