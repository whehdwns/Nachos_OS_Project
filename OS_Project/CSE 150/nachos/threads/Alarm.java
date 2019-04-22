package nachos.threads;

import java.util.Comparator;
import java.util.PriorityQueue;

import nachos.machine.*;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
    /**
     * Allocate a new Alarm. Set the machine's timer interrupt handler to this
     * alarm's callback.
     *
     * <p><b>Note</b>: Nachos will not function correctly with more than one
     * alarm.
     */
    public Alarm() {
	Machine.timer().setInterruptHandler(new Runnable() {
		public void run() { timerInterrupt(); }
	    });
    }
    
    threadComparator comparator = new threadComparator();
    PriorityQueue<waitingQueue> priorityThreadQueue = new PriorityQueue<waitingQueue>(1, comparator); // Queue to hold threads

    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the current
     * thread to yield, forcing a context switch if there is another thread
     * that should be run.
     */
    public void timerInterrupt() 
    {
    	boolean interruptStatus = Machine.interrupt().setStatus(false); //Disable interrupt and store previous state
    	
    	while(!priorityThreadQueue.isEmpty() && priorityThreadQueue.peek().threadWaitTime <= Machine.timer().getTime()) //If queue not empty and thread's wait time is over
    	{
    		priorityThreadQueue.peek().thread.ready(); //Move to scheduler's ready queue
    		priorityThreadQueue.poll(); //Remove from wait queue
    	}
    	
    	KThread.currentThread().yield();
    	Machine.interrupt().setStatus(interruptStatus); //Enable interrupt
    }
    
    class waitingQueue //Class to hold current thread & wait time
    {
    	KThread thread;
    	Long threadWaitTime;
    }
    
    class threadComparator implements Comparator<waitingQueue>
    {
		public int compare(waitingQueue t1, waitingQueue t2) {
			return t1.threadWaitTime.compareTo(t2.threadWaitTime);
		}
    }
    

    /**
     * Put the current thread to sleep for at least <i>x</i> ticks,
     * waking it up in the timer interrupt handler. The thread must be
     * woken up (placed in the scheduler ready set) during the first timer
     * interrupt where
     *
     * <p><blockquote>
     * (current time) >= (WaitUntil called time)+(x)
     * </blockquote>
     *
     * @param	x	the minimum number of clock ticks to wait.
     *
     * @see	nachos.machine.Timer#getTime()
     */
    public void waitUntil(long x) 
    {
    	long wakeTime = Machine.timer().getTime() + x;
		boolean interruptStatus = Machine.interrupt().setStatus(false); //Disable interrupt and store previous state
		waitingQueue currentThreadCopy = new waitingQueue(); //Instantiate new class to store current thread and wait time
		currentThreadCopy.threadWaitTime = wakeTime; //Assign time
		currentThreadCopy.thread = KThread.currentThread(); //Assign current thread
		priorityThreadQueue.add(currentThreadCopy); //Add to priority queue
		KThread.sleep(); //Put thread to sleep
		Machine.interrupt().setStatus(interruptStatus); //Enable interrupt
    }
    
}
