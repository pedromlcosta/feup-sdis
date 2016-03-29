package extra;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.xml.bind.DatatypeConverter;

public class Extra {
	private static String workingDirPath = System.getProperty("user.dir");

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

	// TODO check if it is what we want Always relative to the place the
	// function is called
	public static String createDirectory(String dirName) throws IOException {
		Path path = Paths.get(workingDirPath + File.separator + dirName);

		if (!(Files.exists(path) && Files.isDirectory(path))) {
			Files.createDirectory(path);

		}
		return path.toString();
	}

	public static String[] eraseEmpty(String[] toErase) {
		ArrayList<String> toAdd = new ArrayList<String>();

		for (String ele : toErase) {
			if (!ele.isEmpty())
				toAdd.add(ele);
		}
		return toAdd.toArray(new String[toAdd.size()]);
	}

	// TODO without the double i, it should still work
	public static boolean isNumeric(String str) {
		try {
			Integer.parseInt(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	public static String getWorkingDirPath() {
		return workingDirPath;
	}

	public static void setWorkingDirPath(String workingDirPath) {
		Extra.workingDirPath = workingDirPath;
	}
	
	public static void recursiveDelete(File file) {
        
		if (!file.exists())
            return;
        
		if (file.isDirectory()) {
            for (File f : file.listFiles())
                recursiveDelete(f);
            
            if(file.list().length == 0)
    			file.delete();
        }
		
    }
}
