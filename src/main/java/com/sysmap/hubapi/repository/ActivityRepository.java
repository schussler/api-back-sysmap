package com.sysmap.hubapi.repository;

import com.sysmap.hubapi.entity.Activity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ActivityRepository extends JpaRepository<Activity, UUID> {

    // GET /activities sem typeId — ordenação por preferências do usuário
    @Query(value = "SELECT a FROM Activity a WHERE a.deletedAt IS NULL AND a.completedAt IS NULL " +
                   "ORDER BY CASE WHEN a.type.id IN :preferredTypeIds THEN 0 ELSE 1 END, a.createdAt DESC",
           countQuery = "SELECT COUNT(a) FROM Activity a WHERE a.deletedAt IS NULL AND a.completedAt IS NULL")
    Page<Activity> findAvailableWithPreferenceOrdering(@Param("preferredTypeIds") List<UUID> preferredTypeIds,
                                                       Pageable pageable);

    // GET /activities sem typeId, sem preferências
    Page<Activity> findByDeletedAtIsNullAndCompletedAtIsNull(Pageable pageable);

    // GET /activities com typeId
    Page<Activity> findByType_IdAndDeletedAtIsNullAndCompletedAtIsNull(UUID typeId, Pageable pageable);

    // GET /activities/all sem typeId
    @Query("SELECT a FROM Activity a WHERE a.deletedAt IS NULL AND a.completedAt IS NULL")
    List<Activity> findAllAvailable(Sort sort);

    // GET /activities/all com typeId
    List<Activity> findByType_IdAndDeletedAtIsNullAndCompletedAtIsNull(UUID typeId, Sort sort);

    // GET /activities/user/creator (paginado)
    Page<Activity> findByCreator_Id(UUID creatorId, Pageable pageable);

    // GET /activities/user/creator/all
    List<Activity> findByCreator_Id(UUID creatorId, Sort sort);

    // GET /activities/user/participant (paginado)
    @Query(value = "SELECT a FROM Activity a WHERE a.id IN " +
                   "(SELECT ap.activity.id FROM ActivityParticipant ap WHERE ap.user.id = :userId)",
           countQuery = "SELECT COUNT(a) FROM Activity a WHERE a.id IN " +
                        "(SELECT ap.activity.id FROM ActivityParticipant ap WHERE ap.user.id = :userId)")
    Page<Activity> findByParticipantUserId(@Param("userId") UUID userId, Pageable pageable);

    // GET /activities/user/participant/all
    @Query("SELECT a FROM Activity a WHERE a.id IN " +
           "(SELECT ap.activity.id FROM ActivityParticipant ap WHERE ap.user.id = :userId)")
    List<Activity> findAllByParticipantUserId(@Param("userId") UUID userId);
}
