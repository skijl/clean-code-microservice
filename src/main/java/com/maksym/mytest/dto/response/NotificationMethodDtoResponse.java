package com.maksym.mytest.dto.response;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class NotificationMethodDtoResponse {

    private Long id;

    private String messageTitle;

    private Double cost;

    private String userId;

    private BigDecimal price;

    private NotificationChannelDtoResponse notificationChannel;

    private NotificationDtoResponse notification;
}
