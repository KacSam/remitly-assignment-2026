package com.remitly.assignment.persistence.repository;

import java.util.List;
import java.util.Optional;

import com.remitly.assignment.persistence.entity.AuditLogEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {

    List<AuditLogEntity> findAllByOrderByIdAsc();

    Optional<AuditLogEntity> findFirstByOrderByIdAsc();
}
