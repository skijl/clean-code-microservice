package com.maksym.mytest.repository;

import com.maksym.mytest.model.NotificationModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<NotificationModel, Long> {

}
