package com.swd392.onlineshop.service;
import com.swd392.onlineshop.dto.request.*;
import com.swd392.onlineshop.dto.response.SettingRow;
import org.springframework.data.domain.*;
import java.util.List;
public interface SettingService {
    Page<SettingRow> search(SettingFilter filter, Pageable pageable);
    List<SettingRow> roles();
    com.swd392.onlineshop.dto.response.SettingView view(Long id);
    SettingForm form(Long id);
    Long save(Long id, SettingForm form);
    void toggle(Long id);
}