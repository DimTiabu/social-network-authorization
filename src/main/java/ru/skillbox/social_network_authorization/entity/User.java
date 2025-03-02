package ru.skillbox.social_network_authorization.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "users")
@Table(name = "users", indexes = {
        @Index(name = "idx_email", columnList = "email")
})
public class User {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "account_id")
    private UUID accountId;

    @Column(unique = true)
    private String token;

    @ElementCollection(targetClass = UserRole.class, fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "roles", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private List<UserRole> roles = new ArrayList<>(
            Collections.singletonList(UserRole.ROLE_USER));
}
