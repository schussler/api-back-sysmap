package com.sysmap.hubapi.repository;

import com.sysmap.hubapi.entity.Preference;
import com.sysmap.hubapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PreferenceRepository extends JpaRepository<Preference, UUID> {

    @Query("SELECT p FROM Preference p JOIN FETCH p.type WHERE p.user.id = :userId")
    List<Preference> findByUserId(@Param("userId") UUID userId);

    void deleteByUser(User user);
}
