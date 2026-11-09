package com.swd392.onlineshop.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Upload anh len Cloudinary.
 * folder: avatars, posts, sliders, products, feedbacks.
 * Gia tri tra ve la secure_url, luu thang vao DB.
 */
public interface FileStorageService {

    String store(MultipartFile file, String folder);

    /** URL giao anh. publicId dang onlineshop/products/uuid, hoac chinh secure_url thi tra lai. */
    String url(String publicId);
}
