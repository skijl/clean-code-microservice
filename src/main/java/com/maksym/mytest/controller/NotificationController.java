package com.maksym.mytest.controller;

import com.maksym.mytest.dto.mapper.NotificationDtoMapper;
import com.maksym.mytest.dto.request.NotificationDtoRequest;
import com.maksym.mytest.dto.response.NotificationDtoResponse;
import com.maksym.mytest.model.NotificationModel;
import com.maksym.mytest.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    @Operation(summary = "Create an notification", description = "Create new notification")
    @ApiResponse(responseCode = "201", description = "Notification saved successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "404", description = "Invalid foreign key that is not found")
    @ApiResponse(responseCode = "503", description = "Database connection failed")
    public ResponseEntity<NotificationDtoResponse> createNotification(@Valid @RequestBody NotificationDtoRequest notificationDtoRequest) {
        NotificationModel notification = NotificationDtoMapper.toModel(notificationDtoRequest);
        notification = notificationService.create(notification);
        return new ResponseEntity<>(NotificationDtoMapper.toResponse(notification), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Notification", description = "Get Notification By Id")
    @ApiResponse(responseCode = "200", description = "Notification Get successfully")
    @ApiResponse(responseCode = "404", description = "Notification with such an Id not found")
    public ResponseEntity<NotificationDtoResponse> getNotificationById(@PathVariable("id") Long id) {
        NotificationModel notification = notificationService.getById(id);
        return new ResponseEntity<>(NotificationDtoMapper.toResponse(notification), HttpStatus.OK);
    }

    @GetMapping
    @Operation(summary = "Get All Notification", description = "Get All Notification")
    @ApiResponse(responseCode = "200", description = "Notification Get All successfully")
    @ApiResponse(responseCode = "404", description = "No records with Notification have been found")
    public ResponseEntity<Page<NotificationDtoResponse>> getAllNotification(Pageable pageable) {
        Page<NotificationModel> notificationPage = notificationService.getAll(pageable);
        return new ResponseEntity<>(notificationPage.map(NotificationDtoMapper::toResponse), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an notification", description = "Update an notification by Id and new Notification")
    @ApiResponse(responseCode = "201", description = "Notification updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "404", description = "Notification with such an Id not found or invalid foreign key that is not found")
    public ResponseEntity<NotificationDtoResponse> updateNotification(@PathVariable("id") Long id, @Valid @RequestBody NotificationDtoRequest notificationDtoRequest) {
        NotificationModel notification = NotificationDtoMapper.toModel(notificationDtoRequest);
        notification = notificationService.updateById(id, notification);
        return new ResponseEntity<>(NotificationDtoMapper.toResponse(notification), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an notification", description = "Delete an notification by id")
    @ApiResponse(responseCode = "204", description = "Notification deleted successfully")
    public ResponseEntity<Boolean> deleteNotification(@PathVariable("id") Long id) {
        return new ResponseEntity<>(notificationService.deleteById(id), HttpStatus.NO_CONTENT);
    }
}