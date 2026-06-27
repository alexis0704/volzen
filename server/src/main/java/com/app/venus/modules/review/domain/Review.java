package com.app.venus.modules.review.domain;

import com.app.venus.modules.order.domain.Order;
import com.app.venus.modules.provider.domain.Station;
import com.app.venus.modules.user.domain.User;
import com.app.venus.shared.auditing.Auditable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "review", uniqueConstraints = @UniqueConstraint(name = "uk_review_order", columnNames = "order_id"))
public class Review extends Auditable {
    @Id
    @Column(length = 40)
    private String id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "station_id", nullable = false)
    private Station providerStation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false)
    private int rating;

    @Column(nullable = false, length = 1000)
    private String comment;

    protected Review() {
    }

    public Review(String id, Order order, Station providerStation, User author, int rating, String comment) {
        this.id = id;
        this.order = order;
        this.providerStation = providerStation;
        this.author = author;
        this.rating = rating;
        this.comment = comment;
    }

    public String getId() {
        return id;
    }

    public Order getOrder() {
        return order;
    }

    public Station getProviderStation() {
        return providerStation;
    }

    public User getAuthor() {
        return author;
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }
}
