package com.maksym.mytest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maksym.mytest.dto.request.NotificationDtoRequest;
import com.maksym.mytest.dto.response.NotificationDtoResponse;
import com.maksym.mytest.model.NotificationModel;
import com.maksym.mytest.exception.EntityNotFoundException;
import com.maksym.mytest.service.NotificationService;
import com.maksym.mytest.staticObject.StaticNotification;
import com.maksym.mytest.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class NotificationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NotificationService notificationService;

    private final String DOCUMENTATION_URI = "http://swagger_documentation";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private final NotificationDtoRequest notificationRequest = StaticNotification.notificationDtoRequest1();
    private final NotificationModel notificationModel = StaticNotification.notification1(); 
    private final NotificationDtoResponse notificationResponse = StaticNotification.notificationDtoResponse1();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        NotificationController notificationController = new NotificationController(notificationService);
        mockMvc = MockMvcBuilders.standaloneSetup(notificationController)
                .setControllerAdvice(new GlobalExceptionHandler(DOCUMENTATION_URI))
                .build();
    }

    @Test
    void testCreate_Success_ShouldReturnCreated() throws Exception {
        when(notificationService.create(any(NotificationModel.class))).thenReturn(notificationModel);

        mockMvc.perform(post("/api/notification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notificationRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(notificationResponse.getId()))
                .andExpect(jsonPath("$.message").value(notificationResponse.getMessage()))
                .andExpect(jsonPath("$.myData").value(notificationResponse.getMyData()))
                .andExpect(jsonPath("$.userId").value(notificationResponse.getUserId()))
                .andExpect(jsonPath("$.createdAt").value(notificationResponse.getCreatedAt().format(formatter)))
                .andExpect(jsonPath("$.price").value(notificationResponse.getPrice()));
    }

    @Test
    void testCreate_InvalidInput_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/notification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testCreate_EntityNotFoundException_ShouldReturnNotFound() throws Exception {
        when(notificationService.create(any(NotificationModel.class))).thenThrow(new EntityNotFoundException("Notification not found"));

        mockMvc.perform(post("/api/notification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notificationRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage").value("Notification not found"))
                .andExpect(jsonPath("$.documentationUri").value(DOCUMENTATION_URI));
    }

    @Test
    void testCreate_AnyException_ShouldReturnBadRequest() throws Exception {
        doThrow(new DataAccessException("Database connection failed") {}).when(notificationService).create(any(NotificationModel.class));

        mockMvc.perform(post("/api/notification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notificationRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("Database connection failed"))
                .andExpect(jsonPath("$.documentationUri").value(DOCUMENTATION_URI));
    }

    @Test
    void testGetById_Success_ShouldReturnOk() throws Exception {
        when(notificationService.getById(StaticNotification.ID)).thenReturn(notificationModel);

        mockMvc.perform(get("/api/notification/{id}", StaticNotification.ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(notificationResponse.getId()))
                .andExpect(jsonPath("$.message").value(notificationResponse.getMessage()))
                .andExpect(jsonPath("$.myData").value(notificationResponse.getMyData()))
                .andExpect(jsonPath("$.userId").value(notificationResponse.getUserId()))
                .andExpect(jsonPath("$.createdAt").value(notificationResponse.getCreatedAt().format(formatter)))
                .andExpect(jsonPath("$.price").value(notificationResponse.getPrice()));
    }

    @Test
    void testGetById_EntityNotFoundException_ShouldReturnNotFound() throws Exception {
        when(notificationService.getById(any())).thenThrow(new EntityNotFoundException("Notification not found"));

        mockMvc.perform(get("/api/notification/"+StaticNotification.ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage").value("Notification not found"))
                .andExpect(jsonPath("$.documentationUri").value(DOCUMENTATION_URI));
    }

    @Test
    void testGetById_AnyException_ShouldReturnBadRequest() throws Exception {
        doThrow(new DataAccessException("Database connection failed") {}).when(notificationService).getById(any());

        mockMvc.perform(get("/api/notification/"+StaticNotification.ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("Database connection failed"))
                .andExpect(jsonPath("$.documentationUri").value(DOCUMENTATION_URI));
    }


    @Test
    void testGetAll_Success_ShouldReturnOk() throws Exception {
        List<NotificationModel> notificationList = Arrays.asList(notificationModel, StaticNotification.notification1());
        Page<NotificationModel> notificationPage = new PageImpl<>(notificationList);
        Pageable pageable = Pageable.unpaged();
        when(notificationService.getAll(pageable)).thenReturn(notificationPage);

        mockMvc.perform(get("/api/notification/"))
                .andExpect(status().isOk())
		        .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$.[0].id").value(notificationResponse.getId()))
                .andExpect(jsonPath("$.[1].id").value(StaticNotification.notificationDtoResponse2().getId()));
    }

    @Test
    void testGetAll_AnyException_ShouldReturnBadRequest() throws Exception {
        doThrow(new DataAccessException("Database connection failed") {}).when(notificationService).getAll(any(Pageable.class));

        mockMvc.perform(get("/api/notification/"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("Database connection failed"))
                .andExpect(jsonPath("$.documentationUri").value(DOCUMENTATION_URI));
    }


    @Test
    void testUpdateById_Success_ShouldReturnOk() throws Exception {
        when(notificationService.updateById(any(), any(NotificationModel.class))).thenReturn(notificationModel);

        mockMvc.perform(put("/api/notification/"+StaticNotification.ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notificationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(notificationResponse.getId()))
                .andExpect(jsonPath("$.message").value(notificationResponse.getMessage()))
                .andExpect(jsonPath("$.myData").value(notificationResponse.getMyData()))
                .andExpect(jsonPath("$.userId").value(notificationResponse.getUserId()))
                .andExpect(jsonPath("$.createdAt").value(notificationResponse.getCreatedAt().format(formatter)))
                .andExpect(jsonPath("$.price").value(notificationResponse.getPrice()));
    }

    @Test
    void testUpdateById_InvalidInput_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/api/notification/"+StaticNotification.ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testUpdateById_EntityNotFoundException_ShouldReturnNotFound() throws Exception {
        when(notificationService.updateById(any(), any(NotificationModel.class))).thenThrow(new EntityNotFoundException("Notification not found"));

        mockMvc.perform(put("/api/notification/"+StaticNotification.ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notificationRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage").value("Notification not found"))
                .andExpect(jsonPath("$.documentationUri").value(DOCUMENTATION_URI));
    }

    @Test
    void testUpdateById_AnyException_ShouldReturnBadRequest() throws Exception {
        doThrow(new DataAccessException("Database connection failed") {}).when(notificationService).updateById(any(), any(NotificationModel.class));

        mockMvc.perform(put("/api/notification/"+StaticNotification.ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notificationRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("Database connection failed"))
                .andExpect(jsonPath("$.documentationUri").value(DOCUMENTATION_URI));
    }

    @Test
    void testDelete_Success_ShouldReturnNoContent() throws Exception {
        when(notificationService.deleteById(StaticNotification.ID)).thenReturn(true);

        mockMvc.perform(delete("/api/notification/"+StaticNotification.ID))
                .andExpect(status().isNoContent());
    }
	
    @Test
    void testDelete_AnyException_ShouldReturnBadRequest() throws Exception {
        doThrow(new DataAccessException("Database connection failed") {}).when(notificationService).deleteById(StaticNotification.ID);

        mockMvc.perform(delete("/api/notification/"+StaticNotification.ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("Database connection failed"))
                .andExpect(jsonPath("$.documentationUri").value(DOCUMENTATION_URI));
    }
}