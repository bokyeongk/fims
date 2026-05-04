package com.hubilon.google.modules.quotesheet.adapter.out.persistence;

import com.hubilon.google.modules.quotesheet.domain.model.QuoteSheet;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface QuoteSheetJpaRepository extends JpaRepository<QuoteSheet, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT qs FROM QuoteSheet qs WHERE qs.id = :id")
    Optional<QuoteSheet> findByIdWithLock(@Param("id") Long id);

    @EntityGraph(attributePaths = {"items"})
    Page<QuoteSheet> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT COUNT(qs) FROM QuoteSheet qs WHERE qs.createDate >= :startOfDay AND qs.createDate < :endOfDay")
    long countByCreateDate(@Param("startOfDay") LocalDateTime startOfDay,
                            @Param("endOfDay") LocalDateTime endOfDay);
}
