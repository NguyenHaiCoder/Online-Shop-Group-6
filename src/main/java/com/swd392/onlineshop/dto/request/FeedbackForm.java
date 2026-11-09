package com.swd392.onlineshop.dto.request;

import com.swd392.onlineshop.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Form phan hoi cho ca khach hang (public) va Marketing (MF02, MF04).
 */
@Getter
@Setter
@NoArgsConstructor
public class FeedbackForm {

    private Long productId;

    @NotBlank(message = "Vui lòng nhập họ tên.")
    @Size(max = 100, message = "Họ tên tối đa 100 ký tự.")
    private String fullName;

    private Gender gender;

    @NotBlank(message = "Vui lòng nhập email.")
    @Email(message = "Email không đúng định dạng.")
    @Size(max = 100, message = "Email tối đa 100 ký tự.")
    private String email;

    @Size(max = 15, message = "Số điện thoại tối đa 15 ký tự.")
    private String mobile;

    @Size(max = 255, message = "Chủ đề tối đa 255 ký tự.")
    private String subject = "Phản hồi từ khách hàng";

    @Min(value = 1, message = "Đánh giá từ 1 đến 5 sao.")
    @Max(value = 5, message = "Đánh giá từ 1 đến 5 sao.")
    private Integer rating = 5;

    @NotBlank(message = "Vui lòng nhập nội dung.")
    @Size(max = 2000, message = "Nội dung tối đa 2000 ký tự.")
    private String content;

    private boolean visible = true;

    private List<MultipartFile> images;
}
