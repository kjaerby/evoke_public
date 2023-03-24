package moe.evoke.application.backend.auth;

import moe.evoke.application.backend.db.Database;
import moe.evoke.application.backend.db.UserRoles;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.List;

public class UserAuthentication {

    private static byte[] createSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);

        return salt;
    }

    public static void registerUser(String username, String password, String email) {

        long userID = Database.instance().getIDForUser(username);
        if (userID > -1) {
            return;
        }

        byte[] salt = createSalt();
        byte[] hash = getPasswordHash(password, salt);
        Database.instance().createUser(username, hash, salt, email);
    }

    public static boolean authenticateUser(String username, String password) {
        byte[] saltFromDB = Database.instance().getSaltForUser(username);
        byte[] hashFromDB = Database.instance().getPasswordForUser(username);

        if (saltFromDB == null || hashFromDB == null)
            return false;

        byte[] hash = getPasswordHash(password, saltFromDB);
        if (hash == null)
            return false;

        return Arrays.equals(hashFromDB, hash);
    }

    private static byte[] getPasswordHash(String password, byte[] salt) {
        try {
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

            return factory.generateSecret(spec).getEncoded();
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void updatePasswordForUser(String username, String password) {
        byte[] salt = createSalt();
        byte[] hash = getPasswordHash(password, salt);

        Database.instance().updatePasswordForUser(username, hash, salt);
    }

    public static void updateRolesForUser(String username, List<UserRoles> roles) {
        Database.instance().removeAllRolesForUser(username);

        for (UserRoles role : roles) {
            Database.instance().addRoleToUser(username, role);
        }
    }

    public static void updateEmailForUser(String username, String email) {
        Database.instance().updateEmailForUser(username, email);
    }
}
