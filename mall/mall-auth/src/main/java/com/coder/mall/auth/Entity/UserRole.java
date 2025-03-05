package com.coder.mall.auth.Entity;
import jakarta.persistence.*;
import lombok.Data;
@Entity
@Table(name = "sys_user_role")
@Data
public class UserRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String roleCode;
}