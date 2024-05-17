package com.maksym.mytest.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class NotificationModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @Column(name = "message")
    private String message;

    @Column(name = "data")
    private BigDecimal myData;

    @Column(name = "userId")
    private String userId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "price")
    private BigDecimal price;

    @OneToMany(mappedBy = "notification")
    private List<NotificationChannel> notificationChannels;
}
