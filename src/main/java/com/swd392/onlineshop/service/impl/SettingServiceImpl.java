package com.swd392.onlineshop.service.impl;

import com.swd392.onlineshop.dto.request.SettingFilter;
import com.swd392.onlineshop.dto.request.SettingForm;
import com.swd392.onlineshop.dto.response.SettingRow;
import com.swd392.onlineshop.dto.response.SettingView;
import com.swd392.onlineshop.entity.Setting;
import com.swd392.onlineshop.enums.SettingType;
import com.swd392.onlineshop.repository.SettingRepository;
import com.swd392.onlineshop.repository.UserRepository;
import com.swd392.onlineshop.service.FormException;
import com.swd392.onlineshop.service.SettingService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettingServiceImpl implements SettingService {

    public static final Set<String> ROLE_VALUES = Set.of("ADMIN", "CUSTOMER", "MARKETING", "SALE", "SALE_MANAGER");
    private final SettingRepository settings;
    private final UserRepository users;

    @Override
    public Page<SettingRow> search(SettingFilter f, Pageable pageable) {
        return settings.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (f.getType() != null) predicates.add(cb.equal(root.get("type"), f.getType()));
            if (f.getActive() != null) predicates.add(cb.equal(root.get("active"), f.getActive()));
            if (f.getKeyword() != null && !f.getKeyword().isBlank()) {
                String k = "%" + f.getKeyword().trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(cb.like(cb.lower(root.get("code")), k), cb.like(cb.lower(root.get("name")), k)));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        }, pageable).map(SettingRow::from);
    }

    @Override
    public List<SettingRow> roles() {
        return settings.findByTypeOrderByDisplayOrderAscIdAsc(SettingType.USER_ROLE).stream()
                .filter(Setting::isActive).filter(s -> ROLE_VALUES.contains(s.getValue())).map(SettingRow::from).toList();
    }

    private Setting get(Long id) {
        return settings.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Override
    public SettingView view(Long id) {
        return SettingView.from(get(id));
    }

    @Override
    public SettingForm form(Long id) {
        Setting s = get(id);
        SettingForm f = new SettingForm();
        f.setType(s.getType());
        f.setCode(s.getCode());
        f.setName(s.getName());
        f.setGroupName(s.getGroupName());
        f.setValue(s.getValue());
        f.setDisplayOrder(s.getDisplayOrder());
        f.setDescription(s.getDescription());
        f.setActive(s.isActive());
        return f;
    }

    @Override
    @Transactional
    public Long save(Long id, SettingForm f) {
        Setting s = id == null ? new Setting() : get(id);
        if (s.getType() == SettingType.USER_ROLE && (f.getType() != s.getType() || !s.getValue().equals(f.getValue())))
            throw new FormException("value", "Không được đổi nhóm hoặc mã quyền của vai trò có sẵn.");
        if (f.getType() == SettingType.USER_ROLE && !ROLE_VALUES.contains(f.getValue()))
            throw new FormException("value", "Quyền hợp lệ: ADMIN, CUSTOMER, MARKETING, SALE, SALE_MANAGER.");
        if (id != null && s.getType() == SettingType.USER_ROLE && !f.isActive() && users.existsByRoleId(id))
            throw new FormException("active", "Vai trò đang được sử dụng, không thể ẩn.");
        Long except = id == null ? -1L : id;
        if (settings.existsByTypeAndCodeAndIdNot(f.getType(), f.getCode().trim(), except))
            throw new FormException("code", "Mã cấu hình đã tồn tại trong nhóm.");
        if (settings.existsByTypeAndValueAndIdNot(f.getType(), f.getValue().trim(), except))
            throw new FormException("value", "Giá trị đã tồn tại trong nhóm.");
        s.setType(f.getType());
        s.setCode(f.getCode().trim());
        s.setName(f.getName().trim());
        s.setGroupName(f.getGroupName());
        s.setValue(f.getValue().trim());
        s.setDisplayOrder(f.getDisplayOrder());
        s.setDescription(f.getDescription());
        s.setActive(f.isActive());
        return settings.save(s).getId();
    }

    @Override
    @Transactional
    public void toggle(Long id) {
        Setting s = get(id);
        if (s.getType() == SettingType.USER_ROLE && s.isActive() && users.existsByRoleId(id))
            throw new FormException("active", "Vai trò đang được sử dụng, không thể ẩn.");
        s.setActive(!s.isActive());
    }

    @Override
    public List<Setting> getActiveSettingsByType(SettingType type) {
        return settings.findByTypeAndActiveTrueOrderByDisplayOrderAsc(type);
    }

    @Override
    public String getSettingValueByCode(String code, String defaultValue) {
        return settings.findByCode(code)
                .filter(Setting::isActive)
                .map(Setting::getValue)
                .orElse(defaultValue);
    }
}
