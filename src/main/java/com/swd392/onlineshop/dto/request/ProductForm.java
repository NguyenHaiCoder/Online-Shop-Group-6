package com.swd392.onlineshop.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Form them / sua san pham (PR02, PR04).
 * specifications: moi dong "Ten: Gia tri".
 */
@Getter
@Setter
@NoArgsConstructor
public class ProductForm {

    @NotBlank(message = "Vui lòng nhập mã sản phẩm.")
    @Size(max = 50, message = "Mã sản phẩm tối đa 50 ký tự.")
    private String code;

    @NotBlank(message = "Vui lòng nhập tên sản phẩm.")
    @Size(max = 255, message = "Tên sản phẩm tối đa 255 ký tự.")
    private String name;

    @NotNull(message = "Vui lòng chọn danh mục.")
    private Long categoryId;

    @NotNull(message = "Vui lòng nhập giá bán.")
    @DecimalMin(value = "0", message = "Giá bán không được âm.")
    @DecimalMax(value = "9999999999999", message = "Giá bán quá lớn.")
    @Digits(integer = 13, fraction = 0, message = "Giá bán là số nguyên.")
    private BigDecimal originalPrice;

    @NotNull(message = "Vui lòng nhập số lượng còn bán.")
    @Min(value = 0, message = "Số lượng không được âm.")
    @Max(value = 1000000000, message = "Số lượng quá lớn.")
    private Integer stockQuantity;

    private boolean visible = true;

    @NotBlank(message = "Vui lòng nhập mô tả ngắn.")
    @Size(max = 500, message = "Mô tả ngắn tối đa 500 ký tự.")
    private String briefInfo;

    @NotBlank(message = "Vui lòng nhập mô tả đầy đủ.")
    private String description;

    @NotBlank(message = "Vui lòng nhập thông số kỹ thuật.")
    private String specifications;

    /** Anh dai dien moi. Bat buoc khi tao. */
    private MultipartFile thumbnailFile;

    /** Anh gallery them moi. */
    private List<MultipartFile> galleryFiles = new ArrayList<>();

    /** Id anh gallery can xoa khi sua. */
    private List<Long> deleteImageIds = new ArrayList<>();
}
