package com.rentora.dao.interfaces;

import com.rentora.model.Promotion;
import java.util.List;
import java.util.Optional;

public interface PromotionDAO {
    long create(Promotion promotion) throws Exception;
    boolean update(Promotion promotion) throws Exception;
    boolean delete(long promotionId) throws Exception;
    Optional<Promotion> findById(long promotionId) throws Exception;
    List<Promotion> findAll() throws Exception;
    List<Promotion> findByVehicle(long vehicleId) throws Exception;

    /** The single currently-active, in-date-range promotion for a vehicle, if any (a vehicle should only have one running at a time). */
    Optional<Promotion> findActiveForVehicle(long vehicleId) throws Exception;

    /** Active, in-date-range promotions for every vehicle at once — used to badge a whole vehicle listing page in one query. */
    List<Promotion> findAllActive() throws Exception;
}
