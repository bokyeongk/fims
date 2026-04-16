package com.hubilon.google.modules.quote.adapter.out.persistence;

import com.hubilon.google.modules.quote.domain.model.Quote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface QuoteJpaRepository extends JpaRepository<Quote, Long>, JpaSpecificationExecutor<Quote> {
}
