package com.app.venus.modules.order.infrastructure;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.app.venus.modules.order.domain.Order;
import com.app.venus.shared.domain.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, String> {
    Optional<Order> findByIdAndDriverId(String id, String driverId);

    List<Order> findByDriverIdOrderByCreatedInstantDesc(String driverId, Pageable pageable);

    List<Order> findByDriverIdAndStatusOrderByCreatedInstantDesc(
            String driverId,
            OrderStatus status,
            Pageable pageable);

    long countByDriverId(String driverId);

    long countByDriverIdAndStatus(String driverId, OrderStatus status);

    @Query("""
            select count(orderEntity) > 0
            from ChargingOrder orderEntity
            where orderEntity.providerStation.id = :stationId
              and orderEntity.status in :blockingStatuses
              and orderEntity.startTime < :endTime
              and orderEntity.endTime > :startTime
            """)
    boolean existsOverlappingStationOrder(
            @Param("stationId") String stationId,
            @Param("startTime") OffsetDateTime startTime,
            @Param("endTime") OffsetDateTime endTime,
            @Param("blockingStatuses") Collection<OrderStatus> blockingStatuses);

    @Query("""
            select orderEntity
            from ChargingOrder orderEntity
            where orderEntity.providerStation.id = :stationId
              and orderEntity.status <> :excludedStatus
              and orderEntity.startTime < :endExclusive
              and orderEntity.endTime > :startInclusive
            order by orderEntity.startTime asc
            """)
    List<Order> findBookedSlotsForStationDate(
            @Param("stationId") String stationId,
            @Param("startInclusive") OffsetDateTime startInclusive,
            @Param("endExclusive") OffsetDateTime endExclusive,
            @Param("excludedStatus") OrderStatus excludedStatus);
}
