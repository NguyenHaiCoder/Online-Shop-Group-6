package com.swd392.onlineshop.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

/** Form them / sua slider (MS02, MS04). */
@Getter
@Setter
@NoArgsConstructor
public class SliderForm {

    @NotBlank(message = "Vui lòng nhập tiêu đề.")
    @Size(max = 255, message = "Tiêu đề tối đa 255 ký tự.")
    private String title;

    @NotBlank(message = "Vui lòng nhập nội dung ngắn.")
    @Size(max = 500, message = "Nội dung ngắn tối đa 500 ký tự.")
    private String notes;

    @NotBlank(message = "Vui lòng nhập nhãn nút.")
    @Size(max = 100, message = "Nhãn nút tối đa 100 ký tự.")
    private String buttonLabel;

    @NotBlank(message = "Vui lòng nhập liên kết đích.")
    @Size(max = 255, message = "Liên kết đích tối đa 255 ký tự.")
    @Pattern(regexp = "^(/|https?://).*", message = "Liên kết bắt đầu bằng / hoặc http(s)://")
    private String backlink;

    @NotNull(message = "Vui lòng nhập thứ tự hiển thị.")
    @Min(value = 0, message = "Thứ tự không được âm.")
    @Max(value = 9999, message = "Thứ tự tối đa 9999.")
    private Integer displayOrder;

    private boolean visible = true;

    /** Anh banner moi. Bat buoc khi tao. */
    private MultipartFile imageFile;
}
