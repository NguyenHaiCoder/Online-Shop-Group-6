package com.swd392.onlineshop.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Tim kiem / loc danh sach slider (MS01). */
@Getter
@Setter
@NoArgsConstructor
public class SliderFilter {

    /** Tim theo tieu de, noi dung ngan hoac lien ket dich. */
    private String keyword;
    private Boolean visible;
}
