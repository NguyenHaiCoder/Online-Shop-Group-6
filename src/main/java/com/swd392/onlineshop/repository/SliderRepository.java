package com.swd392.onlineshop.repository;

import com.swd392.onlineshop.entity.Slider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface SliderRepository extends JpaRepository<Slider, Long>, JpaSpecificationExecutor<Slider> {

    List<Slider> findByVisibleTrueOrderByDisplayOrderAsc();
}
