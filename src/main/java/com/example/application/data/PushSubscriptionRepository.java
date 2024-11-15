package com.example.application.data;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PushSubscriptionRepository extends JpaRepository<WebPushSubscriptionEntity, Long> {

    List<WebPushSubscriptionEntity> findPushSubscriptionByUserName(String userName);
}
