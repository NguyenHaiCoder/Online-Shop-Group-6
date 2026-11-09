package com.swd392.onlineshop.service;

import com.swd392.onlineshop.dto.request.SliderFilter;
import com.swd392.onlineshop.dto.request.SliderForm;
import com.swd392.onlineshop.dto.response.SliderRow;
import com.swd392.onlineshop.dto.response.SliderView;
import com.swd392.onlineshop.entity.Slider;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Marketing: View Slider List, Add New Slider, View Slider Details,
 * Edit Slider, Show/Hide Slider (MS01-MS05).
 * Public: Get visible sliders for homepage.
 */
public interface SliderService {

    /** page bat dau tu 1. */
    Page<SliderRow> search(SliderFilter filter, int page);

    SliderView getView(Long id);

    SliderForm getForm(Long id);

    /** authorEmail: Marketing dang dang nhap. */
    Long create(SliderForm form, String authorEmail);

    void update(Long id, SliderForm form);

    /** Doi Hien/An, tra ve trang thai moi. */
    boolean toggleVisible(Long id);

    List<Slider> getVisibleSliders();
}
