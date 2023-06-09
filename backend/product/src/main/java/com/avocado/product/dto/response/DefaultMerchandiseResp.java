package com.avocado.product.dto.response;

import com.avocado.product.config.enums.MBTI;
import com.avocado.product.config.enums.PersonalColor;
import com.avocado.product.dto.query.DefaultMerchandiseDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DefaultMerchandiseResp {
    private String brand_name;
    private Long merchandise_id;
    private String merchandise_category;
    private String merchandise_name;
    private Integer price;
    private Integer discounted_price;

    private Boolean is_wishlist;

    private String mbti;
    private String personal_color;
    private String age_group;

    public void updateDefault(DefaultMerchandiseDTO defaultMerchandiseDTO) {
        this.brand_name = defaultMerchandiseDTO.getBrandName();
        this.merchandise_id = defaultMerchandiseDTO.getMerchandiseId();
        this.merchandise_category = defaultMerchandiseDTO.getMerchandiseCategory();
        this.merchandise_name = defaultMerchandiseDTO.getMerchandiseName();
        this.price = defaultMerchandiseDTO.getPrice();
        this.discounted_price = defaultMerchandiseDTO.getDiscountedPrice();
        this.is_wishlist = false;
        this.mbti = MBTI.getMBTI(defaultMerchandiseDTO.getMbtiId());
        this.personal_color = PersonalColor.getPersonalColor(defaultMerchandiseDTO.getPersonalColorId());
        this.age_group = defaultMerchandiseDTO.getAgeGroup() != null
                ? defaultMerchandiseDTO.getAgeGroup().toString() + "대"
                : null;
    }
    
    public void updateIsWishlist(Boolean is_wishlist) {
        this.is_wishlist = is_wishlist;
    }
}
