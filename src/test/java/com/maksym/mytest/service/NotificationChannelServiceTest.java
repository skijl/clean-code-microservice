package com.maksym.mytest.service;


import com.maksym.mytest.exception.EntityNotFoundException;
import com.maksym.mytest.model.NotificationChannel;
import com.maksym.mytest.repository.NotificationChannelRepository;
import com.maksym.mytest.staticObject.StaticNotificationChannel;
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

class NotificationChannelServiceTest {

    @Mock
    private NotificationChannelRepository notificationChannelRepository;
    @InjectMocks
    private NotificationChannelService notificationChannelService;
    private final NotificationChannel notificationChannel = StaticNotificationChannel.notificationChannel1();
    private final NotificationChannel notificationChannel2 = StaticNotificationChannel.notificationChannel2();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreate() {
	    when(notificationChannelRepository.save(any(NotificationChannel.class))).thenReturn(notificationChannel);

        NotificationChannel createdNotificationChannel = notificationChannelService.create(notificationChannel);

        assertNotNull(createdNotificationChannel);
        assertEquals(notificationChannel, createdNotificationChannel);
        verify(notificationChannelRepository, times(1)).save(notificationChannel);
    }

    @Test
    void testCreate_DataAccessException() {
        when(notificationChannelRepository.findById(StaticNotificationChannel.ID)).thenThrow(new DataAccessException("Database connection failed") {
        });

        RuntimeException exception = assertThrows(RuntimeException.class, () -> notificationChannelService.getById(StaticNotificationChannel.ID));

        assertNotNull(exception);
        assertEquals("Database connection failed", exception.getMessage());
        verify(notificationChannelRepository, times(1)).findById(StaticNotificationChannel.ID);
    }

    @Test
    void testGetAll() {
        List<NotificationChannel> notificationChannelList = new ArrayList<>();
        notificationChannelList.add(notificationChannel);
        notificationChannelList.add(notificationChannel2);
        Page<NotificationChannel> notificationChannelPage = new PageImpl<>(notificationChannelList);
        Pageable pageable = Pageable.unpaged();
        when(notificationChannelRepository.findAll(pageable)).thenReturn(notificationChannelPage);

        Page<NotificationChannel> result = notificationChannelService.getAll(pageable);

        assertEquals(notificationChannelList.size(), result.getSize());
        assertEquals(notificationChannel, result.getContent().get(0));
        assertEquals(notificationChannel2, result.getContent().get(1));
    }

    @Test
    void testGetAll_AnyException() {
        when(notificationChannelRepository.findAll(any(Pageable.class))).thenThrow(new DataAccessException("Database connection failed") {});

        Pageable pageable = Pageable.unpaged();
        RuntimeException exception = assertThrows(DataAccessException.class, () -> notificationChannelService.getAll(pageable));

        assertNotNull(exception);
        assertEquals("Database connection failed", exception.getMessage());
        verify(notificationChannelRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void testUpdate_Success() {
	    NotificationChannel existingNotificationChannel = StaticNotificationChannel.notificationChannel1();
        NotificationChannel updatedNotificationChannel = StaticNotificationChannel.notificationChannel2();
	    when(notificationChannelRepository.findById(StaticNotificationChannel.ID)).thenReturn(java.util.Optional.of(existingNotificationChannel));
        when(notificationChannelRepository.save(updatedNotificationChannel)).thenReturn(updatedNotificationChannel);

        NotificationChannel result = notificationChannelService.updateById(StaticNotificationChannel.ID, updatedNotificationChannel);

        assertEquals(updatedNotificationChannel, result);
        verify(notificationChannelRepository, times(1)).findById(StaticNotificationChannel.ID);
        verify(notificationChannelRepository, times(1)).save(updatedNotificationChannel);
    }


    @Test
    void testUpdateById_EntityNotFoundException() {
        NotificationChannel updatedNotificationChannel = StaticNotificationChannel.notificationChannel1();
        when(notificationChannelRepository.findById(StaticNotificationChannel.ID)).thenReturn(java.util.Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> notificationChannelService.updateById(StaticNotificationChannel.ID, updatedNotificationChannel));
        verify(notificationChannelRepository, times(1)).findById(StaticNotificationChannel.ID);
        verify(notificationChannelRepository, never()).save(updatedNotificationChannel);
    }

    @Test
    void testUpdateById_AnyException() {
        NotificationChannel existingNotificationChannel = StaticNotificationChannel.notificationChannel1();
        NotificationChannel updatedNotificationChannel = StaticNotificationChannel.notificationChannel2();
        when(notificationChannelRepository.findById(StaticNotificationChannel.ID)).thenReturn(java.util.Optional.of(existingNotificationChannel));
	    when(notificationChannelRepository.save(updatedNotificationChannel)).thenThrow(new DataAccessException("Database connection failed") {
        });

        RuntimeException exception = assertThrows(RuntimeException.class, () -> notificationChannelService.updateById(StaticNotificationChannel.ID, updatedNotificationChannel));

        assertNotNull(exception);
        assertEquals("Database connection failed", exception.getMessage());
        verify(notificationChannelRepository, times(1)).save(updatedNotificationChannel);
    }

    @Test
    void testDeleteById_Success() {
        boolean result = notificationChannelService.deleteById(StaticNotificationChannel.ID);

        verify(notificationChannelRepository).deleteById(StaticNotificationChannel.ID);
        assertTrue(result);
    }

    @Test
    void testDeleteById_AnyException() {
        doThrow(new DataAccessException("Database connection failed") {}).when(notificationChannelRepository).deleteById(StaticNotificationChannel.ID);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> notificationChannelService.deleteById(StaticNotificationChannel.ID));

        assertNotNull(exception);
        assertEquals("Database connection failed", exception.getMessage());
        verify(notificationChannelRepository, times(1)).deleteById(StaticNotificationChannel.ID);
    }
}