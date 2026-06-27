package com.app.venus.modules.review.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.app.venus.modules.order.domain.Order;
import com.app.venus.modules.provider.domain.Station;
import com.app.venus.modules.review.domain.Review;

public interface ReviewRepository extends JpaRepository<Review, String> {
    List<Review> findByProviderStationOrderByCreatedInstantDesc(Station providerStation);

    long countByProviderStation(Station providerStation);

    @Query("select avg(review.rating) from Review review where review.providerStation = :providerStation")
    Double averageRatingByProviderStation(@Param("providerStation") Station providerStation);

    Optional<Review> findByOrder(Order order);

    boolean existsByOrder(Order order);
}
