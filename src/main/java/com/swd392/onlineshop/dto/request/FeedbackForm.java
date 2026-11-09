package com.swd392.onlineshop.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Form them / sua phan hoi (MF02, MF04). */
@Getter
@Setter
@NoArgsConstructor
public class FeedbackForm {

    @NotBlank(message = "Vui lòng nhập tên người gửi.")
    @Size(max = 100, message = "Tên người gửi tối đa 100 ký tự.")
    private String fullName;

    @NotBlank(message = "Vui lòng nhập email.")
    @Email(message = "Email không hợp lệ.")
    @Size(max = 100, message = "Email tối đa 100 ký tự.")
    private String email;

    @NotBlank(message = "Vui lòng nhập chủ đề.")
    @Size(max = 255, message = "Chủ đề tối đa 255 ký tự.")
    private String subject;

    /** San pham lien quan, khong bat buoc. */
    private Long productId;

    @NotBlank(message = "Vui lòng nhập nội dung.")
    @Size(max = 2000, message = "Nội dung tối đa 2000 ký tự.")
    private String content;

    private boolean visible = true;
}
