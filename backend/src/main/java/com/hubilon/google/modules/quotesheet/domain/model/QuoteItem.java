package com.hubilon.google.modules.quotesheet.domain.model;

import com.hubilon.google.common.entity.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "quote_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class QuoteItem extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_sheet_id", nullable = false)
    private QuoteSheet quoteSheet;

    @Column(name = "item_name", length = 200)
    private String itemName;

    @Column(name = "spec", length = 200)
    private String spec;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "unit_price", precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "amount", precision = 19, scale = 2)
    private BigDecimal amount;
}
