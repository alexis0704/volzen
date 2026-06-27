package com.app.venus.modules.provider.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.app.venus.modules.user.domain.User;
import com.app.venus.shared.auditing.Auditable;
import com.app.venus.shared.domain.Amenity;
import com.app.venus.shared.domain.ConnectorType;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "station")
public class Station extends Auditable {
    @Id
    @Column(length = 40)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_id", nullable = false)
    private User provider;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal lat;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal lng;

    @Column(nullable = false)
    private int pricePerHour;

    @ElementCollection
    @CollectionTable(name = "station_connector_type", joinColumns = @JoinColumn(name = "station_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "connector_type", nullable = false)
    private Set<ConnectorType> connectorTypes = new LinkedHashSet<>();

    @ElementCollection
    @CollectionTable(name = "station_amenity", joinColumns = @JoinColumn(name = "station_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "amenity", nullable = false)
    private Set<Amenity> amenities = new LinkedHashSet<>();

    @ElementCollection
    @CollectionTable(name = "station_photo_url", joinColumns = @JoinColumn(name = "station_id"))
    @OrderColumn(name = "display_order")
    @Column(name = "photo_url", nullable = false, length = 500)
    private List<String> photoUrls = new ArrayList<>();

    @Column(nullable = false)
    private boolean available;

    protected Station() {
    }

    public Station(
            String id,
            User provider,
            String address,
            BigDecimal lat,
            BigDecimal lng,
            int pricePerHour,
            Set<ConnectorType> connectorTypes,
            Set<Amenity> amenities,
            List<String> photoUrls,
            boolean available) {
        this(
                id,
                provider,
                provider.getFullName(),
                address,
                lat,
                lng,
                pricePerHour,
                connectorTypes,
                amenities,
                photoUrls,
                available);
    }

    public Station(
            String id,
            User provider,
            String name,
            String address,
            BigDecimal lat,
            BigDecimal lng,
            int pricePerHour,
            Set<ConnectorType> connectorTypes,
            Set<Amenity> amenities,
            List<String> photoUrls,
            boolean available) {
        this.id = id;
        this.provider = provider;
        this.name = name;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        this.pricePerHour = pricePerHour;
        this.connectorTypes = new LinkedHashSet<>(connectorTypes);
        this.amenities = new LinkedHashSet<>(amenities);
        this.photoUrls = new ArrayList<>(photoUrls);
        this.available = available;
    }

    public String getId() {
        return id;
    }

    public User getProvider() {
        return provider;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public BigDecimal getLat() {
        return lat;
    }

    public BigDecimal getLng() {
        return lng;
    }

    public int getPricePerHour() {
        return pricePerHour;
    }

    public Set<ConnectorType> getConnectorTypes() {
        return Set.copyOf(connectorTypes);
    }

    public Set<Amenity> getAmenities() {
        return Set.copyOf(amenities);
    }

    public List<String> getPhotoUrls() {
        return List.copyOf(photoUrls);
    }

    public boolean isAvailable() {
        return available;
    }

    public void update(
            String name,
            String address,
            BigDecimal lat,
            BigDecimal lng,
            int pricePerHour,
            Set<ConnectorType> connectorTypes,
            Set<Amenity> amenities,
            List<String> photoUrls,
            boolean available) {
        this.name = name;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        this.pricePerHour = pricePerHour;
        this.connectorTypes = new LinkedHashSet<>(connectorTypes);
        this.amenities = new LinkedHashSet<>(amenities);
        this.photoUrls = new ArrayList<>(photoUrls);
        this.available = available;
    }
}
