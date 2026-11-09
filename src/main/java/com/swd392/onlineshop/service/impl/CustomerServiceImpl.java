package com.swd392.onlineshop.service.impl;

import com.swd392.onlineshop.dto.request.CustomerFilter;
import com.swd392.onlineshop.dto.request.CustomerForm;
import com.swd392.onlineshop.dto.request.ReceiverForm;
import com.swd392.onlineshop.dto.response.CustomerRow;
import com.swd392.onlineshop.dto.response.CustomerView;
import com.swd392.onlineshop.entity.Customer;
import com.swd392.onlineshop.entity.CustomerHistory;
import com.swd392.onlineshop.enums.CustomerStatus;
import com.swd392.onlineshop.enums.Gender;
import com.swd392.onlineshop.enums.OrderStatus;
import com.swd392.onlineshop.exception.NotFoundException;
import com.swd392.onlineshop.repository.CustomerHistoryRepository;
import com.swd392.onlineshop.repository.CustomerRepository;
import com.swd392.onlineshop.repository.OrderRepository;
import com.swd392.onlineshop.service.CustomerService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerHistoryRepository customerHistoryRepository;
    private final OrderRepository orderRepository;

    @Value("${app.page-size:10}")
    private int pageSize;

    @Override
    @Transactional
    public void refreshStatus(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy khách hàng."));
        customer.setStatus(resolve(customer));
    }

    @Override
    @Transactional
    public void refreshStatus(Customer customer) {
        if (customer == null || customer.getId() == null) {
            return;
        }
        customer.setStatus(resolve(customer));
        customerRepository.save(customer);
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

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerRow> search(CustomerFilter filter, int page) {
        String keyword = trimToNull(filter.getKeyword());
        Specification<Customer> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null) {
                String like = "%" + keyword + "%";
                predicates.add(cb.or(
                        cb.like(root.get("fullName"), like),
                        cb.like(root.get("code"), like),
                        cb.like(root.get("email"), like)));
            }
            if (filter.getVisible() != null) {
                predicates.add(cb.equal(root.get("visible"), filter.getVisible()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        PageRequest pageable = PageRequest.of(Math.max(page, 1) - 1, pageSize,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));
        return customerRepository.findAll(spec, pageable).map(c -> CustomerRow.builder()
                .id(c.getId())
                .code(c.getCode())
                .fullName(c.getFullName())
                .mobile(c.getMobile())
                .email(c.getEmail())
                .createdAt(c.getCreatedAt())
                .visible(c.isVisible())
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerView getView(Long id) {
        Customer c = find(id);
        return CustomerView.builder()
                .id(c.getId())
                .code(c.getCode())
                .fullName(c.getFullName())
                .email(c.getEmail())
                .mobile(c.getMobile())
                .address(c.getAddress())
                .note(c.getNote())
                .visible(c.isVisible())
                .linkedAccount(c.getUser() != null)
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerForm getForm(Long id) {
        Customer c = find(id);
        CustomerForm form = new CustomerForm();
        form.setFullName(c.getFullName());
        form.setEmail(c.getEmail());
        form.setMobile(c.getMobile());
        form.setAddress(c.getAddress());
        form.setNote(c.getNote());
        form.setVisible(c.isVisible());
        return form;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean emailExists(String email, Long excludeId) {
        String value = normalizeEmail(email);
        if (value == null) {
            return false;
        }
        return excludeId == null
                ? customerRepository.existsByEmailIgnoreCase(value)
                : customerRepository.existsByEmailIgnoreCaseAndIdNot(value, excludeId);
    }

    @Override
    @Transactional
    public Long create(CustomerForm form) {
        Customer c = new Customer();
        c.setCode(tempCode());
        c.setEmail(normalizeEmail(form.getEmail()));
        c.setStatus(CustomerStatus.CONTACT);
        apply(c, form);
        customerRepository.saveAndFlush(c);
        c.setCode("KH" + String.format("%04d", c.getId()));
        return c.getId();
    }

    @Override
    @Transactional
    public void update(Long id, CustomerForm form) {
        Customer c = find(id);
        String email = normalizeEmail(form.getEmail());
        if (!c.getEmail().equalsIgnoreCase(email)) {
            if (c.getUser() != null) {
                throw new IllegalArgumentException("Email đã gắn với tài khoản, không đổi được.");
            }
            c.setEmail(email);
        }
        apply(c, form);
    }

    @Override
    @Transactional
    public boolean toggleVisible(Long id) {
        Customer c = find(id);
        c.setVisible(!c.isVisible());
        return c.isVisible();
    }

    @Override
    @Transactional
    public Customer upsertFromReceiver(ReceiverForm form) {
        String email = form.getEmail().trim().toLowerCase(Locale.ROOT);
        Optional<Customer> existingOpt = customerRepository.findByEmail(email);

        if (existingOpt.isEmpty()) {
            Customer customer = new Customer();
            customer.setCode(generateCustomerCode());
            customer.setEmail(email);
            customer.setFullName(form.getFullName().trim());
            customer.setGender(form.getGender());
            customer.setMobile(form.getMobile().trim());
            customer.setAddress(form.getAddress().trim());
            customer.setStatus(CustomerStatus.CONTACT);
            customer.setVisible(true);

            Customer saved = customerRepository.save(customer);

            CustomerHistory history = new CustomerHistory();
            history.setCustomer(saved);
            history.setEmail(saved.getEmail());
            history.setFullName(saved.getFullName());
            history.setGender(saved.getGender());
            history.setMobile(saved.getMobile());
            history.setAddress(saved.getAddress());
            history.setUpdatedBy(null);
            customerHistoryRepository.save(history);

            return saved;
        }

        Customer customer = existingOpt.get();
        boolean changed = !Objects.equals(customer.getFullName(), form.getFullName().trim())
                || !Objects.equals(customer.getGender(), form.getGender())
                || !Objects.equals(customer.getMobile(), form.getMobile().trim())
                || !Objects.equals(customer.getAddress(), form.getAddress().trim());

        if (changed) {
            customer.setFullName(form.getFullName().trim());
            customer.setGender(form.getGender());
            customer.setMobile(form.getMobile().trim());
            customer.setAddress(form.getAddress().trim());
            customerRepository.save(customer);

            CustomerHistory history = new CustomerHistory();
            history.setCustomer(customer);
            history.setEmail(customer.getEmail());
            history.setFullName(customer.getFullName());
            history.setGender(customer.getGender());
            history.setMobile(customer.getMobile());
            history.setAddress(customer.getAddress());
            history.setUpdatedBy(null);
            customerHistoryRepository.save(history);
        }

        return customer;
    }

    @Override
    @Transactional
    public Customer findOrCreateContact(String email, String fullName, Gender gender, String mobile) {
        String cleanEmail = email.trim().toLowerCase(Locale.ROOT);
        return customerRepository.findByEmail(cleanEmail).orElseGet(() -> {
            Customer customer = new Customer();
            customer.setCode(generateCustomerCode());
            customer.setEmail(cleanEmail);
            customer.setFullName(fullName != null ? fullName.trim() : "Khách hàng");
            customer.setGender(gender != null ? gender : Gender.OTHER);
            customer.setMobile(mobile != null ? mobile.trim() : "");
            customer.setStatus(CustomerStatus.CONTACT);
            customer.setVisible(true);

            Customer saved = customerRepository.save(customer);

            CustomerHistory history = new CustomerHistory();
            history.setCustomer(saved);
            history.setEmail(saved.getEmail());
            history.setFullName(saved.getFullName());
            history.setGender(saved.getGender());
            history.setMobile(saved.getMobile());
            history.setAddress(saved.getAddress());
            history.setUpdatedBy(null);
            customerHistoryRepository.save(history);

            return saved;
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Customer> findByUserId(Long userId) {
        return customerRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Customer> findByEmail(String email) {
        return customerRepository.findByEmail(email.trim().toLowerCase(Locale.ROOT));
    }

    private String generateCustomerCode() {
        long count = customerRepository.count();
        String code = String.format("KH%04d", count + 1);
        while (customerRepository.existsByCode(code)) {
            code = String.format("KH%04d", count + 1 + new Random().nextInt(1000));
        }
        return code;
    }

    private void apply(Customer c, CustomerForm form) {
        c.setFullName(form.getFullName().trim());
        c.setMobile(form.getMobile().trim());
        c.setAddress(form.getAddress().trim());
        c.setNote(trimToNull(form.getNote()));
        c.setVisible(form.isVisible());
    }

    private Customer find(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy khách hàng."));
    }

    private static String normalizeEmail(String email) {
        String value = trimToNull(email);
        return value == null ? null : value.toLowerCase(Locale.ROOT);
    }

    private static String tempCode() {
        return "TMP" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
