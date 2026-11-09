package com.swd392.onlineshop.dto.request;

import com.swd392.onlineshop.enums.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter
public class UserForm {
    @Size(max=20) private String code;
    @NotBlank @Size(min=2,max=100) private String fullName;
    @NotBlank @Email @Size(max=100) private String email;
    @Size(max=15) private String mobile;
    @Size(max=255) private String address;
    private Gender gender;
    @NotNull private Long roleId;
    @NotNull private UserStatus status = UserStatus.ACTIVE;
    @Size(max=32) private String password;
    @Size(max=32) private String confirmPassword;
    private org.springframework.web.multipart.MultipartFile avatarFile;
    private boolean visible = true;
    private boolean emailVerified;
}