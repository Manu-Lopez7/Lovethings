package core.neoarcadia.lovethings;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class encoder {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String adminPassword = encoder.encode("admin");
        String moderatorPassword = encoder.encode("moderador");
        String userPassword = encoder.encode("user");

        System.out.println("Admin Password: " + adminPassword);
        System.out.println("Moderator Password: " + moderatorPassword);
        System.out.println("User Password: " + userPassword);
    }
}