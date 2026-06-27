package com.app.venus.modules.order.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.app.venus.modules.order.domain.Order;
import com.app.venus.modules.order.infrastructure.OrderRepository;
import com.app.venus.modules.provider.domain.Station;
import com.app.venus.modules.provider.infrastructure.StationRepository;
import com.app.venus.modules.user.application.DemoCurrentUserService;
import com.app.venus.modules.user.domain.User;
import com.app.venus.modules.user.infrastructure.UserRepository;
import com.app.venus.modules.vehicle.domain.Vehicle;
import com.app.venus.modules.vehicle.infrastructure.VehicleRepository;
import com.app.venus.shared.domain.Amenity;
import com.app.venus.shared.domain.ConnectorType;
import com.app.venus.shared.domain.OrderStatus;
import com.app.venus.shared.domain.Role;
import com.app.venus.shared.exception.InvalidStatusTransitionException;
import com.app.venus.shared.exception.NotFoundException;

@SpringBootTest
@Transactional
class HostOrderServiceTests {
    @Autowired
    private HostOrderService hostOrderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private UserRepository userRepository;

    private User driver;
    private Station hostStation;
    private Station otherStation;
    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        vehicleRepository.deleteAll();
        stationRepository.deleteAll();

        User provider = userRepository.findById(DemoCurrentUserService.DEMO_PROVIDER_ID)
                .orElseGet(() -> userRepository.saveAndFlush(new User(
                        DemoCurrentUserService.DEMO_PROVIDER_ID,
                        "Minh Tuan",
                        "p1@volzen.test",
                        Role.PROVIDER,
                        null)));
        User otherProvider = userRepository.findById("usr_host_order_other_provider")
                .orElseGet(() -> userRepository.saveAndFlush(new User(
                        "usr_host_order_other_provider",
                        "Other Provider",
                        "other-provider@volzen.test",
                        Role.PROVIDER,
                        null)));
        driver = userRepository.findById("usr_host_order_driver")
                .orElseGet(() -> userRepository.saveAndFlush(new User(
                        "usr_host_order_driver",
                        "Host Order Driver",
                        "host-order-driver@volzen.test",
                        Role.DRIVER,
                        null)));
        hostStation = stationRepository.saveAndFlush(station("pvd_host_orders", provider));
        otherStation = stationRepository.saveAndFlush(station("pvd_host_orders_other", otherProvider));
        vehicle = vehicleRepository.saveAndFlush(new Vehicle(
                "veh_host_order",
                driver,
                "VinFast",
                "VF8",
                2024,
                ConnectorType.CCS,
                true));
    }

    @Test
    void listsAndFiltersCurrentProviderOrdersOnly() {
        orderRepository.save(order("ord_host_pending", hostStation, OrderStatus.PENDING));
        orderRepository.save(order("ord_host_completed", hostStation, OrderStatus.COMPLETED));
        orderRepository.save(order("ord_other_provider", otherStation, OrderStatus.PENDING));
        orderRepository.flush();

        var all = hostOrderService.listCurrentProviderOrders(null, 20, 0);
        var pending = hostOrderService.listCurrentProviderOrders("pending", 20, 0);

        assertThat(all.total()).isEqualTo(2);
        assertThat(all.orders()).extracting(Order::getId).containsExactlyInAnyOrder("ord_host_pending", "ord_host_completed");
        assertThat(pending.total()).isEqualTo(1);
        assertThat(pending.orders()).extracting(Order::getId).containsExactly("ord_host_pending");
    }

    @Test
    void appliesValidProviderStatusTransitions() {
        orderRepository.saveAndFlush(order("ord_transition", hostStation, OrderStatus.PENDING));

        hostOrderService.updateCurrentProviderOrderStatus("ord_transition", "confirmed");
        assertThat(orderRepository.findById("ord_transition").orElseThrow().getStatus()).isEqualTo(OrderStatus.CONFIRMED);

        hostOrderService.updateCurrentProviderOrderStatus("ord_transition", "active");
        assertThat(orderRepository.findById("ord_transition").orElseThrow().getStatus()).isEqualTo(OrderStatus.ACTIVE);

        Order completed = hostOrderService.updateCurrentProviderOrderStatus("ord_transition", "completed");
        assertThat(completed.getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    void rejectsInvalidTransitionAndOtherProviderOrder() {
        orderRepository.saveAndFlush(order("ord_invalid_transition", hostStation, OrderStatus.COMPLETED));
        orderRepository.saveAndFlush(order("ord_not_owned", otherStation, OrderStatus.PENDING));

        assertThatThrownBy(() -> hostOrderService.updateCurrentProviderOrderStatus("ord_invalid_transition", "confirmed"))
                .isInstanceOf(InvalidStatusTransitionException.class);
        assertThatThrownBy(() -> hostOrderService.updateCurrentProviderOrderStatus("ord_not_owned", "confirmed"))
                .isInstanceOf(NotFoundException.class);
    }

    private Station station(String id, User provider) {
        return new Station(
                id,
                provider,
                "Host Order Station",
                "12 Nguyen Hue",
                new BigDecimal("10.7769000"),
                new BigDecimal("106.7009000"),
                25000,
                Set.of(ConnectorType.CCS),
                Set.of(Amenity.WIFI),
                List.of(),
                true);
    }

    private Order order(String id, Station station, OrderStatus status) {
        return new Order(
                id,
                station,
                vehicle,
                driver,
                OffsetDateTime.parse("2026-06-28T09:00:00+07:00"),
                OffsetDateTime.parse("2026-06-28T11:00:00+07:00"),
                new BigDecimal("2.00"),
                station.getPricePerHour(),
                50000,
                5000,
                55000,
                status);
    }
}
