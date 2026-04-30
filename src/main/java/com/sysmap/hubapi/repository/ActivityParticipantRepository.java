package com.sysmap.hubapi.repository;

import com.sysmap.hubapi.entity.ActivityParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ActivityParticipantRepository extends JpaRepository<ActivityParticipant, UUID> {

    long countByActivity_Id(UUID activityId);

    Optional<ActivityParticipant> findByActivity_IdAndUser_Id(UUID activityId, UUID userId);

    boolean existsByActivity_IdAndUser_Id(UUID activityId, UUID userId);

    @Query("SELECT ap FROM ActivityParticipant ap JOIN FETCH ap.user WHERE ap.activity.id = :activityId")
    List<ActivityParticipant> findByActivityIdWithUser(@Param("activityId") UUID activityId);
}
