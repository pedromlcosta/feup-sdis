package extra;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.xml.bind.DatatypeConverter;

public class Extra {
	public Extra() {
	}

	public static String SHA256(String toHash) {
		MessageDigest md;
		String hashed = null;
		try {
			md = MessageDigest.getInstance("SHA-256");

			// Change this to "UTF-16" if needed
			md.update(toHash.getBytes("UTF-8"));

			hashed = DatatypeConverter.printHexBinary(md.digest());
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return hashed;
	}
}
