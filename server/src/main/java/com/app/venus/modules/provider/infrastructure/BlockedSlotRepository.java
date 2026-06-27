package com.app.venus.modules.provider.infrastructure;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.app.venus.modules.provider.domain.BlockedSlot;

public interface BlockedSlotRepository extends JpaRepository<BlockedSlot, String> {
    Optional<BlockedSlot> findByIdAndStationProviderId(String id, String providerId);

    List<BlockedSlot> findByStationIdOrderByStartTimeAsc(String stationId);

    @Query("""
            select blockedSlot
            from BlockedSlot blockedSlot
            where blockedSlot.station.id = :stationId
              and blockedSlot.startTime < :endExclusive
              and blockedSlot.endTime > :startInclusive
            order by blockedSlot.startTime asc
            """)
    List<BlockedSlot> findOverlappingSlots(
            @Param("stationId") String stationId,
            @Param("startInclusive") OffsetDateTime startInclusive,
            @Param("endExclusive") OffsetDateTime endExclusive);

    @Query("""
            select count(blockedSlot) > 0
            from BlockedSlot blockedSlot
            where blockedSlot.station.id = :stationId
              and blockedSlot.startTime < :endTime
              and blockedSlot.endTime > :startTime
            """)
    boolean existsOverlappingSlot(
            @Param("stationId") String stationId,
            @Param("startTime") OffsetDateTime startTime,
            @Param("endTime") OffsetDateTime endTime);
}
