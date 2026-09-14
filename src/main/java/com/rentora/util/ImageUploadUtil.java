package com.rentora.util;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
}
