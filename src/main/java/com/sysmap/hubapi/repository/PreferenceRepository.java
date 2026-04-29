package com.sysmap.hubapi.repository;

import com.sysmap.hubapi.entity.Preference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PreferenceRepository extends JpaRepository<Preference, UUID> {
    // TODO: findByUserId, deleteByUserId
}
