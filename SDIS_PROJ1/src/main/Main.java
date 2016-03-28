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
		FileInputStream fileWriter = new FileInputStream(dirPath + File.separator + "968F8C59A0F6016B2BDD5D969F474BF6EF55F14A148B0666DD667A43CA1139FF_1");
		ObjectInputStream out = new ObjectInputStream(fileWriter);
		// TODO OR out.writeObject(chunk.getData());
		Chunk c = (Chunk) out.readObject();
		fileWriter.close();
		out.close();
		System.out.println(c);
		System.out.println("END");
	}
}
