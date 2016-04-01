package main;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;

import chunk.Chunk;

public class Main {

	public static void main(String arg[]) throws Exception {

		System.out.println(System.getProperty("user.dir"));
		String dirPath = "C:\\Users\\Filipe\\git\\feup-sdis\\SDIS_PROJ1\\2\\backup\\";

		FileOutputStream file = new FileOutputStream("12.pdf");
		ObjectInputStream out = null;
		// TODO OR out.writeObject(chunk.getData());
		for (int i = 1; i < 12610; i++) {
			FileInputStream fileWriter = new FileInputStream(dirPath + "AE9CC210D78F37587295CCA57B109751B6C79F9359FD55DEEEF41A29CBC59065_" + i);
			out = new ObjectInputStream(fileWriter);
			Chunk c = (Chunk) out.readObject();

			file.write(c.getData());
		}
		file.close();
		out.close();
		System.out.println("END");
	}
}
