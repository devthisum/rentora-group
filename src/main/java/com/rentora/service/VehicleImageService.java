package com.rentora.service;

import com.rentora.dao.impl.VehicleImageDAOImpl;
import com.rentora.dao.interfaces.VehicleImageDAO;

import com.rentora.exception.ValidationException;
import com.rentora.model.VehicleImage;

import java.util.ArrayList;
import java.util.List;

/** Manages the extra gallery photos for a vehicle beyond its single cover image. */
public class VehicleImageService {

    /** Cover + extras. Keeps the details-page gallery and the DB table a sensible size. */
    public static final int MAX_PHOTOS_PER_VEHICLE = 10;

    private final VehicleImageDAO vehicleImageDAO = new VehicleImageDAOImpl();

    /**
     * Saves the cover image plus any additional gallery URLs (one per line from
     * a textarea) into vehicle_images, so vehicle-details can render a gallery.
     */
    public void saveGallery(long vehicleId, String coverImageUrl, String additionalImagesRaw) throws Exception {
        if (coverImageUrl != null && !coverImageUrl.isBlank()) {
            vehicleImageDAO.addImage(vehicleId, coverImageUrl.trim(), true);
        }
        if (additionalImagesRaw == null || additionalImagesRaw.isBlank()) return;

        for (String line : additionalImagesRaw.split("\\r?\\n")) {
            String url = line.trim();
            if (!url.isEmpty()) {
                vehicleImageDAO.addImage(vehicleId, url, false);
            }
        }
    }

    public List<String> getGallery(long vehicleId) throws Exception {
        List<String> images = vehicleImageDAO.findByVehicle(vehicleId);
        return images != null ? images : new ArrayList<>();
    }

    /** Extra photos are saved as non-primary gallery rows (the cover is the primary row). */
    public void addExtraImages(long vehicleId, List<String> urls) throws Exception {
        if (urls == null || urls.isEmpty()) return;
        int existing = vehicleImageDAO.findRowsByVehicle(vehicleId).size();
        if (existing + urls.size() > MAX_PHOTOS_PER_VEHICLE) {
            throw new ValidationException("A vehicle can have at most " + MAX_PHOTOS_PER_VEHICLE
                    + " photos (including the cover). Remove some first.");
        }
        for (String url : urls) {
            if (url != null && !url.isBlank()) vehicleImageDAO.addImage(vehicleId, url.trim(), false);
        }
    }

    /** Re-points the primary row at the current cover — covers seeded vehicles that have no rows yet. */
    public void syncPrimary(long vehicleId, String coverImageUrl) throws Exception {
        vehicleImageDAO.deletePrimary(vehicleId);
        if (coverImageUrl != null && !coverImageUrl.isBlank()) {
            vehicleImageDAO.addImage(vehicleId, coverImageUrl.trim(), true);
        }
    }

    public void removeImages(long vehicleId, String[] imageIds) throws Exception {
        if (imageIds == null) return;
        for (String id : imageIds) {
            try { vehicleImageDAO.deleteImage(Long.parseLong(id), vehicleId); }
            catch (NumberFormatException ignored) { /* skip a malformed id */ }
        }
    }

    public List<VehicleImage> getGalleryRows(long vehicleId) throws Exception {
        return vehicleImageDAO.findRowsByVehicle(vehicleId);
    }

    /** Splits a textarea (one URL per line) into clean http(s) URLs. */
    public static List<String> parseUrls(String raw) {
        List<String> urls = new ArrayList<>();
        if (raw == null) return urls;
        for (String line : raw.split("\\r?\\n")) {
            String u = line.trim();
            if (u.startsWith("http://") || u.startsWith("https://")) urls.add(u);
        }
        return urls;
    }
}
