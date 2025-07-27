package in.lazygod.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import in.lazygod.enums.Role;
import in.lazygod.enums.Verification;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "app_user")
public class User {
    @Id
    private String userId;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private Verification verification;

    @JsonIgnore
    private String verificationCode;

    private String fullName;
    private boolean isActive;

    private LocalDateTime createdOn;
    @JsonIgnore
    private LocalDateTime updatedOn;

    @JsonIgnore
    @Column(nullable = false)
    private String password; // hashed
}
