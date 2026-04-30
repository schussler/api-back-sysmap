package com.sysmap.hubapi.service;

import com.sysmap.hubapi.dto.response.*;
import com.sysmap.hubapi.entity.*;
import com.sysmap.hubapi.exception.ActivityNotFoundException;
import com.sysmap.hubapi.exception.MissingFieldsException;
import com.sysmap.hubapi.exception.NotActivityCreatorException;
import com.sysmap.hubapi.repository.*;
import com.sysmap.hubapi.util.ConfirmationCodeGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final ActivityTypeRepository activityTypeRepository;
    private final ActivityAddressRepository activityAddressRepository;
    private final ActivityParticipantRepository activityParticipantRepository;
    private final PreferenceRepository preferenceRepository;
    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final S3Service s3Service;

    public ActivityService(ActivityRepository activityRepository,
                           ActivityTypeRepository activityTypeRepository,
                           ActivityAddressRepository activityAddressRepository,
                           ActivityParticipantRepository activityParticipantRepository,
                           PreferenceRepository preferenceRepository,
                           AchievementRepository achievementRepository,
                           UserAchievementRepository userAchievementRepository,
                           S3Service s3Service) {
        this.activityRepository = activityRepository;
        this.activityTypeRepository = activityTypeRepository;
        this.activityAddressRepository = activityAddressRepository;
        this.activityParticipantRepository = activityParticipantRepository;
        this.preferenceRepository = preferenceRepository;
        this.achievementRepository = achievementRepository;
        this.userAchievementRepository = userAchievementRepository;
        this.s3Service = s3Service;
    }

    // ── Tipos ─────────────────────────────────────────────────────────────

    public List<ActivityTypeResponse> listTypes() {
        return activityTypeRepository.findAll().stream()
                .map(t -> new ActivityTypeResponse(t.getId(), t.getName(), t.getDescription(), t.getImage()))
                .toList();
    }

    // ── Listagem paginada ─────────────────────────────────────────────────

    public PagedActivityResponse listActivities(int page, int pageSize, UUID typeId,
                                                String orderBy, String order) {
        User user = currentUser();
        Sort sort = buildSort(orderBy, order);

        Page<Activity> activitiesPage;
        if (typeId != null) {
            Pageable pageable = PageRequest.of(page - 1, pageSize, sort);
            activitiesPage = activityRepository
                    .findByType_IdAndDeletedAtIsNullAndCompletedAtIsNull(typeId, pageable);
        } else {
            List<UUID> preferredTypeIds = preferenceRepository.findByUserId(user.getId())
                    .stream().map(p -> p.getType().getId()).toList();

            if (preferredTypeIds.isEmpty()) {
                activitiesPage = activityRepository
                        .findByDeletedAtIsNullAndCompletedAtIsNull(PageRequest.of(page - 1, pageSize, sort));
            } else {
                activitiesPage = activityRepository
                        .findAvailableWithPreferenceOrdering(preferredTypeIds, PageRequest.of(page - 1, pageSize));
            }
        }

        return toPagedResponse(activitiesPage, page, pageSize, user);
    }

    // ── Listagem sem paginação ────────────────────────────────────────────

    public List<ActivityResponse> listAllActivities(UUID typeId, String orderBy, String order) {
        User user = currentUser();
        Sort sort = buildSort(orderBy, order);
        List<Activity> activities = typeId != null
                ? activityRepository.findByType_IdAndDeletedAtIsNullAndCompletedAtIsNull(typeId, sort)
                : activityRepository.findAllAvailable(sort);
        return activities.stream().map(a -> toActivityResponse(a, user)).toList();
    }

    // ── Atividades do criador ─────────────────────────────────────────────

    public PagedActivityResponse listCreatedByUser(int page, int pageSize) {
        User user = currentUser();
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Activity> result = activityRepository.findByCreator_Id(user.getId(), pageable);
        return toPagedResponse(result, page, pageSize, user);
    }

    public List<ActivityResponse> listAllCreatedByUser() {
        User user = currentUser();
        return activityRepository
                .findByCreator_Id(user.getId(), Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream().map(a -> toActivityResponse(a, user)).toList();
    }

    // ── Atividades do participante ────────────────────────────────────────

    public PagedActivityResponse listParticipatedByUser(int page, int pageSize) {
        User user = currentUser();
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<Activity> result = activityRepository.findByParticipantUserId(user.getId(), pageable);
        return toPagedResponse(result, page, pageSize, user);
    }

    public List<ActivityResponse> listAllParticipatedByUser() {
        User user = currentUser();
        return activityRepository.findAllByParticipantUserId(user.getId())
                .stream().map(a -> toActivityResponse(a, user)).toList();
    }

    // ── Participantes de uma atividade ────────────────────────────────────

    public List<ParticipantResponse> listParticipants(UUID activityId) {
        Activity activity = findActivityOrThrow(activityId);
        return activityParticipantRepository.findByActivityIdWithUser(activity.getId())
                .stream()
                .map(ap -> new ParticipantResponse(
                        ap.getId(),
                        ap.getUser().getId(),
                        ap.getUser().getName(),
                        ap.getUser().getAvatar(),
                        resolveParticipantStatus(ap),
                        ap.getConfirmedAt()))
                .toList();
    }

    // ── CRUD ──────────────────────────────────────────────────────────────

    @Transactional
    public ActivityResponse createActivity(String title, String description, UUID typeId,
                                           String address, MultipartFile image,
                                           LocalDateTime scheduledDate, boolean isPrivate) {
        User user = currentUser();

        ActivityType type = activityTypeRepository.findById(typeId)
                .orElseThrow(MissingFieldsException::new);

        String imageUrl = s3Service.uploadFile(image, "activities");

        Activity activity = Activity.builder()
                .title(title)
                .description(description)
                .type(type)
                .confirmationCode(ConfirmationCodeGenerator.generate())
                .image(imageUrl)
                .scheduledDate(scheduledDate)
                .createdAt(LocalDateTime.now())
                .isPrivate(isPrivate)
                .creator(user)
                .build();
        activityRepository.save(activity);

        saveAddress(activity, address);
        grantAchievement(user, "Organizador");

        return toActivityResponse(activity, user);
    }

    @Transactional
    public ActivityResponse updateActivity(UUID id, String title, String description,
                                           UUID typeId, String address, MultipartFile image,
                                           String scheduledDate, Boolean isPrivate) {
        User user = currentUser();
        Activity activity = findActivityOrThrow(id);
        requireCreator(activity, user, "Apenas o criador da atividade pode editá-la.");

        if (title != null) activity.setTitle(title);
        if (description != null) activity.setDescription(description);
        if (typeId != null) {
            ActivityType type = activityTypeRepository.findById(typeId)
                    .orElseThrow(MissingFieldsException::new);
            activity.setType(type);
        }
        if (image != null && !image.isEmpty()) {
            activity.setImage(s3Service.uploadFile(image, "activities"));
        }
        if (scheduledDate != null) {
            activity.setScheduledDate(LocalDateTime.parse(scheduledDate));
        }
        if (isPrivate != null) activity.setIsPrivate(isPrivate);

        if (address != null) {
            ActivityAddress addr = activityAddressRepository.findByActivity_Id(id)
                    .orElse(ActivityAddress.builder().activity(activity).build());
            String[] parts = address.split(",");
            addr.setLatitude(Double.parseDouble(parts[0].trim()));
            addr.setLongitude(Double.parseDouble(parts[1].trim()));
            activityAddressRepository.save(addr);
        }

        activityRepository.save(activity);
        return toActivityResponse(activity, user);
    }

    @Transactional
    public void concludeActivity(UUID id) {
        User user = currentUser();
        Activity activity = findActivityOrThrow(id);
        requireCreator(activity, user, "Apenas o criador da atividade pode concluí-la.");

        activity.setCompletedAt(LocalDateTime.now());
        activityRepository.save(activity);

        grantAchievement(user, "Mestre de Cerimônias");
    }

    @Transactional
    public void deleteActivity(UUID id) {
        User user = currentUser();
        Activity activity = findActivityOrThrow(id);
        requireCreator(activity, user, "Apenas o criador da atividade pode excluí-la.");

        activity.setDeletedAt(LocalDateTime.now());
        activityRepository.save(activity);
    }

    // ── helpers ───────────────────────────────────────────────────────────

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private Activity findActivityOrThrow(UUID id) {
        return activityRepository.findById(id)
                .filter(a -> a.getDeletedAt() == null)
                .orElseThrow(ActivityNotFoundException::new);
    }

    private void requireCreator(Activity activity, User user, String message) {
        if (!activity.getCreator().getId().equals(user.getId())) {
            throw new NotActivityCreatorException(message);
        }
    }

    private ActivityResponse toActivityResponse(Activity activity, User currentUser) {
        AddressResponse address = activityAddressRepository.findByActivity_Id(activity.getId())
                .map(a -> new AddressResponse(a.getLatitude(), a.getLongitude()))
                .orElse(null);

        long participantCount = activityParticipantRepository.countByActivity_Id(activity.getId());

        boolean isCreator = activity.getCreator().getId().equals(currentUser.getId());
        String confirmationCode = isCreator ? activity.getConfirmationCode() : null;
        String subscriptionStatus = isCreator
                ? "NOT_SUBSCRIBED"
                : resolveSubscriptionStatus(activity.getId(), currentUser.getId());

        return new ActivityResponse(
                activity.getId(),
                activity.getTitle(),
                activity.getDescription(),
                activity.getType().getName(),
                activity.getImage(),
                confirmationCode,
                participantCount,
                address,
                activity.getScheduledDate(),
                activity.getCreatedAt(),
                activity.getCompletedAt(),
                activity.getIsPrivate(),
                new CreatorResponse(
                        activity.getCreator().getId(),
                        activity.getCreator().getName(),
                        activity.getCreator().getAvatar()),
                subscriptionStatus);
    }

    private String resolveSubscriptionStatus(UUID activityId, UUID userId) {
        return activityParticipantRepository.findByActivity_IdAndUser_Id(activityId, userId)
                .map(this::resolveParticipantStatus)
                .orElse("NOT_SUBSCRIBED");
    }

    private String resolveParticipantStatus(ActivityParticipant ap) {
        if (ap.getConfirmedAt() != null) return "CONFIRMED";
        if (Boolean.TRUE.equals(ap.getApproved())) return "APPROVED";
        return "PENDING";
    }

    private PagedActivityResponse toPagedResponse(Page<Activity> page, int currentPage,
                                                   int pageSize, User user) {
        List<ActivityResponse> activities = page.getContent()
                .stream().map(a -> toActivityResponse(a, user)).toList();
        int totalPages = page.getTotalPages();
        return new PagedActivityResponse(
                currentPage,
                pageSize,
                page.getTotalElements(),
                totalPages,
                currentPage > 1 ? currentPage - 1 : 0,
                currentPage < totalPages ? currentPage + 1 : 0,
                activities);
    }

    private Sort buildSort(String orderBy, String order) {
        String field = switch (orderBy == null ? "createdAt" : orderBy) {
            case "scheduledDate" -> "scheduledDate";
            case "title" -> "title";
            default -> "createdAt";
        };
        Sort.Direction direction = "asc".equalsIgnoreCase(order)
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(direction, field);
    }

    private void saveAddress(Activity activity, String address) {
        String[] parts = address.split(",");
        activityAddressRepository.save(ActivityAddress.builder()
                .activity(activity)
                .latitude(Double.parseDouble(parts[0].trim()))
                .longitude(Double.parseDouble(parts[1].trim()))
                .build());
    }

    private void grantAchievement(User user, String achievementName) {
        achievementRepository.findByName(achievementName).ifPresent(achievement -> {
            boolean alreadyGranted = userAchievementRepository
                    .existsByUser_IdAndAchievement_Id(user.getId(), achievement.getId());
            if (!alreadyGranted) {
                userAchievementRepository.save(UserAchievement.builder()
                        .user(user).achievement(achievement).build());
            }
        });
    }
}
