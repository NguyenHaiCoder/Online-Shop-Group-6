package com.swd392.onlineshop.service;

import com.swd392.onlineshop.dto.request.SettingFilter;
import com.swd392.onlineshop.dto.request.SettingForm;
import com.swd392.onlineshop.dto.response.SettingRow;
import com.swd392.onlineshop.dto.response.SettingView;
import com.swd392.onlineshop.entity.Setting;
import com.swd392.onlineshop.enums.SettingType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SettingService {

    Page<SettingRow> search(SettingFilter filter, Pageable pageable);

    List<SettingRow> roles();

    SettingView view(Long id);

    SettingForm form(Long id);

    Long save(Long id, SettingForm form);

    void toggle(Long id);

    List<Setting> getActiveSettingsByType(SettingType type);

    String getSettingValueByCode(String code, String defaultValue);
}
