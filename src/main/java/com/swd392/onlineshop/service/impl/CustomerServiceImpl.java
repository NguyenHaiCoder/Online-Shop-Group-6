package com.swd392.onlineshop.service.impl;

import com.swd392.onlineshop.dto.request.CustomerFilter;
import com.swd392.onlineshop.dto.request.CustomerForm;
import com.swd392.onlineshop.dto.response.CustomerRow;
import com.swd392.onlineshop.dto.response.CustomerView;
import com.swd392.onlineshop.entity.Customer;
import com.swd392.onlineshop.repository.CustomerRepository;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Value("${app.page-size:10}")
    private int pageSize;

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
        c.setEmail(normalizeEmail(form.getEmail()));
        apply(c, form);
        // Ma KH0001 sinh theo id: luu ma tam truoc de co id
        c.setCode(tempCode());
        customerRepository.saveAndFlush(c);
        c.setCode(String.format("KH%04d", c.getId()));
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
