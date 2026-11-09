package com.swd392.onlineshop.service.impl;

import com.swd392.onlineshop.entity.Customer;
import com.swd392.onlineshop.enums.CustomerStatus;
import com.swd392.onlineshop.enums.OrderStatus;
import com.swd392.onlineshop.exception.NotFoundException;
import com.swd392.onlineshop.repository.CustomerRepository;
import com.swd392.onlineshop.repository.OrderRepository;
import com.swd392.onlineshop.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public void refreshStatus(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy khách hàng."));
        customer.setStatus(resolve(customer));
    }

    private CustomerStatus resolve(Customer customer) {
        if (orderRepository.existsByCustomer_IdAndStatus(customer.getId(), OrderStatus.SUCCESS)) {
            return CustomerStatus.CUSTOMER;
        }
        if (customer.getUser() != null || orderRepository.existsByCustomer_Id(customer.getId())) {
            return CustomerStatus.POTENTIAL;
        }
        return CustomerStatus.CONTACT;
    }
}
