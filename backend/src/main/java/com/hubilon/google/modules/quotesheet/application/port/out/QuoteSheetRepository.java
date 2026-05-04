package com.hubilon.google.modules.quotesheet.application.port.out;

import com.hubilon.google.modules.quotesheet.domain.model.QuoteSheet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;

public interface QuoteSheetRepository {

    QuoteSheet save(QuoteSheet quoteSheet);

    Optional<QuoteSheet> findById(Long id);

    /**
     * Pessimistic write lock — use when updating mutable fields (e.g. sheetUrl) to prevent lost updates.
     */
    Optional<QuoteSheet> findByIdWithLock(Long id);

    Page<QuoteSheet> findByUserId(Long userId, Pageable pageable);

    QuoteSheet updateSheetUrl(Long id, String sheetUrl);

    /**
     * 특정 날짜(createDate 기준)에 생성된 견적서 수를 반환.
     * 견적번호 순번 생성에 사용.
     */
    long countByCreateDate(LocalDate date);
}
