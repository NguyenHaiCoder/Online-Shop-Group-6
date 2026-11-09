package com.swd392.onlineshop.dto.request;

import com.swd392.onlineshop.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Form cap nhat trang thai va ghi chu noi bo. */
@Getter
@Setter
@NoArgsConstructor
public class OrderUpdateForm {

    @NotNull(message = "Chọn trạng thái.")
    private OrderStatus status;

    @Size(max = 500, message = "Ghi chú tối đa 500 ký tự.")
    private String saleNote;
}
