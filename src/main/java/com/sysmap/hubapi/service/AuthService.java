package com.sysmap.hubapi.service;

import com.sysmap.hubapi.dto.request.RegisterRequest;
import com.sysmap.hubapi.dto.request.SignInRequest;
import com.sysmap.hubapi.dto.response.AchievementResponse;
import com.sysmap.hubapi.dto.response.SignInResponse;
import com.sysmap.hubapi.entity.User;
import com.sysmap.hubapi.exception.AccountDeactivatedException;
import com.sysmap.hubapi.exception.DuplicateUserException;
import com.sysmap.hubapi.exception.UserNotFoundException;
import com.sysmap.hubapi.exception.WrongPasswordException;
import com.sysmap.hubapi.repository.UserAchievementRepository;
import com.sysmap.hubapi.repository.UserRepository;
import com.sysmap.hubapi.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {

    @Value("${cloud.aws.s3.public-url}")
    private String s3PublicUrl;

    @Value("${cloud.aws.s3.bucket}")
    private String s3Bucket;

    private final UserRepository userRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository,
                       UserAchievementRepository userAchievementRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.userAchievementRepository = userAchievementRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email()) || userRepository.existsByCpf(request.cpf())) {
            throw new DuplicateUserException();
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .cpf(request.cpf())
                .password(passwordEncoder.encode(request.password()))
                .avatar(s3PublicUrl + "/" + s3Bucket + "/avatars/default-avatar.png")
                .xp(0)
                .level(1)
                .build();

        userRepository.save(user);
    }

    public SignInResponse signIn(SignInRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(UserNotFoundException::new);

        if (user.getDeletedAt() != null) {
            throw new AccountDeactivatedException();
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new WrongPasswordException();
        }

        List<AchievementResponse> achievements = userAchievementRepository
                .findByUserId(user.getId())
                .stream()
                .map(ua -> new AchievementResponse(
                        ua.getAchievement().getName(),
                        ua.getAchievement().getCriterion()))
                .toList();

        return new SignInResponse(
                jwtTokenProvider.generateToken(user),
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCpf(),
                user.getAvatar(),
                user.getXp(),
                user.getLevel(),
                achievements);
    }
}
