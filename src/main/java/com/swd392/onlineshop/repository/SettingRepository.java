package com.swd392.onlineshop.repository;

import com.swd392.onlineshop.entity.Setting;
import com.swd392.onlineshop.enums.SettingType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SettingRepository extends JpaRepository<Setting, Long> {

    List<Setting> findByTypeAndActiveTrueOrderByDisplayOrderAsc(SettingType type);

    Optional<Setting> findByIdAndType(Long id, SettingType type);
}
