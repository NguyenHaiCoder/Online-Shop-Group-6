package com.swd392.onlineshop.service;

import com.swd392.onlineshop.dto.request.CustomerFilter;
import com.swd392.onlineshop.dto.request.CustomerForm;
import com.swd392.onlineshop.dto.request.ReceiverForm;
import com.swd392.onlineshop.dto.response.CustomerRow;
import com.swd392.onlineshop.dto.response.CustomerView;
import com.swd392.onlineshop.entity.Customer;
import com.swd392.onlineshop.enums.Gender;
import org.springframework.data.domain.Page;

import java.util.Optional;

/**
 * Tinh lai customers.status theo don va tai khoan. Khong gui email.
 * <p>
 * Marketing: View Customer List, Add New Customer, View Customer Details,
 * Edit Customer, Show/Hide Customer (MC01-MC05).
 * <p>
 * Customer & Public: upsertFromReceiver, findOrCreateContact, findByUserId, findByEmail.
 */
public interface CustomerService {

    void refreshStatus(Long customerId);

    void refreshStatus(Customer customer);

    /** page bat dau tu 1. */
    Page<CustomerRow> search(CustomerFilter filter, int page);

    CustomerView getView(Long id);

    CustomerForm getForm(Long id);

    /** excludeId = null khi tao moi. */
    boolean emailExists(String email, Long excludeId);

    Long create(CustomerForm form);

    void update(Long id, CustomerForm form);

    /** Doi Hien/An, tra ve trang thai moi. */
    boolean toggleVisible(Long id);

    Customer upsertFromReceiver(ReceiverForm form);

    Customer findOrCreateContact(String email, String fullName, Gender gender, String mobile);

    Optional<Customer> findByUserId(Long userId);

    Optional<Customer> findByEmail(String email);
}
