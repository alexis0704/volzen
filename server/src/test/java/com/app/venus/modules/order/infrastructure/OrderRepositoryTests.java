package com.app.venus.modules.order.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.app.venus.modules.order.domain.Order;
import com.app.venus.modules.provider.domain.Station;
import com.app.venus.modules.provider.infrastructure.StationRepository;
import com.app.venus.modules.user.domain.User;
import com.app.venus.modules.user.infrastructure.UserRepository;
import com.app.venus.modules.vehicle.domain.Vehicle;
import com.app.venus.modules.vehicle.infrastructure.VehicleRepository;
import com.app.venus.shared.domain.Amenity;
import com.app.venus.shared.domain.ConnectorType;
import com.app.venus.shared.domain.OrderStatus;
import com.app.venus.shared.domain.Role;

@SpringBootTest
@Transactional
class OrderRepositoryTests {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private UserRepository userRepository;

    private User driver;
    private Station station;
    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        vehicleRepository.deleteAll();
        stationRepository.deleteAll();

        driver = userRepository.findById("usr_order_driver")
                .orElseGet(() -> userRepository.saveAndFlush(new User(
                        "usr_order_driver",
                        "Order Driver",
                        "order-driver@volzen.test",
                        Role.DRIVER,
                        null)));
        User provider = userRepository.findById("usr_order_provider")
                .orElseGet(() -> userRepository.saveAndFlush(new User(
                        "usr_order_provider",
                        "Order Provider",
                        "order-provider@volzen.test",
                        Role.PROVIDER,
                        null)));
        station = stationRepository.saveAndFlush(new Station(
                "pvd_order_station",
                provider,
                "12 Nguyen Hue, District 1, Ho Chi Minh City",
                new BigDecimal("10.7769000"),
                new BigDecimal("106.7009000"),
                25000,
                Set.of(ConnectorType.CCS),
                Set.of(Amenity.WIFI),
                List.of("https://cdn.volzen.vn/stations/pvd_order_station/photo_1.jpg"),
                true));
        vehicle = vehicleRepository.saveAndFlush(new Vehicle(
                "veh_order_driver",
                driver,
                "VinFast",
                "VF8",
                2024,
                ConnectorType.CCS,
                true));
    }

    @Test
    void detectsOverlappingStationOrders() {
        orderRepository.saveAndFlush(order(
                "ord_overlap",
                "2026-06-28T09:00:00+07:00",
                "2026-06-28T11:00:00+07:00",
                OrderStatus.CONFIRMED));

        boolean overlaps = orderRepository.existsOverlappingStationOrder(
                station.getId(),
                OffsetDateTime.parse("2026-06-28T10:30:00+07:00"),
                OffsetDateTime.parse("2026-06-28T12:00:00+07:00"),
                EnumSet.of(OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.ACTIVE));
        boolean adjacent = orderRepository.existsOverlappingStationOrder(
                station.getId(),
                OffsetDateTime.parse("2026-06-28T11:00:00+07:00"),
                OffsetDateTime.parse("2026-06-28T12:00:00+07:00"),
                EnumSet.of(OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.ACTIVE));

        assertThat(overlaps).isTrue();
        assertThat(adjacent).isFalse();
    }

    @Test
    void findsBookedSlotsForDateAndExcludesCancelled() {
        Order confirmed = orderRepository.save(order(
                "ord_confirmed_slot",
                "2026-06-28T09:00:00+07:00",
                "2026-06-28T11:00:00+07:00",
                OrderStatus.CONFIRMED));
        orderRepository.save(order(
                "ord_cancelled_slot",
                "2026-06-28T13:00:00+07:00",
                "2026-06-28T14:00:00+07:00",
                OrderStatus.CANCELLED));
        orderRepository.flush();

        List<Order> bookedSlots = orderRepository.findBookedSlotsForStationDate(
                station.getId(),
                OffsetDateTime.parse("2026-06-28T00:00:00+07:00"),
                OffsetDateTime.parse("2026-06-29T00:00:00+07:00"),
                OrderStatus.CANCELLED);

        assertThat(bookedSlots).extracting(Order::getId).containsExactly(confirmed.getId());
    }

    private Order order(String id, String start, String end, OrderStatus status) {
        return new Order(
                id,
                station,
                vehicle,
                driver,
                OffsetDateTime.parse(start),
                OffsetDateTime.parse(end),
                new BigDecimal("2.00"),
                25000,
                50000,
                5000,
                55000,
                status);
    }
}
