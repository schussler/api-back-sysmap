package com.sysmap.hubapi.repository;

import com.sysmap.hubapi.entity.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ActivityTypeRepository extends JpaRepository<ActivityType, UUID> {
}
