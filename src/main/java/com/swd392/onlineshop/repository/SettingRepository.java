package com.swd392.onlineshop.repository;

import com.swd392.onlineshop.entity.Setting;
import com.swd392.onlineshop.enums.SettingType;
import org.springframework.data.jpa.repository.*;
import java.util.*;

public interface SettingRepository extends JpaRepository<Setting, Long>, JpaSpecificationExecutor<Setting> {
    List<Setting> findByTypeOrderByDisplayOrderAscIdAsc(SettingType type);
    Optional<Setting> findByTypeAndValue(SettingType type, String value);
    boolean existsByTypeAndCodeAndIdNot(SettingType type, String code, Long id);
    boolean existsByTypeAndValueAndIdNot(SettingType type, String value, Long id);
}