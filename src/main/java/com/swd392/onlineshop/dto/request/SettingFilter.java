package com.swd392.onlineshop.dto.request;
import com.swd392.onlineshop.enums.SettingType;
import lombok.*;
@Getter @Setter
public class SettingFilter {
    private String keyword;
    private SettingType type;
    private Boolean active;
}