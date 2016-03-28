package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import chunk.Chunk;

public class Main {

	public static void main(String arg[]) throws IOException, ClassNotFoundException {

		System.out.println(System.getProperty("user.dir"));
		String dirPath = "C:\\Users\\Filipe\\git\\feup-sdis\\SDIS_PROJ1\\2\\backup";
		FileInputStream fileWriter = new FileInputStream(dirPath + File.separator + "F4A845409A1F6E88E82778B9DF1CC7465BB5FB97CF6EADFB5350DE8B42C749A7_1");
		ObjectInputStream out = new ObjectInputStream(fileWriter);
		// TODO OR out.writeObject(chunk.getData());
		Chunk c = (Chunk) out.readObject();
		fileWriter.close();
		out.close();
		System.out.println(c);
		System.out.println("END");
	}
}
