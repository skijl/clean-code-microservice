package com.maksym.mytest.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class NotificationChannelDtoResponse {

    private Long id;

    private String messageTitle;

    private Long amount;

    private String userId;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")

    private LocalDateTime sentAt;

    private BigDecimal price;

    private NotificationDtoResponse notification;
}
