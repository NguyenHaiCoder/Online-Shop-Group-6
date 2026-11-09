package com.swd392.onlineshop.dto.request;

import com.swd392.onlineshop.enums.Gender;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter
public class RegisterForm {
    @NotBlank @Size(min=2,max=100) private String fullName;
    @NotBlank @Email @Size(max=100) private String email;
    @NotNull private Gender gender;
    @NotBlank @Pattern(regexp="0[0-9]{9}") private String mobile;
    @NotBlank @Size(max=255) private String address;
    @NotBlank @Pattern(regexp="(?=.*[A-Za-z])(?=.*[0-9]).{6,32}", message="Mật khẩu cần 6–32 ký tự, có chữ và số.")
    private String password;
    @NotBlank @Size(max=32) private String confirmPassword;
}