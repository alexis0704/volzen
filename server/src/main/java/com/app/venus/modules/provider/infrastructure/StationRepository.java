package com.app.venus.modules.provider.infrastructure;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.app.venus.modules.provider.domain.Station;
import com.app.venus.shared.domain.ConnectorType;

public interface StationRepository extends JpaRepository<Station, String> {
    Optional<Station> findByIdAndAvailableTrue(String id);

    Optional<Station> findByProviderId(String providerId);

    List<Station> findByAvailableTrue();

    @Query("""
            select distinct station
            from Station station
            where station.available = true
              and (:maxPricePerHour is null or station.pricePerHour <= :maxPricePerHour)
            """)
    List<Station> findSearchCandidates(
            @Param("maxPricePerHour") Integer maxPricePerHour);

    @Query("""
            select distinct station
            from Station station
            join station.connectorTypes connectorType
            where station.available = true
              and connectorType = :connectorType
              and (:maxPricePerHour is null or station.pricePerHour <= :maxPricePerHour)
            """)
    List<Station> findSearchCandidatesByConnectorType(
            @Param("connectorType") ConnectorType connectorType,
            @Param("maxPricePerHour") Integer maxPricePerHour);
}
