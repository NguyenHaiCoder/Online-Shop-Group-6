package com.swd392.onlineshop.dto.response;
import com.swd392.onlineshop.entity.Setting;
import com.swd392.onlineshop.enums.SettingType;
public record SettingRow(Long id, SettingType type, String name, String code, String groupName,
                         String value, Integer displayOrder, String description, boolean active) {
    public static SettingRow from(Setting s) {
        return new SettingRow(s.getId(), s.getType(), s.getName(), s.getCode(), s.getGroupName(),
                s.getValue(), s.getDisplayOrder(), s.getDescription(), s.isActive());
    }
}