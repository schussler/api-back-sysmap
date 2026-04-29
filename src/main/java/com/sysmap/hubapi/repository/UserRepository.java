package com.sysmap.hubapi.repository;

import com.sysmap.hubapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    // TODO: findByEmail, existsByEmail, existsByCpf
}
