package com.example.saas.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.saas.model.OrderEntity;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    Optional<OrderEntity> findByIdAndUserId(Long id, String userId);
}
