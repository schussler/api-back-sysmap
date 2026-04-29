package com.sysmap.hubapi.repository;

import com.sysmap.hubapi.entity.ActivityParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ActivityParticipantRepository extends JpaRepository<ActivityParticipant, UUID> {
    // TODO: findByActivityIdAndUserId, existsByActivityIdAndUserId
}
