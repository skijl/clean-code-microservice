package com.maksym.mytest.service;

import com.maksym.mytest.exception.EntityNotFoundException;
import com.maksym.mytest.model.NotificationModel;
import com.maksym.mytest.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public NotificationModel create(NotificationModel notification) {
        log.info("NotificationModel create: {}", notification);

        return notificationRepository.save(notification);
    }

    public NotificationModel getById(Long id) {
        log.info("NotificationModel get by id: {}", id);
        return notificationRepository.findById(id).orElseThrow(()->new EntityNotFoundException("Notification with id: " + id + " does not exist"));
    }

    public Page<NotificationModel> getAll(Pageable pageable) {
        log.info("NotificationModel get all: {}", pageable);
        return notificationRepository.findAll(pageable);
    }

    public NotificationModel updateById(Long id, NotificationModel notification) {
        getById(id);
        notification.setId(id);

        log.info("NotificationModel update by id: {}", notification);
        return notificationRepository.save(notification);
    }

    public Boolean deleteById(Long id) {
        log.info("NotificationModel delete by id: {}", id);
        notificationRepository.deleteById(id);
        return true;
    }
}
