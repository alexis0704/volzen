package com.app.venus.shared.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import com.app.venus.modules.order.infrastructure.OrderRepository;
import com.app.venus.modules.provider.infrastructure.BlockedSlotRepository;
import com.app.venus.modules.provider.infrastructure.StationRepository;
import com.app.venus.modules.review.infrastructure.ReviewRepository;
import com.app.venus.modules.user.infrastructure.UserRepository;
import com.app.venus.modules.vehicle.infrastructure.VehicleRepository;

@SpringBootTest(properties = "app.seed.demo-data=true")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class DemoDataSeederTests {
    @Autowired
    private DemoDataSeeder demoDataSeeder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private BlockedSlotRepository blockedSlotRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Test
    void seedingIsIdempotent() throws Exception {
        long users = userRepository.count();
        long vehicles = vehicleRepository.count();
        long stations = stationRepository.count();
        long blockedSlots = blockedSlotRepository.count();
        long orders = orderRepository.count();
        long reviews = reviewRepository.count();

        demoDataSeeder.run();

        assertThat(userRepository.count()).isEqualTo(users);
        assertThat(vehicleRepository.count()).isEqualTo(vehicles);
        assertThat(stationRepository.count()).isEqualTo(stations);
        assertThat(blockedSlotRepository.count()).isEqualTo(blockedSlots);
        assertThat(orderRepository.count()).isEqualTo(orders);
        assertThat(reviewRepository.count()).isEqualTo(reviews);
        assertThat(userRepository.existsById("usr_demo_driver")).isTrue();
        assertThat(vehicleRepository.existsById("veh_demo_vf8")).isTrue();
        assertThat(stationRepository.existsById("pvd_p1")).isTrue();
        assertThat(blockedSlotRepository.existsById("blk_demo_p1_maintenance")).isTrue();
        assertThat(orderRepository.existsById("ord_demo_completed_1")).isTrue();
        assertThat(reviewRepository.existsById("rev_demo_p1")).isTrue();
    }
}
