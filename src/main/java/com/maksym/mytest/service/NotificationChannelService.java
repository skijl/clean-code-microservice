package com.maksym.mytest.service;

import com.maksym.mytest.exception.EntityNotFoundException;
import com.maksym.mytest.model.NotificationChannel;
import com.maksym.mytest.repository.NotificationChannelRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationChannelService {
    private final NotificationChannelRepository notificationChannelRepository;

    public NotificationChannelService(NotificationChannelRepository notificationChannelRepository) {
        this.notificationChannelRepository = notificationChannelRepository;
    }

    public NotificationChannel create(NotificationChannel notificationChannel) {
        log.info("NotificationChannel create: {}", notificationChannel);

        return notificationChannelRepository.save(notificationChannel);
    }

    public NotificationChannel getById(Long id) {
        log.info("NotificationChannel get by id: {}", id);
        return notificationChannelRepository.findById(id).orElseThrow(()->new EntityNotFoundException("NotificationChannel with id: " + id + " does not exist"));
    }

    public Page<NotificationChannel> getAll(Pageable pageable) {
        log.info("NotificationChannel get all: {}", pageable);
        return notificationChannelRepository.findAll(pageable);
    }

    public NotificationChannel updateById(Long id, NotificationChannel notificationChannel) {
        getById(id);
        notificationChannel.setId(id);

        log.info("NotificationChannel update by id: {}", notificationChannel);
        return notificationChannelRepository.save(notificationChannel);
    }

    public Boolean deleteById(Long id) {
        log.info("NotificationChannel delete by id: {}", id);
        notificationChannelRepository.deleteById(id);
        return true;
    }
}
