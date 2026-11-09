package com.swd392.onlineshop.service.impl;

import com.swd392.onlineshop.dto.request.FeedbackFilter;
import com.swd392.onlineshop.dto.request.FeedbackForm;
import com.swd392.onlineshop.dto.response.FeedbackRow;
import com.swd392.onlineshop.dto.response.FeedbackView;
import com.swd392.onlineshop.entity.Customer;
import com.swd392.onlineshop.entity.Feedback;
import com.swd392.onlineshop.entity.FeedbackImage;
import com.swd392.onlineshop.entity.Product;
import com.swd392.onlineshop.entity.User;
import com.swd392.onlineshop.exception.NotFoundException;
import com.swd392.onlineshop.repository.CustomerRepository;
import com.swd392.onlineshop.repository.FeedbackImageRepository;
import com.swd392.onlineshop.repository.FeedbackRepository;
import com.swd392.onlineshop.repository.ProductRepository;
import com.swd392.onlineshop.repository.UserRepository;
import com.swd392.onlineshop.service.CustomerService;
import com.swd392.onlineshop.service.FeedbackService;
import com.swd392.onlineshop.service.FileStorageService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final FeedbackImageRepository feedbackImageRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CustomerService customerService;
    private final FileStorageService fileStorageService;

    @Value("${app.page-size:10}")
    private int pageSize;

    @Override
    @Transactional(readOnly = true)
    public Page<FeedbackRow> search(FeedbackFilter filter, int page) {
        String keyword = trimToNull(filter.getKeyword());
        Specification<Feedback> spec = (root, query, cb) -> {
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
        return feedbackRepository.findAll(spec, pageable).map(f -> FeedbackRow.builder()
                .id(f.getId())
                .code(f.getCode())
                .fullName(f.getFullName())
                .email(f.getEmail())
                .subject(f.getSubject())
                .createdAt(f.getCreatedAt())
                .visible(f.isVisible())
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public FeedbackView getView(Long id) {
        Feedback f = find(id);
        Product product = f.getProduct();
        return FeedbackView.builder()
                .id(f.getId())
                .code(f.getCode())
                .source(f.getSource())
                .fullName(f.getFullName())
                .email(f.getEmail())
                .subject(f.getSubject())
                .content(f.getContent())
                .visible(f.isVisible())
                .productId(product != null ? product.getId() : null)
                .productCode(product != null ? product.getCode() : null)
                .productName(product != null ? product.getName() : null)
                .productThumbnail(product != null ? product.getThumbnail() : null)
                .imageUrls(f.getImages().stream().map(FeedbackImage::getImageUrl).toList())
                .updatedByName(f.getUpdatedBy() != null ? f.getUpdatedBy().getFullName() : null)
                .createdAt(f.getCreatedAt())
                .updatedAt(f.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public FeedbackForm getForm(Long id) {
        Feedback f = find(id);
        FeedbackForm form = new FeedbackForm();
        form.setFullName(f.getFullName());
        form.setEmail(f.getEmail());
        form.setSubject(f.getSubject());
        form.setProductId(f.getProduct() != null ? f.getProduct().getId() : null);
        form.setContent(f.getContent());
        form.setVisible(f.isVisible());
        return form;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean customerExists(String email) {
        String value = normalizeEmail(email);
        return value != null && customerRepository.existsByEmailIgnoreCase(value);
    }

    @Override
    @Transactional
    public Long create(FeedbackForm form) {
        Customer customer = requireCustomer(form.getEmail());
        Feedback f = new Feedback();
        f.setCode(tempCode());
        f.setCustomer(customer);
        f.setSource("Website");
        apply(f, form);
        feedbackRepository.saveAndFlush(f);
        f.setCode("PH" + String.format("%04d", f.getId()));
        return f.getId();
    }

    @Override
    @Transactional
    public void update(Long id, FeedbackForm form, String actorEmail) {
        Feedback f = find(id);
        Customer customer = requireCustomer(form.getEmail());
        f.setCustomer(customer);
        f.setUpdatedBy(requireUser(actorEmail));
        apply(f, form);
    }

    @Override
    @Transactional
    public boolean toggleVisible(Long id, String actorEmail) {
        Feedback f = find(id);
        f.setVisible(!f.isVisible());
        f.setUpdatedBy(requireUser(actorEmail));
        return f.isVisible();
    }

    @Override
    @Transactional
    public Feedback submitFeedback(FeedbackForm form, Long userId) {
        Product product = null;
        if (form.getProductId() != null) {
            product = productRepository.findByIdAndVisibleTrue(form.getProductId())
                    .orElseThrow(() -> new NotFoundException("Sản phẩm không tồn tại hoặc đã ngừng kinh doanh."));
        }

        Customer customer = customerService.findOrCreateContact(
                form.getEmail(),
                form.getFullName(),
                form.getGender(),
                form.getMobile()
        );

        Feedback feedback = new Feedback();
        feedback.setCode(generateFeedbackCode());
        feedback.setCustomer(customer);
        feedback.setProduct(product);
        feedback.setFullName(form.getFullName().trim());
        feedback.setEmail(form.getEmail().trim().toLowerCase(Locale.ROOT));
        feedback.setMobile(form.getMobile() != null ? form.getMobile().trim() : null);
        feedback.setGender(form.getGender());
        feedback.setRating(form.getRating() != null ? form.getRating() : 5);
        feedback.setSubject(form.getSubject() != null && !form.getSubject().isBlank()
                ? form.getSubject().trim()
                : (product != null ? "Phản hồi về " + product.getName() : "Ý kiến đóng góp dịch vụ"));
        feedback.setContent(form.getContent().trim());
        feedback.setSource("Website");
        feedback.setVisible(true);

        Feedback savedFeedback = feedbackRepository.save(feedback);

        if (form.getImages() != null && !form.getImages().isEmpty()) {
            for (MultipartFile file : form.getImages()) {
                if (file != null && !file.isEmpty()) {
                    try {
                        String imageUrl = fileStorageService.store(file, "feedbacks");
                        FeedbackImage fi = new FeedbackImage();
                        fi.setFeedback(savedFeedback);
                        fi.setImageUrl(imageUrl);
                        feedbackImageRepository.save(fi);
                        savedFeedback.getImages().add(fi);
                    } catch (Exception ex) {
                        log.warn("Lỗi upload ảnh feedback lên Cloudinary: {}", ex.getMessage());
                    }
                }
            }
        }

        return savedFeedback;
    }

    private String generateFeedbackCode() {
        long count = feedbackRepository.count();
        String code = String.format("PH%04d", count + 1);
        while (feedbackRepository.existsByCode(code)) {
            code = String.format("PH%04d", count + 1 + new Random().nextInt(1000));
        }
        return code;
    }

    private void apply(Feedback f, FeedbackForm form) {
        f.setFullName(form.getFullName().trim());
        f.setEmail(normalizeEmail(form.getEmail()));
        f.setSubject(form.getSubject().trim());
        f.setContent(form.getContent().trim());
        f.setVisible(form.isVisible());
        if (form.getProductId() == null) {
            f.setProduct(null);
        } else {
            f.setProduct(productRepository.findById(form.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Sản phẩm liên quan không hợp lệ.")));
        }
    }

    private Customer requireCustomer(String email) {
        return customerRepository.findByEmailIgnoreCase(normalizeEmail(email))
                .orElseThrow(() -> new IllegalArgumentException("Email chưa có trong danh sách khách hàng."));
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy tài khoản đang đăng nhập."));
    }

    private Feedback find(Long id) {
        return feedbackRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy phản hồi."));
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
