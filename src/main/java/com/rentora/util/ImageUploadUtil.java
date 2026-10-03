package com.rentora.util;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Saves an uploaded photo (multipart file) into a given webapp/assets/uploads/... subfolder
 * and returns the URL the browser can use to display it. If no file was chosen,
 * returns null so the caller can fall back to a plain URL text field instead.
 */
public final class ImageUploadUtil {

    private ImageUploadUtil() { }

    /** @param subfolder e.g. "vehicles" or "profiles" — saved under assets/uploads/{subfolder}/ */
    public static String saveIfPresent(Part filePart, ServletContext context, String contextPath, String subfolder) throws IOException {
        if (filePart == null || filePart.getSize() == 0) {
            return null;
        }
        String originalName = filePart.getSubmittedFileName();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf('.'));
        }
        String fileName = UUID.randomUUID() + extension;

        String uploadSubpath = "assets/uploads/" + subfolder;
        String realUploadDir = context.getRealPath("/" + uploadSubpath);
        Path uploadDir = Paths.get(realUploadDir);
        Files.createDirectories(uploadDir);

        try (InputStream in = filePart.getInputStream()) {
            Files.copy(in, uploadDir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        }

        return contextPath + "/" + uploadSubpath + "/" + fileName;
    }

    private static final Set<String> IMAGE_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".webp", ".gif");

    /**
     * Saves every non-empty image part named {@code fieldName} (a multi-file input) and
     * returns their URLs. Non-image files are skipped rather than stored.
     */
    public static List<String> saveAll(Collection<Part> parts, String fieldName, ServletContext context,
                                       String contextPath, String subfolder) throws IOException {
        List<String> urls = new ArrayList<>();
        for (Part part : parts) {
            if (!fieldName.equals(part.getName()) || part.getSize() == 0) continue;
            String name = part.getSubmittedFileName() == null ? "" : part.getSubmittedFileName().toLowerCase();
            String ct = part.getContentType() == null ? "" : part.getContentType();
            boolean okExt = IMAGE_EXTENSIONS.stream().anyMatch(name::endsWith);
            if (!ct.startsWith("image/") || !okExt) continue;
            String url = saveIfPresent(part, context, contextPath, subfolder);
            if (url != null) urls.add(url);
        }
        return urls;
    }
}
