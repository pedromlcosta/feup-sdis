package service;


import java.util.concurrent.LinkedBlockingQueue;

import messages.Message;



public class Processor extends Thread{

	private LinkedBlockingQueue<Message> messageQueue = new LinkedBlockingQueue<Message>();
	
}
