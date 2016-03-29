package main;

import java.io.*;

import chunk.Chunk;

public class Main {

	public static void main(String arg[]) throws IOException, ClassNotFoundException {

		System.out.println(System.getProperty("user.dir"));
		String dirPath = "C:\\Users\\Filipe\\git\\feup-sdis\\SDIS_PROJ1\\2\\backup\\";
		FileInputStream fileWriter = new FileInputStream(dirPath + "61BAB190A1ABF5DB7579059DDC49A9E2E7A42ECB6D82D1F34FABD40C3944C844_1");
		ObjectInputStream out = new ObjectInputStream(fileWriter);
		// TODO OR out.writeObject(chunk.getData());
		Chunk c = (Chunk) out.readObject();
		FileOutputStream file = new FileOutputStream("t12.png");
		file.write(c.getData());
		file.close();
		out.close();
		System.out.println(c);
		System.out.println("END");
	}
}
