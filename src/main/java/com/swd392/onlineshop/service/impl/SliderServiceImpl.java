package com.swd392.onlineshop.service.impl;

import com.swd392.onlineshop.dto.request.SliderFilter;
import com.swd392.onlineshop.dto.request.SliderForm;
import com.swd392.onlineshop.dto.response.SliderRow;
import com.swd392.onlineshop.dto.response.SliderView;
import com.swd392.onlineshop.entity.Slider;
import com.swd392.onlineshop.entity.User;
import com.swd392.onlineshop.repository.SliderRepository;
import com.swd392.onlineshop.repository.UserRepository;
import com.swd392.onlineshop.service.FileStorageService;
import com.swd392.onlineshop.service.SliderService;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SliderServiceImpl implements SliderService {

    private static final String FOLDER = "sliders";

    private final SliderRepository sliderRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Value("${app.page-size:10}")
    private int pageSize;

    @Override
    @Transactional(readOnly = true)
    public Page<SliderRow> search(SliderFilter filter, int page) {
        String keyword = trimToNull(filter.getKeyword());
        Specification<Slider> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null) {
                String like = "%" + keyword + "%";
                predicates.add(cb.or(
                        cb.like(root.get("title"), like),
                        cb.like(root.get("notes"), like),
                        cb.like(root.get("backlink"), like)));
            }
            if (filter.getVisible() != null) {
                predicates.add(cb.equal(root.get("visible"), filter.getVisible()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        PageRequest pageable = PageRequest.of(Math.max(page, 1) - 1, pageSize,
                Sort.by(Sort.Order.asc("displayOrder"), Sort.Order.asc("id")));
        return sliderRepository.findAll(spec, pageable).map(s -> SliderRow.builder()
                .id(s.getId())
                .title(s.getTitle())
                .image(s.getImage())
                .backlink(s.getBacklink())
                .displayOrder(s.getDisplayOrder())
                .visible(s.isVisible())
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public SliderView getView(Long id) {
        Slider s = find(id);
        return SliderView.builder()
                .id(s.getId())
                .title(s.getTitle())
                .image(s.getImage())
                .notes(s.getNotes())
                .buttonLabel(s.getButtonLabel())
                .backlink(s.getBacklink())
                .displayOrder(s.getDisplayOrder())
                .visible(s.isVisible())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SliderForm getForm(Long id) {
        Slider s = find(id);
        SliderForm form = new SliderForm();
        form.setTitle(s.getTitle());
        form.setNotes(s.getNotes());
        form.setButtonLabel(s.getButtonLabel());
        form.setBacklink(s.getBacklink());
        form.setDisplayOrder(s.getDisplayOrder());
        form.setVisible(s.isVisible());
        return form;
    }

    @Override
    @Transactional
    public Long create(SliderForm form, String authorEmail) {
        User author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy tài khoản đang đăng nhập."));
        Slider s = new Slider();
        s.setImage(fileStorageService.store(form.getImageFile(), FOLDER));
        apply(s, form);
        s.setAuthor(author);
        return sliderRepository.save(s).getId();
    }

    @Override
    @Transactional
    public void update(Long id, SliderForm form) {
        Slider s = find(id);
        if (hasFile(form.getImageFile())) {
            s.setImage(fileStorageService.store(form.getImageFile(), FOLDER));
        }
        apply(s, form);
    }

    @Override
    @Transactional
    public boolean toggleVisible(Long id) {
        Slider s = find(id);
        s.setVisible(!s.isVisible());
        return s.isVisible();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Slider> getVisibleSliders() {
        return sliderRepository.findByVisibleTrueOrderByDisplayOrderAsc();
    }

    private void apply(Slider s, SliderForm form) {
        s.setTitle(form.getTitle().trim());
        s.setNotes(form.getNotes().trim());
        s.setButtonLabel(form.getButtonLabel().trim());
        s.setBacklink(form.getBacklink().trim());
        s.setDisplayOrder(form.getDisplayOrder());
        s.setVisible(form.isVisible());
    }

    private Slider find(Long id) {
        return sliderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy slider."));
    }

    private static boolean hasFile(MultipartFile file) {
        return file != null && !file.isEmpty();
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
