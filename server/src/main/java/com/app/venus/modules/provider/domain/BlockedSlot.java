package com.app.venus.modules.provider.domain;

import java.time.OffsetDateTime;

import com.app.venus.shared.auditing.Auditable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "blocked_slot")
public class BlockedSlot extends Auditable {
    @Id
    @Column(length = 40)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @Column(nullable = false)
    private OffsetDateTime startTime;

    @Column(nullable = false)
    private OffsetDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private BlockReason reason;

    protected BlockedSlot() {
    }

    public BlockedSlot(String id, Station station, OffsetDateTime startTime, OffsetDateTime endTime, BlockReason reason) {
        this.id = id;
        this.station = station;
        this.startTime = startTime;
        this.endTime = endTime;
        this.reason = reason;
    }

    public String getId() {
        return id;
    }

    public Station getStation() {
        return station;
    }

    public OffsetDateTime getStartTime() {
        return startTime;
    }

    public OffsetDateTime getEndTime() {
        return endTime;
    }

    public BlockReason getReason() {
        return reason;
    }
}
