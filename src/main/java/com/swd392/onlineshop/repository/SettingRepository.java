package com.swd392.onlineshop.repository;

import com.swd392.onlineshop.entity.Setting;
import com.swd392.onlineshop.enums.SettingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface SettingRepository extends JpaRepository<Setting, Long>, JpaSpecificationExecutor<Setting> {

    List<Setting> findByTypeAndActiveTrueOrderByDisplayOrderAsc(SettingType type);

    Optional<Setting> findByIdAndType(Long id, SettingType type);

    List<Setting> findByTypeOrderByDisplayOrderAscIdAsc(SettingType type);

    Optional<Setting> findByTypeAndValue(SettingType type, String value);

    boolean existsByTypeAndCodeAndIdNot(SettingType type, String code, Long id);

    boolean existsByTypeAndValueAndIdNot(SettingType type, String value, Long id);

    Optional<Setting> findByCode(String code);
}
