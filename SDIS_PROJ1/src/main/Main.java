package main;

import java.io.IOException;

public class Main {

	public static void main(String arg[]) throws IOException, ClassNotFoundException {

		System.out.println(System.getProperty("user.dir"));
		new ServerThread().start();
		System.out.println(System.getProperty("END"));
	}
}
