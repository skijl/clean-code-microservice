package com.maksym.mytest.service;


import com.maksym.mytest.exception.EntityNotFoundException;
import com.maksym.mytest.model.NotificationMethod;
import com.maksym.mytest.repository.NotificationMethodRepository;
import com.maksym.mytest.staticObject.StaticNotificationMethod;
import com.maksym.mytest.staticObject.StaticNotificationChannel;
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

class NotificationMethodServiceTest {

    @Mock
    private NotificationMethodRepository notificationMethodRepository;
    @Mock
    private NotificationChannelService notificationChannelService;
    @Mock
    private NotificationService notificationService;
    @InjectMocks
    private NotificationMethodService notificationMethodService;
    private final NotificationMethod notificationMethod = StaticNotificationMethod.notificationMethod1();
    private final NotificationMethod notificationMethod2 = StaticNotificationMethod.notificationMethod2();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreate() {
        when(notificationChannelService.getById(StaticNotificationChannel.ID)).thenReturn(StaticNotificationChannel.notificationChannel1());
        when(notificationService.getById(StaticNotification.ID)).thenReturn(StaticNotification.notification1());
	    when(notificationMethodRepository.save(any(NotificationMethod.class))).thenReturn(notificationMethod);

        NotificationMethod createdNotificationMethod = notificationMethodService.create(notificationMethod);

        assertNotNull(createdNotificationMethod);
        assertEquals(notificationMethod, createdNotificationMethod);
        verify(notificationChannelService, times(1)).getById(StaticNotification.ID);
        verify(notificationService, times(1)).getById(StaticNotification.ID);
        verify(notificationMethodRepository, times(1)).save(notificationMethod);
    }

    @Test
    void testCreate_EntityNotFoundException_NotificationChannelNotFound() {
        when(notificationChannelService.getById(StaticNotificationChannel.ID)).thenThrow(new EntityNotFoundException("NotificationChannel not found"));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> notificationMethodService.create(notificationMethod));

        assertNotNull(exception);
        assertEquals("NotificationChannel not found", exception.getMessage());
        verify(notificationChannelService, times(1)).getById(StaticNotificationChannel.ID);
        verifyNoInteractions(notificationMethodRepository);
    }

    @Test
    void testCreate_EntityNotFoundException_NotificationNotFound() {
        when(notificationChannelService.getById(StaticNotificationChannel.ID)).thenReturn(StaticNotificationChannel.notificationChannel1());
        when(notificationService.getById(StaticNotification.ID)).thenThrow(new EntityNotFoundException("Notification not found"));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> notificationMethodService.create(notificationMethod));

        assertNotNull(exception);
        assertEquals("Notification not found", exception.getMessage());
        verify(notificationChannelService, times(1)).getById(StaticNotificationChannel.ID);
        verify(notificationService, times(1)).getById(StaticNotification.ID);
        verifyNoInteractions(notificationMethodRepository);
    }

    @Test
    void testCreate_DataAccessException() {
        when(notificationChannelService.getById(StaticNotificationChannel.ID)).thenReturn(StaticNotificationChannel.notificationChannel1());
        when(notificationService.getById(StaticNotification.ID)).thenReturn(StaticNotification.notification1());
        when(notificationMethodRepository.findById(StaticNotificationMethod.ID)).thenThrow(new DataAccessException("Database connection failed") {
        });

        RuntimeException exception = assertThrows(RuntimeException.class, () -> notificationMethodService.getById(StaticNotificationMethod.ID));

        assertNotNull(exception);
        assertEquals("Database connection failed", exception.getMessage());
        verify(notificationMethodRepository, times(1)).findById(StaticNotificationMethod.ID);
    }

    @Test
    void testGetAll() {
        List<NotificationMethod> notificationMethodList = new ArrayList<>();
        notificationMethodList.add(notificationMethod);
        notificationMethodList.add(notificationMethod2);
        Page<NotificationMethod> notificationMethodPage = new PageImpl<>(notificationMethodList);
        Pageable pageable = Pageable.unpaged();
        when(notificationMethodRepository.findAll(pageable)).thenReturn(notificationMethodPage);

        Page<NotificationMethod> result = notificationMethodService.getAll(pageable);

        assertEquals(notificationMethodList.size(), result.getSize());
        assertEquals(notificationMethod, result.getContent().get(0));
        assertEquals(notificationMethod2, result.getContent().get(1));
    }

    @Test
    void testGetAll_AnyException() {
        when(notificationMethodRepository.findAll(any(Pageable.class))).thenThrow(new DataAccessException("Database connection failed") {});

        Pageable pageable = Pageable.unpaged();
        RuntimeException exception = assertThrows(DataAccessException.class, () -> notificationMethodService.getAll(pageable));

        assertNotNull(exception);
        assertEquals("Database connection failed", exception.getMessage());
        verify(notificationMethodRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void testUpdate_Success() {
	    NotificationMethod existingNotificationMethod = StaticNotificationMethod.notificationMethod1();
        NotificationMethod updatedNotificationMethod = StaticNotificationMethod.notificationMethod2();
        when(notificationChannelService.getById(StaticNotificationChannel.ID)).thenReturn(StaticNotificationChannel.notificationChannel1());
        when(notificationService.getById(StaticNotification.ID)).thenReturn(StaticNotification.notification1());
	    when(notificationMethodRepository.findById(StaticNotificationMethod.ID)).thenReturn(java.util.Optional.of(existingNotificationMethod));
        when(notificationMethodRepository.save(updatedNotificationMethod)).thenReturn(updatedNotificationMethod);

        NotificationMethod result = notificationMethodService.updateById(StaticNotificationMethod.ID, updatedNotificationMethod);

        assertEquals(updatedNotificationMethod, result);
        verify(notificationMethodRepository, times(1)).findById(StaticNotificationMethod.ID);
        verify(notificationMethodRepository, times(1)).save(updatedNotificationMethod);
    }

    @Test
    void testUpdateById_EntityNotFoundException_NotificationChannelNotFound() {
        when(notificationMethodRepository.findById(StaticNotificationMethod.ID)).thenReturn(java.util.Optional.of(notificationMethod));
        when(notificationChannelService.getById(StaticNotificationChannel.ID)).thenThrow(new EntityNotFoundException("NotificationChannel not found"));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> notificationMethodService.updateById(StaticNotificationMethod.ID, notificationMethod));

        assertNotNull(exception);
        assertEquals("NotificationChannel not found", exception.getMessage());
        verify(notificationChannelService, times(1)).getById(StaticNotificationChannel.ID);
    }

    @Test
    void testUpdateById_EntityNotFoundException_NotificationNotFound() {
        when(notificationMethodRepository.findById(StaticNotificationMethod.ID)).thenReturn(java.util.Optional.of(notificationMethod));
        when(notificationChannelService.getById(StaticNotificationChannel.ID)).thenReturn(StaticNotificationChannel.notificationChannel1());
        when(notificationService.getById(StaticNotification.ID)).thenThrow(new EntityNotFoundException("Notification not found"));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> notificationMethodService.updateById(StaticNotificationMethod.ID, notificationMethod));

        assertNotNull(exception);
        assertEquals("Notification not found", exception.getMessage());
        verify(notificationChannelService, times(1)).getById(StaticNotificationChannel.ID);
        verify(notificationService, times(1)).getById(StaticNotification.ID);
    }


    @Test
    void testUpdateById_EntityNotFoundException() {
        NotificationMethod updatedNotificationMethod = StaticNotificationMethod.notificationMethod1();
        when(notificationMethodRepository.findById(StaticNotificationMethod.ID)).thenReturn(java.util.Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> notificationMethodService.updateById(StaticNotificationMethod.ID, updatedNotificationMethod));
        verify(notificationMethodRepository, times(1)).findById(StaticNotificationMethod.ID);
        verify(notificationMethodRepository, never()).save(updatedNotificationMethod);
    }

    @Test
    void testUpdateById_AnyException() {
        NotificationMethod existingNotificationMethod = StaticNotificationMethod.notificationMethod1();
        NotificationMethod updatedNotificationMethod = StaticNotificationMethod.notificationMethod2();
        when(notificationMethodRepository.findById(StaticNotificationMethod.ID)).thenReturn(java.util.Optional.of(existingNotificationMethod));
        when(notificationChannelService.getById(StaticNotificationChannel.ID)).thenReturn(StaticNotificationChannel.notificationChannel1());
        when(notificationService.getById(StaticNotification.ID)).thenReturn(StaticNotification.notification1());
	    when(notificationMethodRepository.save(updatedNotificationMethod)).thenThrow(new DataAccessException("Database connection failed") {
        });

        RuntimeException exception = assertThrows(RuntimeException.class, () -> notificationMethodService.updateById(StaticNotificationMethod.ID, updatedNotificationMethod));

        assertNotNull(exception);
        assertEquals("Database connection failed", exception.getMessage());
        verify(notificationMethodRepository, times(1)).save(updatedNotificationMethod);
    }

    @Test
    void testDeleteById_Success() {
        boolean result = notificationMethodService.deleteById(StaticNotificationMethod.ID);

        verify(notificationMethodRepository).deleteById(StaticNotificationMethod.ID);
        assertTrue(result);
    }

    @Test
    void testDeleteById_AnyException() {
        doThrow(new DataAccessException("Database connection failed") {}).when(notificationMethodRepository).deleteById(StaticNotificationMethod.ID);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> notificationMethodService.deleteById(StaticNotificationMethod.ID));

        assertNotNull(exception);
        assertEquals("Database connection failed", exception.getMessage());
        verify(notificationMethodRepository, times(1)).deleteById(StaticNotificationMethod.ID);
    }
}