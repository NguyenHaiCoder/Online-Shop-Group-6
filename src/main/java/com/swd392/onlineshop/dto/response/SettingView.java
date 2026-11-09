package com.swd392.onlineshop.dto.response;
import com.swd392.onlineshop.entity.Setting;
import java.time.LocalDateTime;
public record SettingView(SettingRow info, LocalDateTime createdAt, LocalDateTime updatedAt) {
    public static SettingView from(Setting s) { return new SettingView(SettingRow.from(s), s.getCreatedAt(), s.getUpdatedAt()); }
}