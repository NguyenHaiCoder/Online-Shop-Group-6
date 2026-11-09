package com.swd392.onlineshop.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

/** Form them / sua bai viet (MP02, MP04). */
@Getter
@Setter
@NoArgsConstructor
public class PostForm {

    @NotBlank(message = "Vui lòng nhập tiêu đề.")
    @Size(max = 255, message = "Tiêu đề tối đa 255 ký tự.")
    private String title;

    @NotBlank(message = "Vui lòng nhập tóm tắt.")
    @Size(max = 160, message = "Tóm tắt tối đa 160 ký tự.")
    private String briefInfo;

    @NotBlank(message = "Vui lòng nhập nội dung.")
    private String content;

    private boolean visible = true;

    /** Anh dai dien moi. Bat buoc khi tao. */
    private MultipartFile thumbnailFile;
}
