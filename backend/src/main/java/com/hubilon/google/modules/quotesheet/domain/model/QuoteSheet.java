package com.hubilon.google.modules.quotesheet.domain.model;

import com.hubilon.google.common.entity.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quote_sheets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class QuoteSheet extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "quote_number", nullable = false, unique = true, length = 50)
    private String quoteNumber;

    @Column(name = "contractor_name", length = 100)
    private String contractorName;

    @Column(name = "quote_date")
    private LocalDate quoteDate;

    @Column(name = "total_amount", precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "sheet_url")
    private String sheetUrl;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Builder.Default
    @OneToMany(
            mappedBy = "quoteSheet",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<QuoteItem> items = new ArrayList<>();

    public void updateSheetUrl(String sheetUrl) {
        this.sheetUrl = sheetUrl;
    }
}
