package cr.pedicafe.service;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import java.io.IOException;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FirebaseStorageService {

    public static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;
    private static final Logger logger = LoggerFactory.getLogger(FirebaseStorageService.class);
    private static final String PRODUCT_FOLDER = "productos";
    private static final String FILE_NAME_PATTERN = "(?i)[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.(jpg|png|webp)";

    private final Storage storage;
    @Value("${firebase.bucket.name:}")
    private String bucketName;
    @Value("${firebase.storage.path:pedicafe}")
    private String storagePath;

    public String uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ImagenStorageException("product.image.empty");
        }
        if (file.getSize() > MAX_IMAGE_BYTES) {
            throw new ImagenStorageException("product.image.size");
        }

        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException exception) {
            throw new ImagenStorageException("product.image.read");
        }

        ImageFormat format = detectFormat(content);
        if (format == null) {
            throw new ImagenStorageException("product.image.type");
        }

        if (bucketName.isBlank()) {
            throw new ImagenStorageException(
                    "product.image.firebase.unavailable");
        }

        String fileName = UUID.randomUUID() + "." + format.getExtension();
        String objectName = objectName(fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucketName.trim(), objectName))
                .setContentType(format.getContentType())
                .build();
        try {
            storage.create(blobInfo, content);
            return storage.signUrl(blobInfo, 1825, TimeUnit.DAYS).toString();
        } catch (RuntimeException exception) {
            logger.warn("Falló la carga en Firebase Storage ({}).", exception.getClass().getSimpleName());
            throw new ImagenStorageException(
                    "product.image.uploadFailed");
        }
    }

    public String getSignedImageUrl(String imageReference) {
        if (imageReference == null || imageReference.isBlank()) {
            return imageReference;
        }

        String normalizedReference = imageReference.trim();
        if (normalizedReference.regionMatches(true, 0, "http://", 0, 7)
                || normalizedReference.regionMatches(true, 0, "https://", 0, 8)) {
            return normalizedReference;
        }

        String objectName = getProductObjectName(normalizedReference);
        if (objectName == null) {
           
            return "";
        }
        if (bucketName.isBlank()) {
            return "";
        }

        BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucketName.trim(), objectName)).build();
        try {
            Blob existingBlob = storage.get(blobInfo.getBlobId());
            if (existingBlob == null) {
                return "";
            }
            return storage.signUrl(blobInfo, 1825, TimeUnit.DAYS).toString();
        } catch (RuntimeException exception) {
            logger.warn("No fue posible generar una URL firmada para una imagen de producto ({}).",
                    exception.getClass().getSimpleName());
            return "";
        }
    }

    private String getProductObjectName(String imageReference) {
        String normalizedReference = imageReference.replace('\\', '/');
        String localPrefix = "/imagenes/" + PRODUCT_FOLDER + "/";
        if (normalizedReference.startsWith(localPrefix)) {
            String fileName = normalizedReference.substring(localPrefix.length());
            return fileName.matches(FILE_NAME_PATTERN) ? objectName(fileName) : null;
        }

        String folderPrefix = PRODUCT_FOLDER + "/";
        if (normalizedReference.startsWith(folderPrefix)) {
            String fileName = normalizedReference.substring(folderPrefix.length());
            return fileName.matches(FILE_NAME_PATTERN) ? objectName(fileName) : null;
        }

        String objectPrefix = normalizeStoragePath(storagePath) + "/" + folderPrefix;
        if (normalizedReference.startsWith(objectPrefix)) {
            String fileName = normalizedReference.substring(objectPrefix.length());
            return fileName.matches(FILE_NAME_PATTERN) ? normalizedReference : null;
        }

        return null;
    }

    private String objectName(String fileName) {
        return normalizeStoragePath(storagePath) + "/" + PRODUCT_FOLDER + "/" + fileName;
    }

    private static String normalizeStoragePath(String path) {
        String normalized = path.trim().replace('\\', '/');
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if (normalized.isBlank()) {
            return "pedicafe";
        }
        for (String segment : normalized.split("/")) {
            if (segment.equals(".") || segment.equals("..")) {
                throw new IllegalArgumentException("FIREBASE_STORAGE_PATH contiene un segmento no permitido.");
            }
        }
        return normalized;
    }

    private static ImageFormat detectFormat(byte[] bytes) {
        if (bytes.length >= 3 && (bytes[0] & 0xFF) == 0xFF
                && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF) {
            return new ImageFormat("jpg", "image/jpeg");
        }
        if (bytes.length >= 8 && (bytes[0] & 0xFF) == 0x89
                && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47
                && bytes[4] == 0x0D && bytes[5] == 0x0A && bytes[6] == 0x1A && bytes[7] == 0x0A) {
            return new ImageFormat("png", "image/png");
        }
        if (bytes.length >= 12 && ascii(bytes, 0, 4).equals("RIFF")
                && ascii(bytes, 8, 4).equals("WEBP")) {
            return new ImageFormat("webp", "image/webp");
        }
        return null;
    }

    private static String ascii(byte[] bytes, int offset, int length) {
        return new String(bytes, offset, length, java.nio.charset.StandardCharsets.US_ASCII).toUpperCase(Locale.ROOT);
    }

    @Getter
    @RequiredArgsConstructor
    private static class ImageFormat {
        private final String extension;
        private final String contentType;
    }

}
