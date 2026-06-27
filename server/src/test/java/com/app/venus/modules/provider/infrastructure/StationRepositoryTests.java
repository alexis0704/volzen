package com.app.venus.modules.provider.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.app.venus.modules.provider.application.StationDistanceCalculator;
import com.app.venus.modules.provider.domain.Station;
import com.app.venus.modules.order.infrastructure.OrderRepository;
import com.app.venus.modules.user.domain.User;
import com.app.venus.modules.user.infrastructure.UserRepository;
import com.app.venus.shared.domain.Amenity;
import com.app.venus.shared.domain.ConnectorType;
import com.app.venus.shared.domain.Role;

@SpringBootTest
@Transactional
class StationRepositoryTests {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StationDistanceCalculator distanceCalculator;

    private User provider;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        stationRepository.deleteAll();
        provider = userRepository.findById("usr_station_provider")
                .orElseGet(() -> userRepository.saveAndFlush(new User(
                        "usr_station_provider",
                        "Station Provider",
                        "station-provider@volzen.test",
                        Role.PROVIDER,
                        null)));
    }

    @Test
    void persistsStationCollectionsAndFindsSearchCandidates() {
        Station station = new Station(
                "pvd_station_test",
                provider,
                "12 Nguyen Hue, District 1, Ho Chi Minh City",
                new BigDecimal("10.7769000"),
                new BigDecimal("106.7009000"),
                25000,
                Set.of(ConnectorType.CCS, ConnectorType.TYPE_2),
                Set.of(Amenity.COFFEE, Amenity.WIFI),
                List.of("https://cdn.volzen.vn/stations/pvd_station_test/photo_1.jpg"),
                true);
        stationRepository.saveAndFlush(station);

        List<Station> candidates = stationRepository.findSearchCandidates(ConnectorType.CCS, 30000);

        assertThat(candidates).extracting(Station::getId).contains("pvd_station_test");
        Station persisted = stationRepository.findByIdAndAvailableTrue("pvd_station_test").orElseThrow();
        assertThat(persisted.getConnectorTypes()).containsExactlyInAnyOrder(ConnectorType.CCS, ConnectorType.TYPE_2);
        assertThat(persisted.getAmenities()).containsExactlyInAnyOrder(Amenity.COFFEE, Amenity.WIFI);
        assertThat(persisted.getPhotoUrls()).containsExactly("https://cdn.volzen.vn/stations/pvd_station_test/photo_1.jpg");
    }

    @Test
    void calculatesDistanceInKilometers() {
        double distance = distanceCalculator.distanceKm(
                new BigDecimal("10.7769000"),
                new BigDecimal("106.7009000"),
                new BigDecimal("10.7798000"),
                new BigDecimal("106.6990000"));

        assertThat(distance).isBetween(0.3, 0.5);
    }
}
