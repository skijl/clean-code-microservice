package com.maksym.mytest.dto.mapper;

import com.maksym.mytest.model.NotificationModel;
import com.maksym.mytest.model.NotificationChannel;
import com.maksym.mytest.model.NotificationMethod;
import com.maksym.mytest.dto.request.NotificationMethodDtoRequest;

public class NotificationMethodDtoMapper {

    public static NotificationMethod toModel(NotificationMethodDtoRequest request) {
        NotificationMethod model = new NotificationMethod();

        model.setMessageTitle(request.getMessageTitle());
        model.setCost(request.getCost());
        model.setUserId(request.getUserId());
        model.setPrice(request.getPrice());
        NotificationChannel notificationChannel = new NotificationChannel();
        notificationChannel.setId(request.getNotificationChannelId());
        model.setNotificationChannelModel(notificationChannel);
        NotificationModel notification = new NotificationModel();
        notification.setId(request.getNotificationId());
        model.setNotificationModel(notification);

        return model;
    }

    private NotificationMethodDtoMapper() {}

}
