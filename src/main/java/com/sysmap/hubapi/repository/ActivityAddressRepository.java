package com.sysmap.hubapi.repository;

import com.sysmap.hubapi.entity.ActivityAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ActivityAddressRepository extends JpaRepository<ActivityAddress, UUID> {

    Optional<ActivityAddress> findByActivity_Id(UUID activityId);
}
