package com.swd392.onlineshop.repository;
import com.swd392.onlineshop.entity.Slider;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface SliderRepository extends JpaRepository<Slider,Long> {
    List<Slider> findByVisibleTrueOrderByDisplayOrderAscIdAsc();
}