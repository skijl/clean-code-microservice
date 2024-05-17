package com.maksym.mytest.repository;

import com.maksym.mytest.model.NotificationChannel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationChannelRepository extends JpaRepository<NotificationChannel, Long> {

}
