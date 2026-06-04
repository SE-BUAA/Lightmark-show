package top.ortus.timemark.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import top.ortus.timemark.backend.exception.ApiException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class ObjectStorageService {

    private final HttpClient httpClient;
    private final String baseUrl;

    public ObjectStorageService(@Value("${timemark.object-storage.base-url:}") String baseUrl) {
        this.httpClient = HttpClient.newBuilder().build();
        this.baseUrl = baseUrl == null ? "" : baseUrl.trim();
    }

    public String uploadAvatar(String normalizedUserId16, MultipartFile file) {
        if (baseUrl.isBlank()) {
            throw new ApiException(500, "object storage not configured");
        }
        if (normalizedUserId16 == null || normalizedUserId16.isBlank()) {
            throw new ApiException(400, "invalid user id");
        }
        if (file == null || file.isEmpty()) {
            throw new ApiException(400, "file is required");
        }
        if (file.getSize() > 2L * 1024L * 1024L) {
            throw new ApiException(400, "file too large");
        }

        byte[] jpegBytes = toJpeg(file);
        String objectName = "avatar-" + normalizedUserId16 + ".jpg";
        String url = normalizeBaseUrl(baseUrl) + "/" + objectName;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "image/jpeg")
                .PUT(HttpRequest.BodyPublishers.ofByteArray(jpegBytes))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new ApiException(502, "object storage upload failed");
            }
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiException(502, "object storage upload failed");
        }

        return url;
    }

    private String normalizeBaseUrl(String url) {
        if (url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        }
        return url;
    }

    private byte[] toJpeg(MultipartFile file) {
        try {
            String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
            if (contentType.contains("jpeg") || contentType.contains("jpg")) {
                return file.getBytes();
            }
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw new ApiException(400, "invalid image");
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            boolean ok = ImageIO.write(image, "jpg", baos);
            if (!ok) {
                throw new ApiException(400, "invalid image");
            }
            return baos.toByteArray();
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiException(400, "invalid image");
        }
    }
}
