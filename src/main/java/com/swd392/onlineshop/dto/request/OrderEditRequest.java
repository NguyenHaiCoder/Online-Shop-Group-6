package com.swd392.onlineshop.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class OrderEditRequest {

    @NotBlank(message = "Họ tên người nhận không được để trống")
    private String receiverName;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String receiverMobile;

    @NotBlank(message = "Email không được để trống")
    private String receiverEmail;

    @NotBlank(message = "Địa chỉ nhận hàng không được để trống")
    private String receiverAddress;

    private String note;

    private List<Long> itemIds = new ArrayList<>();

    private List<Integer> quantities = new ArrayList<>();
}

