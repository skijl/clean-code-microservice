package com.maksym.mytest.staticObject;

import com.maksym.mytest.dto.request.NotificationChannelDtoRequest;
import com.maksym.mytest.dto.response.NotificationChannelDtoResponse;
import com.maksym.mytest.model.NotificationChannel;

import java.time.LocalDateTime;
import java.math.BigDecimal;

public class StaticNotificationChannel {

    public static final Long ID = 1L;

    public static NotificationChannel notificationChannel1() {
        NotificationChannel model = new NotificationChannel();
        model.setId(ID);
        model.setMessageTitle("messageTitle");
        model.setAmount(1L);
        model.setUserId("userId");
        model.setSentAt(LocalDateTime.MIN);
        model.setPrice(new BigDecimal(10));
        model.setNotification(StaticNotification.notification1());
        return model;
    }

    public static NotificationChannel notificationChannel2() {
        NotificationChannel model = new NotificationChannel();
        model.setId(ID);
        model.setMessageTitle("messageTitle");
        model.setAmount(2L);
        model.setUserId("userId");
        model.setSentAt(LocalDateTime.MIN);
        model.setPrice(new BigDecimal(20));
        model.setNotification(StaticNotification.notification2());
        return model;
    }

    public static NotificationChannelDtoRequest notificationChannelDtoRequest1() {
        NotificationChannelDtoRequest dtoRequest = new NotificationChannelDtoRequest();
        dtoRequest.setMessageTitle("messageTitle");
        dtoRequest.setAmount(1L);
        dtoRequest.setUserId("userId");
        dtoRequest.setPrice(new BigDecimal(10));
        dtoRequest.setNotificationId(1L);
        return dtoRequest;
    }

    public static NotificationChannelDtoResponse notificationChannelDtoResponse1() {
        NotificationChannelDtoResponse dtoResponse = new NotificationChannelDtoResponse();
        dtoResponse.setId(ID);
        dtoResponse.setMessageTitle("messageTitle");
        dtoResponse.setAmount(1L);
        dtoResponse.setUserId("userId");
        dtoResponse.setSentAt(LocalDateTime.MIN);
        dtoResponse.setPrice(new BigDecimal(10));
        dtoResponse.setNotification(StaticNotification.notificationDtoResponse1());
        return dtoResponse;
    }

    public static NotificationChannelDtoResponse notificationChannelDtoResponse2() {
        NotificationChannelDtoResponse dtoResponse = new NotificationChannelDtoResponse();
        dtoResponse.setId(ID);
        dtoResponse.setMessageTitle("messageTitle");
        dtoResponse.setAmount(2L);
        dtoResponse.setUserId("userId");
        dtoResponse.setSentAt(LocalDateTime.MIN);
        dtoResponse.setPrice(new BigDecimal(20));
        dtoResponse.setNotification(StaticNotification.notificationDtoResponse1());
        return dtoResponse;
    }
}
