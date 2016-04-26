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
	
	/**
	 * Hash a text using SHA-256 cryptographic function and encodes as 64 ASCII character sequence
	 * 
	 * @param toHash text to be hashed
	 * @return text hashed
	 */
	public static String SHA256(String toHash) {
		MessageDigest md;
		String hashed = null;
		try {
			md = MessageDigest.getInstance("SHA-256");
			md.update(toHash.getBytes("UTF-8"));

			hashed = DatatypeConverter.printHexBinary(md.digest());
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			System.out.println("SHA-256 not found or UTF-8 not found");
		}
		return hashed;
	}

	/** 
	 * Create a directory in current working directory if not already exists
	 * 
	 * @param dirName name of directory to be created
	 * @return the String of the path created
	 * @throws IOException
	 */
	public static String createDirectory(String dirName) throws IOException {
		Path path = Paths.get(workingDirPath + File.separator + dirName);

		if (!(Files.exists(path) && Files.isDirectory(path))) {
			Files.createDirectory(path);

		}
		return path.toString();
	}

	/**
	 * Removes from an array of Strings any empty String
	 * 
	 * @param toErase an array of string contain possible empty strings
	 * @return the array without empty Strings
	 */
	public static String[] eraseEmpty(String[] toErase) {
		ArrayList<String> toAdd = new ArrayList<String>();

		for (String ele : toErase) {
			if (!ele.isEmpty())
				toAdd.add(ele);
		}
		return toAdd.toArray(new String[toAdd.size()]);
	}

	/**
	 * Checks is value parsed is a number
	 * 
	 * @param str String with value to be tested
	 * @return true if number, false otherwise
	 */
	public static boolean isNumeric(String str) {
		try {
			Integer.parseInt(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	/**
	 * @return current working directory path
	 */
	public static String getWorkingDirPath() {
		return workingDirPath;
	}

	/**
	 * sets the current working directory path
	 * 
	 * @param workingDirPath the name of current working directory path
	 */
	public static void setWorkingDirPath(String workingDirPath) {
		Extra.workingDirPath = workingDirPath;
	}

	/**
	 * delete any empty folder recursively
	 * 
	 * @param file - root folder
	 */
	public static void recursiveDelete(File file) {

		if (!file.exists())
			return;

		if (file.isDirectory()) {
			for (File f : file.listFiles())
				recursiveDelete(f);

			if (file.list().length == 0)
				file.delete();
		}

	}

	/**
	 * Gets the folder Size, obtaining the size of any files and directories inside
	 * 
	 * @param folderName name of folder
	 * @return size of folder
	 */
	public static long getFolderSize(String folderName) {

		if (folderName.isEmpty())
			return 0;

		File folder = new File(folderName);
		return getFolderSize(folder);
	}

	/**
	 * Gets the folder Size, obtaining the size of any files and directories inside
	 * 
	 * @param folder folder file
	 * @return size of folder
	 */
	public static long getFolderSize(File folder) {
		long size = 0;
		if (folder.isDirectory())
			for (File file : folder.listFiles()) {
				if (file.isDirectory())
					size += Extra.getFolderSize(file.getName());
				else
					size += file.length();
			}
		else
			System.out.println("File is not a directory");
		return size;
	}
}
