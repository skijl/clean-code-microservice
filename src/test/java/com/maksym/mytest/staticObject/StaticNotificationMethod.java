package com.maksym.mytest.staticObject;

import com.maksym.mytest.dto.request.NotificationMethodDtoRequest;
import com.maksym.mytest.dto.response.NotificationMethodDtoResponse;
import com.maksym.mytest.model.NotificationMethod;

import java.math.BigDecimal;

public class StaticNotificationMethod {

    public static final Long ID = 1L;

    public static NotificationMethod notificationMethod1() {
        NotificationMethod model = new NotificationMethod();
        model.setId(ID);
        model.setMessageTitle("messageTitle");
        model.setCost(10D);
        model.setUserId("userId");
        model.setPrice(new BigDecimal(10));
        model.setNotificationChannelModel(StaticNotificationChannel.notificationChannel1());
        model.setNotificationModel(StaticNotification.notification1());
        return model;
    }

    public static NotificationMethod notificationMethod2() {
        NotificationMethod model = new NotificationMethod();
        model.setId(ID);
        model.setMessageTitle("messageTitle");
        model.setCost(20D);
        model.setUserId("userId");
        model.setPrice(new BigDecimal(20));
        model.setNotificationChannelModel(StaticNotificationChannel.notificationChannel2());
        model.setNotificationModel(StaticNotification.notification2());
        return model;
    }

    public static NotificationMethodDtoRequest notificationMethodDtoRequest1() {
        NotificationMethodDtoRequest dtoRequest = new NotificationMethodDtoRequest();
        dtoRequest.setMessageTitle("messageTitle");
        dtoRequest.setCost(10D);
        dtoRequest.setUserId("userId");
        dtoRequest.setPrice(new BigDecimal(10));
        dtoRequest.setNotificationChannelId(1L);
        dtoRequest.setNotificationId(1L);
        return dtoRequest;
    }

    public static NotificationMethodDtoResponse notificationMethodDtoResponse1() {
        NotificationMethodDtoResponse dtoResponse = new NotificationMethodDtoResponse();
        dtoResponse.setId(ID);
        dtoResponse.setMessageTitle("messageTitle");
        dtoResponse.setCost(10D);
        dtoResponse.setUserId("userId");
        dtoResponse.setPrice(new BigDecimal(10));
        dtoResponse.setNotificationChannel(StaticNotificationChannel.notificationChannelDtoResponse1());
        dtoResponse.setNotification(StaticNotification.notificationDtoResponse1());
        return dtoResponse;
    }

    public static NotificationMethodDtoResponse notificationMethodDtoResponse2() {
        NotificationMethodDtoResponse dtoResponse = new NotificationMethodDtoResponse();
        dtoResponse.setId(ID);
        dtoResponse.setMessageTitle("messageTitle");
        dtoResponse.setCost(20D);
        dtoResponse.setUserId("userId");
        dtoResponse.setPrice(new BigDecimal(20));
        dtoResponse.setNotificationChannel(StaticNotificationChannel.notificationChannelDtoResponse1());
        dtoResponse.setNotification(StaticNotification.notificationDtoResponse1());
        return dtoResponse;
    }
}
