package service;


import java.util.concurrent.ConcurrentLinkedQueue;
import messages.Message;



public class Processor extends Thread{

	private ConcurrentLinkedQueue<Message> messageQueue = new ConcurrentLinkedQueue<Message>();
	
}
