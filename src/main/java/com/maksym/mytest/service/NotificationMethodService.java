package com.maksym.mytest.service;

import com.maksym.mytest.exception.EntityNotFoundException;
import com.maksym.mytest.model.NotificationMethod;
import com.maksym.mytest.repository.NotificationMethodRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationMethodService {
    private final NotificationMethodRepository notificationMethodRepository;
    private final NotificationChannelService notificationChannelService;
    private final NotificationService notificationService;

    public NotificationMethodService(NotificationService notificationService, NotificationChannelService notificationChannelService, NotificationMethodRepository notificationMethodRepository) {
        this.notificationService = notificationService;
        this.notificationChannelService = notificationChannelService;
        this.notificationMethodRepository = notificationMethodRepository;
    }

    public NotificationMethod create(NotificationMethod notificationMethod) {
        log.info("NotificationMethod create: {}", notificationMethod);
        notificationMethod.setNotificationChannelModel(notificationChannelService.getById(notificationMethod.getNotificationChannelModel().getId()));
        notificationMethod.setNotificationModel(notificationService.getById(notificationMethod.getNotificationModel().getId()));
        return notificationMethodRepository.save(notificationMethod);
    }

    public NotificationMethod getById(Long id) {
        log.info("NotificationMethod get by id: {}", id);
        return notificationMethodRepository.findById(id).orElseThrow(()->new EntityNotFoundException("NotificationMethod with id: " + id + " does not exist"));
    }

    public Page<NotificationMethod> getAll(Pageable pageable) {
        log.info("NotificationMethod get all: {}", pageable);
        return notificationMethodRepository.findAll(pageable);
    }

    public NotificationMethod updateById(Long id, NotificationMethod notificationMethod) {
        getById(id);
        notificationMethod.setId(id);
        notificationMethod.setNotificationChannelModel(notificationChannelService.getById(notificationMethod.getNotificationChannelModel().getId()));
        notificationMethod.setNotificationModel(notificationService.getById(notificationMethod.getNotificationModel().getId()));
        log.info("NotificationMethod update by id: {}", notificationMethod);
        return notificationMethodRepository.save(notificationMethod);
    }

    public Boolean deleteById(Long id) {
        log.info("NotificationMethod delete by id: {}", id);
        notificationMethodRepository.deleteById(id);
        return true;
    }
}
