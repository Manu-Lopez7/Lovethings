package neoarcadia.core.lovethings.login;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {
    // Método para encriptar una contraseña
    public static String encryptPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    // Método para verificar una contraseña con el hash
    public static boolean checkPassword(String password, String hashed) {
        return BCrypt.checkpw(password, hashed);
    }
}
