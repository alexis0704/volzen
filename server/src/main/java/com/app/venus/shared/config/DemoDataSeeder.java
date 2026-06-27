package com.app.venus.shared.config;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.app.venus.modules.order.domain.Order;
import com.app.venus.modules.order.infrastructure.OrderRepository;
import com.app.venus.modules.provider.domain.Station;
import com.app.venus.modules.provider.infrastructure.StationRepository;
import com.app.venus.modules.review.domain.Review;
import com.app.venus.modules.review.infrastructure.ReviewRepository;
import com.app.venus.modules.user.application.DemoCurrentUserService;
import com.app.venus.modules.user.domain.User;
import com.app.venus.modules.user.infrastructure.UserRepository;
import com.app.venus.modules.vehicle.domain.Vehicle;
import com.app.venus.modules.vehicle.infrastructure.VehicleRepository;
import com.app.venus.shared.domain.Amenity;
import com.app.venus.shared.domain.ConnectorType;
import com.app.venus.shared.domain.OrderStatus;
import com.app.venus.shared.domain.Role;

@Component
@ConditionalOnProperty(name = "app.seed.demo-data", havingValue = "true")
public class DemoDataSeeder implements CommandLineRunner {
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final StationRepository stationRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;

    public DemoDataSeeder(
            UserRepository userRepository,
            VehicleRepository vehicleRepository,
            StationRepository stationRepository,
            OrderRepository orderRepository,
            ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
        this.stationRepository = stationRepository;
        this.orderRepository = orderRepository;
        this.reviewRepository = reviewRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        User driver = user("usr_demo_driver", "Demo Driver", "driver@volzen.test", Role.DRIVER,
                "https://cdn.volzen.vn/avatars/usr_demo_driver.jpg");
        User providerOne = user("usr_provider_p1", "Minh Tuan", "p1@volzen.test", Role.PROVIDER,
                "https://cdn.volzen.vn/avatars/pvd_p1.jpg");
        User providerTwo = user("usr_provider_p2", "Linh Tran", "p2@volzen.test", Role.PROVIDER,
                "https://cdn.volzen.vn/avatars/pvd_p2.jpg");
        User providerThree = user("usr_provider_p3", "An Nguyen", "p3@volzen.test", Role.PROVIDER,
                "https://cdn.volzen.vn/avatars/pvd_p3.jpg");

        Vehicle vehicle = vehicle("veh_demo_vf8", driver, "VinFast", "VF8", 2024, ConnectorType.CCS, true);
        Vehicle typeTwoVehicle = vehicle("veh_demo_model3", driver, "Tesla", "Model 3", 2023, ConnectorType.TYPE_2, false);

        Station stationOne = station(
                "pvd_p1",
                providerOne,
                "12 Nguyen Hue, District 1, Ho Chi Minh City",
                "10.7769000",
                "106.7009000",
                25000,
                Set.of(ConnectorType.CCS, ConnectorType.TYPE_2),
                Set.of(Amenity.COFFEE, Amenity.WIFI, Amenity.PARKING),
                List.of(
                        "https://cdn.volzen.vn/stations/pvd_p1/photo_1.jpg",
                        "https://cdn.volzen.vn/stations/pvd_p1/photo_2.jpg"),
                true);
        Station stationTwo = station(
                "pvd_p2",
                providerTwo,
                "48 Le Loi, District 1, Ho Chi Minh City",
                "10.7731000",
                "106.7002000",
                22000,
                Set.of(ConnectorType.TYPE_2),
                Set.of(Amenity.WIFI, Amenity.RESTROOM),
                List.of("https://cdn.volzen.vn/stations/pvd_p2/photo_1.jpg"),
                true);
        Station stationThree = station(
                "pvd_p3",
                providerThree,
                "88 Xa Lo Ha Noi, Thu Duc City, Ho Chi Minh City",
                "10.8024000",
                "106.7147000",
                30000,
                Set.of(ConnectorType.CCS, ConnectorType.CHADEMO),
                Set.of(Amenity.PARKING, Amenity.SECURITY),
                List.of("https://cdn.volzen.vn/stations/pvd_p3/photo_1.jpg"),
                true);

        Order confirmed = order(
                "ord_demo_confirmed",
                stationOne,
                vehicle,
                driver,
                "2026-06-28T09:00:00+07:00",
                "2026-06-28T11:00:00+07:00",
                25000,
                OrderStatus.CONFIRMED);
        Order completedOne = order(
                "ord_demo_completed_1",
                stationOne,
                vehicle,
                driver,
                "2026-06-20T09:00:00+07:00",
                "2026-06-20T11:00:00+07:00",
                25000,
                OrderStatus.COMPLETED);
        Order completedTwo = order(
                "ord_demo_completed_2",
                stationThree,
                vehicle,
                driver,
                "2026-06-21T14:00:00+07:00",
                "2026-06-21T15:30:00+07:00",
                30000,
                OrderStatus.COMPLETED);
        order(
                "ord_demo_reviewable",
                stationTwo,
                typeTwoVehicle,
                driver,
                "2026-06-22T16:00:00+07:00",
                "2026-06-22T17:00:00+07:00",
                22000,
                OrderStatus.COMPLETED);

        review("rev_demo_p1", completedOne, stationOne, driver, 5, "Great host, fast charger, highly recommend.");
        review("rev_demo_p3", completedTwo, stationThree, driver, 4, "Easy to find and reliable charging.");
        order(
                "ord_demo_upcoming",
                stationTwo,
                typeTwoVehicle,
                driver,
                "2026-06-29T13:00:00+07:00",
                "2026-06-29T14:00:00+07:00",
                22000,
                OrderStatus.CONFIRMED);

        orderRepository.save(confirmed);
    }

    private User user(String id, String name, String email, Role role, String avatarUrl) {
        return userRepository.findById(id)
                .orElseGet(() -> userRepository.save(new User(id, name, email, role, avatarUrl)));
    }

    private Vehicle vehicle(
            String id,
            User driver,
            String brand,
            String model,
            int year,
            ConnectorType connectorType,
            boolean defaultVehicle) {
        return vehicleRepository.findById(id)
                .orElseGet(() -> vehicleRepository.save(new Vehicle(
                        id,
                        driver,
                        brand,
                        model,
                        year,
                        connectorType,
                        defaultVehicle)));
    }

    private Station station(
            String id,
            User provider,
            String address,
            String lat,
            String lng,
            int pricePerHour,
            Set<ConnectorType> connectorTypes,
            Set<Amenity> amenities,
            List<String> photoUrls,
            boolean available) {
        return stationRepository.findById(id)
                .orElseGet(() -> stationRepository.save(new Station(
                        id,
                        provider,
                        address,
                        new BigDecimal(lat),
                        new BigDecimal(lng),
                        pricePerHour,
                        connectorTypes,
                        amenities,
                        photoUrls,
                        available)));
    }

    private Order order(
            String id,
            Station station,
            Vehicle vehicle,
            User driver,
            String start,
            String end,
            int pricePerHour,
            OrderStatus status) {
        return orderRepository.findById(id)
                .orElseGet(() -> orderRepository.save(new Order(
                        id,
                        station,
                        vehicle,
                        driver,
                        OffsetDateTime.parse(start),
                        OffsetDateTime.parse(end),
                        duration(start, end),
                        pricePerHour,
                        subtotal(start, end, pricePerHour),
                        serviceFee(start, end, pricePerHour),
                        subtotal(start, end, pricePerHour) + serviceFee(start, end, pricePerHour),
                        status)));
    }

    private void review(String id, Order order, Station station, User author, int rating, String comment) {
        if (reviewRepository.existsById(id)) {
            return;
        }
        reviewRepository.save(new Review(id, order, station, author, rating, comment));
    }

    private BigDecimal duration(String start, String end) {
        long minutes = java.time.Duration.between(OffsetDateTime.parse(start), OffsetDateTime.parse(end)).toMinutes();
        return new BigDecimal(minutes).divide(new BigDecimal("60"), 2, java.math.RoundingMode.HALF_UP);
    }

    private int subtotal(String start, String end, int pricePerHour) {
        return duration(start, end)
                .multiply(new BigDecimal(pricePerHour))
                .setScale(0, java.math.RoundingMode.HALF_UP)
                .intValueExact();
    }

    private int serviceFee(String start, String end, int pricePerHour) {
        return new BigDecimal(subtotal(start, end, pricePerHour))
                .multiply(new BigDecimal("0.10"))
                .setScale(0, java.math.RoundingMode.HALF_UP)
                .intValueExact();
    }
}
