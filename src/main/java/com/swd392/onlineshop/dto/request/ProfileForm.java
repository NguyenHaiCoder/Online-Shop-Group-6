package com.swd392.onlineshop.dto.request;

import com.swd392.onlineshop.entity.User;
import com.swd392.onlineshop.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class ProfileForm {

    @NotBlank(message = "Họ tên không được để trống")
    @Size(min = 2, max = 100, message = "Họ tên từ 2 đến 100 ký tự")
    private String fullName;

    private String email;

    private Gender gender;

    @Size(max = 15, message = "Số điện thoại tối đa 15 ký tự")
    private String mobile;

    @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự")
    private String address;

    private String avatar;

    private MultipartFile avatarFile;

    public static ProfileForm from(User user) {
        ProfileForm form = new ProfileForm();
        if (user != null) {
            form.setFullName(user.getFullName());
            form.setEmail(user.getEmail());
            form.setGender(user.getGender());
            form.setMobile(user.getMobile());
            form.setAddress(user.getAddress());
            form.setAvatar(user.getAvatar());
        }
        return form;
    }
}
