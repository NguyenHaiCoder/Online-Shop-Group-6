package com.swd392.onlineshop.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Form Sale Manager giao don cho mot Sale. */
@Getter
@Setter
@NoArgsConstructor
public class AssignOrderForm {

    @NotNull(message = "Chọn Sale.")
    private Long saleId;
}
