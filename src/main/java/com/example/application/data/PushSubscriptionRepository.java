package com.example.application.data.repository;

import java.util.List;

import com.example.application.data.entity.PushSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {

    List<PushSubscription> findPushSubscriptionByUserName(String userName);
}
