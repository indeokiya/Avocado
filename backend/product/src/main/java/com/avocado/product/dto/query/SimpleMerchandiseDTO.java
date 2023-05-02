package com.avocado.product.dto.query;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public class SimpleMerchandiseDTO {
    private final Long id;
    private final String brandName;
    private final Long merchandiseId;
    private final String merchandiseCategory;
    private final String imageUrl;
    private final String merchandiseName;
    private final Integer price;
    private final Integer discountedPrice;
    private final Float score;

    @QueryProjection
    public SimpleMerchandiseDTO(Long id, String brandName, Long merchandiseId, String merchandiseCategory, String imageUrl,
                                String merchandiseName, Integer price, Integer discountedPrice, Float score) {
        this.id = id;
        this.brandName = brandName;
        this.merchandiseId = merchandiseId;
        this.merchandiseCategory = merchandiseCategory;
        this.imageUrl = imageUrl;
        this.merchandiseName = merchandiseName;
        this.price = price;
        this.discountedPrice = discountedPrice;
        this.score = score != null ? score : 0;
    }
}