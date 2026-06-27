package com.app.venus.modules.provider.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.app.venus.modules.provider.domain.BlockReason;
import com.app.venus.modules.provider.domain.BlockedSlot;
import com.app.venus.modules.provider.domain.Station;
import com.app.venus.modules.user.domain.User;
import com.app.venus.modules.user.infrastructure.UserRepository;
import com.app.venus.shared.domain.Amenity;
import com.app.venus.shared.domain.ConnectorType;
import com.app.venus.shared.domain.Role;

@SpringBootTest
@Transactional
class BlockedSlotRepositoryTests {
    @Autowired
    private BlockedSlotRepository blockedSlotRepository;

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private UserRepository userRepository;

    private Station station;

    @BeforeEach
    void setUp() {
        blockedSlotRepository.deleteAll();
        stationRepository.deleteAll();
        User provider = userRepository.findById("usr_block_provider")
                .orElseGet(() -> userRepository.saveAndFlush(new User(
                        "usr_block_provider",
                        "Block Provider",
                        "block-provider@volzen.test",
                        Role.PROVIDER,
                        null)));
        station = stationRepository.saveAndFlush(new Station(
                "pvd_block_station",
                provider,
                "Blocked Slot Station",
                "12 Nguyen Hue",
                new BigDecimal("10.7769000"),
                new BigDecimal("106.7009000"),
                25000,
                Set.of(ConnectorType.CCS),
                Set.of(Amenity.WIFI),
                List.of(),
                true));
    }

    @Test
    void persistsAndFindsOverlappingBlockedSlots() {
        blockedSlotRepository.saveAndFlush(new BlockedSlot(
                "blk_test",
                station,
                OffsetDateTime.parse("2026-06-29T10:00:00+07:00"),
                OffsetDateTime.parse("2026-06-29T12:00:00+07:00"),
                BlockReason.MAINTENANCE));

        assertThat(blockedSlotRepository.findByIdAndStationProviderId("blk_test", station.getProvider().getId())).isPresent();
        assertThat(blockedSlotRepository.existsOverlappingSlot(
                station.getId(),
                OffsetDateTime.parse("2026-06-29T11:00:00+07:00"),
                OffsetDateTime.parse("2026-06-29T13:00:00+07:00"))).isTrue();
        assertThat(blockedSlotRepository.existsOverlappingSlot(
                station.getId(),
                OffsetDateTime.parse("2026-06-29T12:00:00+07:00"),
                OffsetDateTime.parse("2026-06-29T13:00:00+07:00"))).isFalse();
        assertThat(blockedSlotRepository.findOverlappingSlots(
                station.getId(),
                OffsetDateTime.parse("2026-06-29T00:00:00+07:00"),
                OffsetDateTime.parse("2026-06-30T00:00:00+07:00"))).hasSize(1);
    }
}
