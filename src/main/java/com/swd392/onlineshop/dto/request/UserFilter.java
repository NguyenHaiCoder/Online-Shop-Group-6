package com.swd392.onlineshop.dto.request;
import com.swd392.onlineshop.enums.*;
import lombok.*;
@Getter @Setter
public class UserFilter {
    private String keyword;
    private Long roleId;
    private Gender gender;
    private UserStatus status;
    private Boolean visible;
}