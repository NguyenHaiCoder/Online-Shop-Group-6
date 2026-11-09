package com.swd392.onlineshop.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.swd392.onlineshop.config.CloudinaryProperties;
import com.swd392.onlineshop.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    private static final Set<String> FOLDERS = Set.of("avatars", "posts", "sliders", "products", "feedbacks");
    private static final Set<String> TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final long MAX_BYTES = 5L * 1024 * 1024;

    private final Cloudinary cloudinary;
    private final CloudinaryProperties properties;

    @Override
    public String store(MultipartFile file, String folder) {
        requireConfigured();
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Chưa chọn ảnh.");
        }
        if (!FOLDERS.contains(folder)) {
            throw new IllegalArgumentException("Thư mục ảnh không hợp lệ.");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new IllegalArgumentException("Ảnh tối đa 5MB.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Chỉ nhận ảnh JPG, PNG hoặc WEBP.");
        }
        String publicId = "onlineshop/" + folder + "/" + UUID.randomUUID();
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "public_id", publicId,
                    "resource_type", "image",
                    "overwrite", false
            ));
            Object secureUrl = result.get("secure_url");
            if (secureUrl == null) {
                throw new IllegalStateException("Cloudinary không trả về URL ảnh.");
            }
            return secureUrl.toString();
        } catch (IOException ex) {
            log.error("Upload Cloudinary that bai, folder={}", folder, ex);
            throw new IllegalStateException("Không tải được ảnh lên. Thử lại sau.");
        } catch (RuntimeException ex) {
            if (ex instanceof IllegalStateException illegal) {
                throw illegal;
            }
            log.error("Upload Cloudinary that bai, folder={}", folder, ex);
            throw new IllegalStateException("Không tải được ảnh lên. Thử lại sau.");
        }
    }

    @Override
    public String url(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return null;
        }
        if (publicId.startsWith("http://") || publicId.startsWith("https://")) {
            return publicId;
        }
        requireConfigured();
        return cloudinary.url().secure(true).generate(publicId);
    }

    private void requireConfigured() {
        if (!properties.isConfigured()) {
            throw new IllegalStateException("Chưa cấu hình Cloudinary. Điền cloudinary.cloud-name, api-key và api-secret.");
        }
    }
}
