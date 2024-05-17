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
public class NotificationDtoRequest {

    @NotNull(message = "Message cannot be null")
    @NotBlank(message = "Message cannot be blank")
    private String message;

    @Positive(message = "My Data must be a positive number")
    @NotNull(message = "My Data cannot be null")
    private BigDecimal myData;

    @NotNull(message = "User Id cannot be null")
    @NotBlank(message = "User Id cannot be blank")
    private String userId;

    @Positive(message = "Price must be a positive number")
    @NotNull(message = "Price cannot be null")
    private BigDecimal price;
}
