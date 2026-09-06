package utilities;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

	// Why this is a static method with no constructor/instance needed: same
	// reasoning as ConnectionFactory — this class holds pure, stateless
	// utility functions. Nobody should ever need "new PasswordUtil()" just
	// to hash a string.
	//
	// Why BCrypt.gensalt() with no arguments: this generates a random salt
	// at a default "work factor" (computational cost) of 10 rounds — strong
	// enough for this project's purposes without being so slow it noticeably
	// delays registration. BCrypt automatically embeds this salt INSIDE the
	// returned hash string itself, so you never need to store the salt separately.
	public static String hashPassword(String plainPassword) {
		return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
	}

	// Why this takes the plain password AND the stored hash, returning a
	// boolean: you can never "decrypt" a BCrypt hash back to the original
	// password (that's the whole point of hashing) — the only way to check
	// a login attempt is to hash the ATTEMPT the same way and let BCrypt
	// internally compare it against the stored hash. BCrypt.checkpw()
	// handles extracting the original salt from the stored hash automatically.
	public static boolean checkPassword(String plainPassword, String hashedPassword) {
		return BCrypt.checkpw(plainPassword, hashedPassword);
	}
}