package com.swd392.onlineshop.service.impl;

import com.swd392.onlineshop.config.AppProperties;
import com.swd392.onlineshop.dto.request.PostFilter;
import com.swd392.onlineshop.dto.request.PostForm;
import com.swd392.onlineshop.dto.response.PostRow;
import com.swd392.onlineshop.dto.response.PostView;
import com.swd392.onlineshop.entity.Post;
import com.swd392.onlineshop.entity.User;
import com.swd392.onlineshop.exception.NotFoundException;
import com.swd392.onlineshop.repository.PostRepository;
import com.swd392.onlineshop.repository.UserRepository;
import com.swd392.onlineshop.service.FileStorageService;
import com.swd392.onlineshop.service.PostService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private static final String FOLDER = "posts";

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final AppProperties appProperties;

    @Value("${app.page-size:10}")
    private int pageSize;

    @Override
    @Transactional(readOnly = true)
    public Page<PostRow> search(PostFilter filter, int page) {
        String keyword = trimToNull(filter.getKeyword());
        Specification<Post> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null) {
                String like = "%" + keyword + "%";
                Join<Post, User> author = root.join("author", JoinType.LEFT);
                predicates.add(cb.or(
                        cb.like(root.get("title"), like),
                        cb.like(root.get("code"), like),
                        cb.like(author.get("email"), like)));
            }
            if (filter.getVisible() != null) {
                predicates.add(cb.equal(root.get("visible"), filter.getVisible()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        PageRequest pageable = PageRequest.of(Math.max(page, 1) - 1, pageSize,
                Sort.by(Sort.Order.desc("updatedAt"), Sort.Order.desc("id")));
        return postRepository.findAll(spec, pageable).map(p -> PostRow.builder()
                .id(p.getId())
                .code(p.getCode())
                .title(p.getTitle())
                .thumbnail(p.getThumbnail())
                .authorName(p.getAuthor() != null ? p.getAuthor().getFullName() : null)
                .updatedAt(p.getUpdatedAt())
                .visible(p.isVisible())
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public PostView getView(Long id) {
        Post p = find(id);
        return PostView.builder()
                .id(p.getId())
                .code(p.getCode())
                .title(p.getTitle())
                .thumbnail(p.getThumbnail())
                .briefInfo(p.getBriefInfo())
                .content(p.getContent())
                .authorName(p.getAuthor() != null ? p.getAuthor().getFullName() : null)
                .visible(p.isVisible())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PostForm getForm(Long id) {
        Post p = find(id);
        PostForm form = new PostForm();
        form.setTitle(p.getTitle());
        form.setBriefInfo(p.getBriefInfo());
        form.setContent(p.getContent());
        form.setVisible(p.isVisible());
        return form;
    }

    @Override
    @Transactional
    public Long create(PostForm form, String authorEmail) {
        User author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tài khoản tác giả không tồn tại."));
        Post p = new Post();
        p.setCode(tempCode());
        p.setAuthor(author);
        if (hasFile(form.getThumbnailFile())) {
            p.setThumbnail(fileStorageService.store(form.getThumbnailFile(), FOLDER));
        }
        apply(p, form);
        postRepository.saveAndFlush(p);
        p.setCode("BV" + String.format("%04d", p.getId()));
        return p.getId();
    }

    @Override
    @Transactional
    public void update(Long id, PostForm form) {
        Post p = find(id);
        if (hasFile(form.getThumbnailFile())) {
            p.setThumbnail(fileStorageService.store(form.getThumbnailFile(), FOLDER));
        }
        apply(p, form);
    }

    @Override
    @Transactional
    public boolean toggleVisible(Long id) {
        Post p = find(id);
        p.setVisible(!p.isVisible());
        return p.isVisible();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Post> getFeaturedPosts(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "updatedAt"));
        return postRepository.findByVisibleTrueAndFeaturedTrue(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Post> getLatestPosts(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "updatedAt"));
        return postRepository.findByVisibleTrue(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Post> getPublicPosts(PostFilter filter) {
        int pageIndex = (filter.getPage() != null && filter.getPage() > 0) ? filter.getPage() - 1 : 0;
        int defaultPageSize = (appProperties != null && appProperties.getPageSize() > 0) ? appProperties.getPageSize() : pageSize;
        Pageable pageable = PageRequest.of(pageIndex, defaultPageSize);

        String keyword = (filter.getKeyword() != null && !filter.getKeyword().isBlank())
                ? filter.getKeyword().trim()
                : null;

        return postRepository.findPublicPosts(keyword, filter.getCategoryId(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Post getPublicPostById(Long id) {
        return postRepository.findByIdAndVisibleTrue(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy bài viết hoặc bài viết đã bị ẩn."));
    }

    private void apply(Post p, PostForm form) {
        p.setTitle(form.getTitle().trim());
        p.setBriefInfo(form.getBriefInfo().trim());
        p.setContent(form.getContent().trim());
        p.setVisible(form.isVisible());
    }

    private Post find(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bài viết."));
    }

    private static String tempCode() {
        return "TMP" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
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
