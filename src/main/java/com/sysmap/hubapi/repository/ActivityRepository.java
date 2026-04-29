package com.sysmap.hubapi.repository;

import com.sysmap.hubapi.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ActivityRepository extends JpaRepository<Activity, UUID> {
    // TODO: consultas paginadas com filtro por tipo e priorização por preferências
}
