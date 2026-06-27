package com.app.venus.modules.review.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import com.app.venus.modules.order.domain.Order;
import com.app.venus.modules.order.infrastructure.OrderRepository;
import com.app.venus.modules.provider.domain.Station;
import com.app.venus.modules.provider.infrastructure.StationRepository;
import com.app.venus.modules.review.domain.Review;
import com.app.venus.modules.user.domain.User;
import com.app.venus.modules.user.infrastructure.UserRepository;
import com.app.venus.shared.domain.Amenity;
import com.app.venus.shared.domain.ConnectorType;
import com.app.venus.shared.domain.Role;

import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
class ReviewRepositoryTests {
    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private User author;
    private User provider;
    private Station station;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();
        orderRepository.deleteAll();
        stationRepository.deleteAll();

        author = userRepository.findById("usr_review_author")
                .orElseGet(() -> userRepository.saveAndFlush(new User(
                        "usr_review_author",
                        "Review Author",
                        "review-author@volzen.test",
                        Role.DRIVER,
                        null)));
        provider = userRepository.findById("usr_review_provider")
                .orElseGet(() -> userRepository.saveAndFlush(new User(
                        "usr_review_provider",
                        "Review Provider",
                        "review-provider@volzen.test",
                        Role.PROVIDER,
                        null)));
        station = stationRepository.saveAndFlush(new Station(
                "pvd_review_station",
                provider,
                "99 Le Loi, District 1, Ho Chi Minh City",
                new BigDecimal("10.7750000"),
                new BigDecimal("106.7010000"),
                22000,
                Set.of(ConnectorType.CCS),
                Set.of(Amenity.RESTROOM),
                List.of("https://cdn.volzen.vn/stations/pvd_review_station/photo_1.jpg"),
                true));
    }

    @Test
    void findsReviewsAndAggregatesRatingForProviderStation() {
        Order firstOrder = persistOrder("ord_review_1");
        Order secondOrder = persistOrder("ord_review_2");
        reviewRepository.save(new Review("rev_1", firstOrder, station, author, 5, "Great charger."));
        reviewRepository.saveAndFlush(new Review("rev_2", secondOrder, station, author, 3, "Good enough."));

        assertThat(reviewRepository.findByProviderStationOrderByCreatedInstantDesc(station)).hasSize(2);
        assertThat(reviewRepository.countByProviderStation(station)).isEqualTo(2);
        assertThat(reviewRepository.averageRatingByProviderStation(station)).isEqualTo(4.0);
        assertThat(reviewRepository.findByOrder(firstOrder)).isPresent();
    }

    @Test
    void enforcesOneReviewPerOrder() {
        Order order = persistOrder("ord_review_unique");
        reviewRepository.saveAndFlush(new Review("rev_unique_1", order, station, author, 5, "Great charger."));

        assertThatThrownBy(() -> {
            reviewRepository.saveAndFlush(new Review("rev_unique_2", order, station, author, 4, "Still good."));
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    private Order persistOrder(String id) {
        Order order = new Order(id);
        entityManager.persist(order);
        entityManager.flush();
        return order;
    }
}
