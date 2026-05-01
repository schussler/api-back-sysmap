package com.sysmap.hubapi.service;

import com.sysmap.hubapi.dto.request.UpdateUserRequest;
import com.sysmap.hubapi.dto.response.AchievementResponse;
import com.sysmap.hubapi.dto.response.AvatarResponse;
import com.sysmap.hubapi.dto.response.PreferenceResponse;
import com.sysmap.hubapi.dto.response.UserResponse;
import com.sysmap.hubapi.entity.Achievement;
import com.sysmap.hubapi.entity.Preference;
import com.sysmap.hubapi.entity.User;
import com.sysmap.hubapi.entity.UserAchievement;
import com.sysmap.hubapi.exception.DuplicateUserException;
import com.sysmap.hubapi.exception.MissingFieldsException;
import com.sysmap.hubapi.repository.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PreferenceRepository preferenceRepository;
    private final ActivityTypeRepository activityTypeRepository;
    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final S3Service s3Service;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PreferenceRepository preferenceRepository,
                       ActivityTypeRepository activityTypeRepository,
                       AchievementRepository achievementRepository,
                       UserAchievementRepository userAchievementRepository,
                       S3Service s3Service,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.preferenceRepository = preferenceRepository;
        this.activityTypeRepository = activityTypeRepository;
        this.achievementRepository = achievementRepository;
        this.userAchievementRepository = userAchievementRepository;
        this.s3Service = s3Service;
        this.passwordEncoder = passwordEncoder;
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public UserResponse getAuthenticatedUser() {
        User user = currentUser();
        return toUserResponse(user);
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<PreferenceResponse> getPreferences() {
        User user = currentUser();
        return preferenceRepository.findByUserId(user.getId()).stream()
                .map(p -> new PreferenceResponse(
                        p.getType().getId(),
                        p.getType().getName(),
                        p.getType().getDescription()))
                .toList();
    }

    @Transactional
    public void definePreferences(List<UUID> typeIds) {
        User user = currentUser();

        var types = activityTypeRepository.findAllById(typeIds);
        if (types.size() != typeIds.size()) {
            throw new MissingFieldsException();
        }

        preferenceRepository.deleteByUser(user);

        List<Preference> preferences = types.stream()
                .map(type -> Preference.builder().user(user).type(type).build())
                .toList();
        preferenceRepository.saveAll(preferences);
    }

    @Transactional
    public AvatarResponse updateAvatar(MultipartFile file) {
        User user = currentUser();
        boolean isFirstAvatar = "default-avatar".equals(user.getAvatar());

        String url = s3Service.uploadFile(file, "avatars");
        user.setAvatar(url);
        userRepository.save(user);

        if (isFirstAvatar) {
            grantAchievement(user, "Fotogênico");
        }

        return new AvatarResponse(url);
    }

    @Transactional
    public UserResponse updateUser(UpdateUserRequest request) {
        User user = currentUser();

        if (request.name() != null) {
            user.setName(request.name());
        }
        if (request.email() != null && !request.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.email())) {
                throw new DuplicateUserException();
            }
            user.setEmail(request.email());
        }
        if (request.password() != null) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        userRepository.save(user);
        return toUserResponse(user);
    }

    @Transactional
    public void deactivate() {
        User user = currentUser();
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    // ── helpers ───────────────────────────────────────────────────────────

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private UserResponse toUserResponse(User user) {
        List<AchievementResponse> achievements = userAchievementRepository
                .findByUserId(user.getId()).stream()
                .map(ua -> new AchievementResponse(
                        ua.getAchievement().getName(),
                        ua.getAchievement().getCriterion()))
                .toList();

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCpf(),
                user.getAvatar(),
                user.getXp(),
                user.getLevel(),
                achievements);
    }

    private void grantAchievement(User user, String achievementName) {
        achievementRepository.findByName(achievementName).ifPresent(achievement -> {
            boolean alreadyGranted = userAchievementRepository
                    .existsByUser_IdAndAchievement_Id(user.getId(), achievement.getId());
            if (!alreadyGranted) {
                userAchievementRepository.save(
                        UserAchievement.builder()
                                .user(user)
                                .achievement(achievement)
                                .build());
            }
        });
    }
}
