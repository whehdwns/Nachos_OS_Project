package nachos.threads;

import nachos.machine.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {
    public Lock lock= new Lock();
    public Condition speaker= new Condition (lock);
    public Condition listener= new Condition (lock);
	private int number;
	private boolean activate= false;
//	private Condition ;

	/**
     * Allocate a new communicator.
     */
	/*
	 *  initialize the speak listen
	 *  	lock
	 *  wake speaker
	 *  wake listener
	 *  sleep speaker
	 *  wake listener
	 *  
	 *  
	 *  
	 *  speaker (listener) will be locked until speaker received a message
	 *  	one message by one listener using condition variable
	 */
    public Communicator() {
    }

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     *
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param	word	the integer to transfer.
     */
    /*
     * message = word
     * if no message/word / 
     * 		speak
     * 			sleep /block
     * 		listen
     * 			sleep/ block
     * acquire lock release the lock for speak class and listen class
     * wait for thread to listen
     * speak() atomically waits until listen() is called
     * 		if there is no listener value, then the speaker will wait(sleep)
     * 			speaker(listener) will be blocked until it has sent(received) a call (word)
     * 		if there is listener value(word), then the speaker will wake.
     * 			if there is no listener value, then the speak will sleep
     */
    
    public void speak(int word) {
    	lock.acquire();
    	while(activate) {
    		speaker.sleep();
    	}
    	number=word;
    	activate=true;
    	listener.wake();
    	speaker.sleep();
    	lock.release();
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */   
    /*
     * wait for thread for speaker
     * listen()waits until speak()is called, at which point 
     * the transfer is made, and both can return
     * listen is similar to speak() but listen() wait until speak() is called
     * listen returns word
     */
    
   public int listen() {
    	lock.acquire();
    	while(!activate) {
    		listener.sleep();
    	}
    	activate=false;
    	int num = number;
    	speaker.wakeAll();
    	lock.release();
    	return num;
   }
}