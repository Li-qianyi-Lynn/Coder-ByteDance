package com.coder.mall.auth.repository;


import com.coder.mall.auth.Entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    List<UserRole> findByUserId(Long userId);
    @Query("SELECT ur.roleCode FROM UserRole ur WHERE ur.userId = ?1")
    List<String> findRoleCodesByUserId(Long userId);
}