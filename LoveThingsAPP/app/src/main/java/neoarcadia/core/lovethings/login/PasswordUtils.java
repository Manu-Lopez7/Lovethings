package neoarcadia.core.lovethings.login;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {
    // Método encriptacion contraseña
    public static String encryptPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    // Método verificacion contraseña con el hash
    public static boolean checkPassword(String password, String hashed) {
        return BCrypt.checkpw(password, hashed);
    }
}
