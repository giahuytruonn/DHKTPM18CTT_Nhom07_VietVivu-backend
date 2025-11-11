package tourbooking.vietvivu.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                        "folder", "vietvivu/tours",
                        "resource_type", "image"
                ));

        String url = (String) uploadResult.get("secure_url");
        log.info("Upload successful: {}", url);
        return url;
    }

    /**
     * Upload nhiều ảnh
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
     * Xóa ảnh từ Cloudinary bằng URL
     */
    public void deleteImage(String imageUrl) throws IOException {
        log.info("Deleting image from Cloudinary: {}", imageUrl);

        // Extract public_id từ URL
        // VD: https://res.cloudinary.com/dpyshymwv/image/upload/v1234/vietvivu/tours/abc.jpg
        // => public_id = vietvivu/tours/abc
        String publicId = extractPublicId(imageUrl);

        if (publicId != null) {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Image deleted successfully: {}", publicId);
        }
    }

    /**
     * Xóa nhiều ảnh
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
     * Trích xuất public_id từ Cloudinary URL
     */
    private String extractPublicId(String imageUrl) {
        try {
            // URL format: https://res.cloudinary.com/{cloud_name}/image/upload/v{version}/{public_id}.{format}
            String[] parts = imageUrl.split("/upload/");
            if (parts.length < 2) return null;

            String pathWithVersion = parts[1];
            // Remove version (vXXXX/)
            String path = pathWithVersion.replaceFirst("v\\d+/", "");
            // Remove extension
            return path.substring(0, path.lastIndexOf('.'));
        } catch (Exception e) {
            log.error("Failed to extract public_id from URL: {}", imageUrl, e);
            return null;
        }
    }
}