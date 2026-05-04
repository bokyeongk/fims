package com.hubilon.google.modules.quotesheet.adapter.out.persistence;

import com.hubilon.google.modules.quotesheet.domain.model.QuoteItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuoteItemJpaRepository extends JpaRepository<QuoteItem, Long> {
}
