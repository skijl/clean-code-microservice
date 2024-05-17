package com.maksym.mytest.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class NotificationMethod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @Column(name = "title")
    private String messageTitle;

    @Column(name = "cost")
    private Double cost;

    @Column(name = "userId")
    private String userId;

    @Column(name = "price")
    private BigDecimal price;

    @ManyToOne
    private NotificationChannel notificationChannelModel;

    @ManyToOne
    private NotificationModel notificationModel;

}
