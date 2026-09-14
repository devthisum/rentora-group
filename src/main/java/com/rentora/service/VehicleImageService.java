package com.rentora.service;

import com.rentora.dao.impl.VehicleImageDAOImpl;
import com.rentora.dao.interfaces.VehicleImageDAO;

import java.util.ArrayList;
import java.util.List;

/** Manages the extra gallery photos for a vehicle beyond its single cover image. */
public class VehicleImageService {

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
}
