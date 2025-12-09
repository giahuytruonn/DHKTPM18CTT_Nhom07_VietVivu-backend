package tourbooking.vietvivu.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;

    /**
     * Upload 1 ảnh lên Cloudinary
     */
    public String uploadImage(MultipartFile file) throws IOException {
        log.info("Uploading image to Cloudinary: {}", file.getOriginalFilename());

        Map uploadResult = cloudinary
                .uploader()
                .upload(
                        file.getBytes(),
                        ObjectUtils.asMap(
                                "folder", "vietvivu/tours",
                                "resource_type", "image"));

        String url = (String) uploadResult.get("secure_url");
        log.info("Upload successful: {}", url);
        return url;
    }

    /**
     * Upload 1 ảnh bất đồng bộ
     */
    @Async
    public CompletableFuture<String> uploadImageAsync(MultipartFile file) {
        try {
            log.info("Async uploading image: {}", file.getOriginalFilename());
            String url = uploadImage(file);
            return CompletableFuture.completedFuture(url);
        } catch (IOException e) {
            log.error("Async upload failed for: {}", file.getOriginalFilename(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Upload nhiều ảnh đồng bộ (giữ nguyên cho backward compatibility)
     */
    public List<String> uploadMultipleImages(List<MultipartFile> files) throws IOException {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            String url = uploadImage(file);
            urls.add(url);
        }
        return urls;
    }

    /**
     * Upload nhiều ảnh bất đồng bộ (song song)
     */
    @Async
    public CompletableFuture<List<String>> uploadMultipleImagesAsync(List<MultipartFile> files) {
        try {
            log.info("Starting async upload for {} images", files.size());

            // Upload tất cả ảnh song song
            List<CompletableFuture<String>> futures =
                    files.stream().map(this::uploadImageAsync).collect(Collectors.toList());

            // Đợi tất cả upload xong
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

            return allFutures.thenApply(
                    v -> futures.stream().map(CompletableFuture::join).collect(Collectors.toList()));

        } catch (Exception e) {
            log.error("Failed to upload multiple images async", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Xóa ảnh từ Cloudinary bằng URL
     */
    public void deleteImage(String imageUrl) throws IOException {
        log.info("Deleting image from Cloudinary: {}", imageUrl);

        String publicId = extractPublicId(imageUrl);

        if (publicId != null) {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Image deleted successfully: {}", publicId);
        }
    }

    /**
     * Xóa ảnh bất đồng bộ
     */
    @Async
    public CompletableFuture<Void> deleteImageAsync(String imageUrl) {
        try {
            deleteImage(imageUrl);
            return CompletableFuture.completedFuture(null);
        } catch (IOException e) {
            log.error("Failed to delete image async: {}", imageUrl, e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Xóa nhiều ảnh đồng bộ
     */
    public void deleteMultipleImages(List<String> imageUrls) {
        for (String url : imageUrls) {
            try {
                deleteImage(url);
            } catch (IOException e) {
                log.error("Failed to delete image: {}", url, e);
            }
        }
    }

    /**
     * Xóa nhiều ảnh bất đồng bộ (song song)
     */
    @Async
    public CompletableFuture<Void> deleteMultipleImagesAsync(List<String> imageUrls) {
        log.info("Starting async deletion for {} images", imageUrls.size());

        List<CompletableFuture<Void>> futures =
                imageUrls.stream().map(this::deleteImageAsync).collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    /**
     * Trích xuất public_id từ Cloudinary URL
     */
    private String extractPublicId(String imageUrl) {
        try {
            String[] parts = imageUrl.split("/upload/");
            if (parts.length < 2) return null;

            String pathWithVersion = parts[1];
            String path = pathWithVersion.replaceFirst("v\\d+/", "");
            return path.substring(0, path.lastIndexOf('.'));
        } catch (Exception e) {
            log.error("Failed to extract public_id from URL: {}", imageUrl, e);
            return null;
        }
    }
}
