package core.neoarcadia.lovethings.controllers;

import core.neoarcadia.lovethings.models.ERole;
import core.neoarcadia.lovethings.models.Role;
import core.neoarcadia.lovethings.models.User;
import core.neoarcadia.lovethings.repository.RoleRepository;
import core.neoarcadia.lovethings.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @PostMapping("/assign-role")
    public ResponseEntity<?> assignRoleToUser(
            @RequestParam String username,
            @RequestParam String roleName // Ej: "admin", "mod", "user"
    ) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Role role;
        switch (roleName.toLowerCase()) {
            case "admin":
                role = roleRepository.findByName(ERole.ROLE_ADMIN)
                        .orElseThrow(() -> new RuntimeException("Role not found"));
                break;
            case "mod":
                role = roleRepository.findByName(ERole.ROLE_MODERATOR)
                        .orElseThrow(() -> new RuntimeException("Role not found"));
                break;
            default:
                role = roleRepository.findByName(ERole.ROLE_USER)
                        .orElseThrow(() -> new RuntimeException("Role not found"));
        }

        Set<Role> roles = new HashSet<>();
        roles.add(role); // Reemplaza roles actuales (si quieres sumar, añade en vez de reemplazar)
        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok("Rol asignado correctamente.");
    }
    /*
    @GetMapping("/users")
    public ResponseEntity<List<String>> getAllUsernames() {
        List<String> usernames = userRepository.findAll()
                .stream()
                .map(User::getUsername)
                .collect(Collectors.toList());

        return ResponseEntity.ok(usernames);
    }

     */
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsernames() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Auth: " + auth);
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("NO AUTH");
        }

        List<String> usernames = userRepository.findAll()
                .stream()
                .map(User::getUsername)
                .collect(Collectors.toList());

        return ResponseEntity.ok(usernames);
    }
}

