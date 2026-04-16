package com.hubilon.google.modules.quote.domain.model;

import com.hubilon.google.common.entity.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "quotes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Quote extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuoteStatus status;

    @Column(name = "contract_date")
    private LocalDate contractDate;

    @Column(name = "contractor_name", nullable = false, length = 100)
    private String contractorName;

    @Column(name = "construction_location", nullable = false, length = 500)
    private String constructionLocation;

    public enum QuoteStatus {
        PENDING, IN_PROGRESS, COMPLETED, CANCELLED
    }
}
