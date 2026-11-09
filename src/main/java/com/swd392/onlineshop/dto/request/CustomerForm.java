package com.swd392.onlineshop.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Form them / sua khach hang (MC02, MC04). */
@Getter
@Setter
@NoArgsConstructor
public class CustomerForm {

    @NotBlank(message = "Vui lòng nhập họ tên.")
    @Size(min = 2, max = 100, message = "Họ tên từ 2 đến 100 ký tự.")
    private String fullName;

    @NotBlank(message = "Vui lòng nhập email.")
    @Email(message = "Email không hợp lệ.")
    @Size(max = 100, message = "Email tối đa 100 ký tự.")
    private String email;

    @NotBlank(message = "Vui lòng nhập số điện thoại.")
    @Pattern(regexp = "^0\\d{9}$", message = "Số điện thoại gồm 10 số, bắt đầu bằng 0.")
    private String mobile;

    @NotBlank(message = "Vui lòng nhập địa chỉ.")
    @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự.")
    private String address;

    @Size(max = 500, message = "Ghi chú tối đa 500 ký tự.")
    private String note;

    private boolean visible = true;
}
