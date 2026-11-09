package com.swd392.onlineshop.dto.request;

import com.swd392.onlineshop.enums.SettingType;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter
public class SettingForm {
    @NotNull private SettingType type;
    @NotBlank @Size(max=100) private String name;
    @NotBlank @Size(max=100) private String code;
    @Size(max=100) private String groupName;
    @NotBlank @Size(max=1000) private String value;
    @NotNull @Min(0) private Integer displayOrder = 0;
    @Size(max=500) private String description;
    private boolean active = true;
}