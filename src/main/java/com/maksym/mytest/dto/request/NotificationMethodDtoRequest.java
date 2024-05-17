package com.maksym.mytest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class NotificationMethodDtoRequest {

    @NotNull(message = "Message Title cannot be null")
    @NotBlank(message = "Message Title cannot be blank")
    private String messageTitle;

    @Positive(message = "Cost must be a positive number")
    @NotNull(message = "Cost cannot be null")
    private Double cost;

    @NotNull(message = "User Id cannot be null")
    @NotBlank(message = "User Id cannot be blank")
    private String userId;

    @Positive(message = "Price must be a positive number")
    @NotNull(message = "Price cannot be null")
    private BigDecimal price;

    @Positive(message = "Notification Channel Model must be a positive number")
    @NotNull(message = "Notification Channel Model cannot be null")
    private Long notificationChannelId;

    @Positive(message = "Notification Model must be a positive number")
    @NotNull(message = "Notification Model cannot be null")
    private Long notificationId;
}
