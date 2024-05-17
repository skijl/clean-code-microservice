package com.maksym.mytest.dto.mapper;

import com.maksym.mytest.model.NotificationModel;
import com.maksym.mytest.dto.request.NotificationDtoRequest;
import com.maksym.mytest.dto.response.NotificationDtoResponse;

public class NotificationDtoMapper {

    public static NotificationModel toModel(NotificationDtoRequest request) {
        NotificationModel model = new NotificationModel();

        model.setMessage(request.getMessage());
        model.setMyData(request.getMyData());
        model.setUserId(request.getUserId());
        model.setPrice(request.getPrice());

        return model;
    }

    public static NotificationDtoResponse toResponse(NotificationModel model) {
        NotificationDtoResponse response = new NotificationDtoResponse();

        response.setId(model.getId());
        response.setMessage(model.getMessage());
        response.setMyData(model.getMyData());
        response.setUserId(model.getUserId());
        response.setCreatedAt(model.getCreatedAt());
        response.setPrice(model.getPrice());

        return response;
    }

    private NotificationDtoMapper() {}

}
