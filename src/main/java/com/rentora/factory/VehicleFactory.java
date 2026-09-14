package com.rentora.factory;

import com.rentora.model.Vehicle;

/**
 * Factory Pattern.
 * Centralizes creation/defaulting logic for different vehicle categories,
 * so category-specific defaults (seat count assumptions, transmission
 * defaults, etc.) live in one place instead of scattered across controllers.
 */
public final class VehicleFactory {

    private VehicleFactory() { }

    public static Vehicle createVehicle(String categoryName) {
        Vehicle vehicle = new Vehicle();
        vehicle.setCategoryName(categoryName);

        switch (categoryName.toUpperCase()) {
            case "MOTORCYCLE":
                vehicle.setSeats(2);
                vehicle.setTransmission("MANUAL");
                break;
            case "THREEWHEELER":
                vehicle.setSeats(3);
                vehicle.setTransmission("MANUAL");
                break;
            case "BUS":
                vehicle.setSeats(40);
                vehicle.setTransmission("MANUAL");
                break;
            case "VAN":
                vehicle.setSeats(8);
                break;
            case "SUV":
                vehicle.setSeats(7);
                break;
            case "LUXURY":
            case "SPORTS":
                vehicle.setSeats(4);
                vehicle.setTransmission("AUTOMATIC");
                break;
            case "ELECTRIC VEHICLE":
                vehicle.setFuelType("ELECTRIC");
                vehicle.setSeats(5);
                break;
            case "CAR":
            default:
                vehicle.setSeats(5);
                break;
        }
        return vehicle;
    }
}
