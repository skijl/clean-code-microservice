package com.maksym.mytest.service;


import com.maksym.mytest.exception.EntityNotFoundException;
import com.maksym.mytest.model.NotificationModel;
import com.maksym.mytest.repository.NotificationRepository;
import com.maksym.mytest.staticObject.StaticNotification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;
    @InjectMocks
    private NotificationService notificationService;
    private final NotificationModel notification = StaticNotification.notification1();
    private final NotificationModel notification2 = StaticNotification.notification2();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreate() {
	    when(notificationRepository.save(any(NotificationModel.class))).thenReturn(notification);

        NotificationModel createdNotification = notificationService.create(notification);

        assertNotNull(createdNotification);
        assertEquals(notification, createdNotification);
        verify(notificationRepository, times(1)).save(notification);
    }

    @Test
    void testCreate_DataAccessException() {
        when(notificationRepository.findById(StaticNotification.ID)).thenThrow(new DataAccessException("Database connection failed") {
        });

        RuntimeException exception = assertThrows(RuntimeException.class, () -> notificationService.getById(StaticNotification.ID));

        assertNotNull(exception);
        assertEquals("Database connection failed", exception.getMessage());
        verify(notificationRepository, times(1)).findById(StaticNotification.ID);
    }

    @Test
    void testGetAll() {
        List<NotificationModel> notificationList = new ArrayList<>();
        notificationList.add(notification);
        notificationList.add(notification2);
        Page<NotificationModel> notificationPage = new PageImpl<>(notificationList);
        Pageable pageable = Pageable.unpaged();
        when(notificationRepository.findAll(pageable)).thenReturn(notificationPage);

        Page<NotificationModel> result = notificationService.getAll(pageable);

        assertEquals(notificationList.size(), result.getSize());
        assertEquals(notification, result.getContent().get(0));
        assertEquals(notification2, result.getContent().get(1));
    }

    @Test
    void testGetAll_AnyException() {
        when(notificationRepository.findAll(any(Pageable.class))).thenThrow(new DataAccessException("Database connection failed") {});

        Pageable pageable = Pageable.unpaged();
        RuntimeException exception = assertThrows(DataAccessException.class, () -> notificationService.getAll(pageable));

        assertNotNull(exception);
        assertEquals("Database connection failed", exception.getMessage());
        verify(notificationRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void testUpdate_Success() {
	    NotificationModel existingNotification = StaticNotification.notification1();
        NotificationModel updatedNotification = StaticNotification.notification2();
	    when(notificationRepository.findById(StaticNotification.ID)).thenReturn(java.util.Optional.of(existingNotification));
        when(notificationRepository.save(updatedNotification)).thenReturn(updatedNotification);

        NotificationModel result = notificationService.updateById(StaticNotification.ID, updatedNotification);

        assertEquals(updatedNotification, result);
        verify(notificationRepository, times(1)).findById(StaticNotification.ID);
        verify(notificationRepository, times(1)).save(updatedNotification);
    }


    @Test
    void testUpdateById_EntityNotFoundException() {
        NotificationModel updatedNotification = StaticNotification.notification1();
        when(notificationRepository.findById(StaticNotification.ID)).thenReturn(java.util.Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> notificationService.updateById(StaticNotification.ID, updatedNotification));
        verify(notificationRepository, times(1)).findById(StaticNotification.ID);
        verify(notificationRepository, never()).save(updatedNotification);
    }

    @Test
    void testUpdateById_AnyException() {
        NotificationModel existingNotification = StaticNotification.notification1();
        NotificationModel updatedNotification = StaticNotification.notification2();
        when(notificationRepository.findById(StaticNotification.ID)).thenReturn(java.util.Optional.of(existingNotification));
	    when(notificationRepository.save(updatedNotification)).thenThrow(new DataAccessException("Database connection failed") {
        });

        RuntimeException exception = assertThrows(RuntimeException.class, () -> notificationService.updateById(StaticNotification.ID, updatedNotification));

        assertNotNull(exception);
        assertEquals("Database connection failed", exception.getMessage());
        verify(notificationRepository, times(1)).save(updatedNotification);
    }

    @Test
    void testDeleteById_Success() {
        boolean result = notificationService.deleteById(StaticNotification.ID);

        verify(notificationRepository).deleteById(StaticNotification.ID);
        assertTrue(result);
    }

    @Test
    void testDeleteById_AnyException() {
        doThrow(new DataAccessException("Database connection failed") {}).when(notificationRepository).deleteById(StaticNotification.ID);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> notificationService.deleteById(StaticNotification.ID));

        assertNotNull(exception);
        assertEquals("Database connection failed", exception.getMessage());
        verify(notificationRepository, times(1)).deleteById(StaticNotification.ID);
    }
}