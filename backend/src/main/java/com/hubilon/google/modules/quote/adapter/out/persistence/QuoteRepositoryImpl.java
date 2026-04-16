package com.hubilon.google.modules.quote.adapter.out.persistence;

import com.hubilon.google.modules.quote.application.port.out.QuoteRepository;
import com.hubilon.google.modules.quote.application.port.out.QuoteSearchCondition;
import com.hubilon.google.modules.quote.domain.model.Quote;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class QuoteRepositoryImpl implements QuoteRepository {

    private final QuoteJpaRepository quoteJpaRepository;

    @Override
    public Page<Quote> findAll(QuoteSearchCondition condition, Pageable pageable) {
        int cappedSize = Math.min(pageable.getPageSize(), 50);
        Pageable cappedPageable = PageRequest.of(pageable.getPageNumber(), cappedSize, pageable.getSort());

        Specification<Quote> spec = buildSpecification(condition);
        return quoteJpaRepository.findAll(spec, cappedPageable);
    }

    private Specification<Quote> buildSpecification(QuoteSearchCondition condition) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (condition.startDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("contractDate"), condition.startDate()));
            }

            if (condition.endDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("contractDate"), condition.endDate()));
            }

            if (condition.contractorName() != null && !condition.contractorName().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("contractorName")),
                        "%" + condition.contractorName().toLowerCase() + "%"
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
