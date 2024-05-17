package com.maksym.mytest.staticObject;

import com.maksym.mytest.dto.request.NotificationDtoRequest;
import com.maksym.mytest.dto.response.NotificationDtoResponse;
import com.maksym.mytest.model.NotificationModel;

import java.time.LocalDateTime;
import java.math.BigDecimal;

public class StaticNotification {

    public static final Long ID = 1L;

    public static NotificationModel notification1() {
        NotificationModel model = new NotificationModel();
        model.setId(ID);
        model.setMessage("message");
        model.setMyData(new BigDecimal(10));
        model.setUserId("userId");
        model.setCreatedAt(LocalDateTime.MIN);
        model.setPrice(new BigDecimal(10));
        return model;
    }

    public static NotificationModel notification2() {
        NotificationModel model = new NotificationModel();
        model.setId(ID);
        model.setMessage("message");
        model.setMyData(new BigDecimal(20));
        model.setUserId("userId");
        model.setCreatedAt(LocalDateTime.MIN);
        model.setPrice(new BigDecimal(20));
        return model;
    }

    public static NotificationDtoRequest notificationDtoRequest1() {
        NotificationDtoRequest dtoRequest = new NotificationDtoRequest();
        dtoRequest.setMessage("message");
        dtoRequest.setMyData(new BigDecimal(10));
        dtoRequest.setUserId("userId");
        dtoRequest.setPrice(new BigDecimal(10));
        return dtoRequest;
    }

    public static NotificationDtoResponse notificationDtoResponse1() {
        NotificationDtoResponse dtoResponse = new NotificationDtoResponse();
        dtoResponse.setId(ID);
        dtoResponse.setMessage("message");
        dtoResponse.setMyData(new BigDecimal(10));
        dtoResponse.setUserId("userId");
        dtoResponse.setCreatedAt(LocalDateTime.MIN);
        dtoResponse.setPrice(new BigDecimal(10));
        return dtoResponse;
    }

    public static NotificationDtoResponse notificationDtoResponse2() {
        NotificationDtoResponse dtoResponse = new NotificationDtoResponse();
        dtoResponse.setId(ID);
        dtoResponse.setMessage("message");
        dtoResponse.setMyData(new BigDecimal(20));
        dtoResponse.setUserId("userId");
        dtoResponse.setCreatedAt(LocalDateTime.MIN);
        dtoResponse.setPrice(new BigDecimal(20));
        return dtoResponse;
    }
}
