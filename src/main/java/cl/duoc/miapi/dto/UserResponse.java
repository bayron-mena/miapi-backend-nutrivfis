package cl.duoc.miapi.dto;

import cl.duoc.miapi.model.User;

import java.time.LocalDate;

// Usuario tal como lo ve el frontend: nunca incluye la contraseña ni su hash
public record UserResponse(
        Long id,
        String name,
        String lastName,
        String email,
        LocalDate birthDate,
        String phone,
        String address,
        String region,
        String comuna,
        String gender,
        String role,
        boolean active,
        boolean locked,
        int failedAttempts
) {
    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getName(), u.getLastName(), u.getEmail(),
                u.getBirthDate(), u.getPhone(), u.getAddress(), u.getRegion(), u.getComuna(),
                u.getGender(), u.getRole().name(), u.isActive(), u.isLocked(), u.getFailedAttempts());
    }
}
