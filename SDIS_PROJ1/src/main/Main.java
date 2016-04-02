package main;

import service.PeerData;

public class Main {

	public static void main(String arg[]) throws Exception {

		System.out.println(System.getProperty("user.dir"));
		// File id = new File("2\\backup");
		// System.out.println(Extra.getFolderSize(id));
		// String dirPath =
		// "C:\\Users\\Filipe\\git\\feup-sdis\\SDIS_PROJ1\\2\\backup\\";
		// FileOutputStream file = new FileOutputStream("1.pdf");
		// ObjectInputStream out = null;
		// // TODO OR out.writeObject(chunk.getData());
		// for (int i = 1; i < 42; i++) {
		// FileInputStream fileWriter = new FileInputStream(dirPath +
		// "968F8C59A0F6016B2BDD5D969F474BF6EF55F14A148B0666DD667A43CA1139FF_" +
		// i);
		// out = new ObjectInputStream(fileWriter);
		// Chunk c = (Chunk) out.readObject();
		//
		// file.write(c.getData());
		// }
		// file.close();
		// out.close();
		String s = null;
		System.out.println("ola".equals(s));
		System.out.println("END: " + PeerData.getDiskSize());
	}
}
