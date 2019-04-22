package nachos.threads;

import java.util.LinkedList;

import nachos.machine.*;

/**
 * An implementation of condition variables that disables interrupt()s for
 * synchronization.
 *
 * <p>
 * You must implement this.
 *
 * @see	nachos.threads.Condition
 */
public class Condition2 {
    /**
     * Allocate a new condition variable.
     *
     * @param	conditionLock	the lock associated with this condition
     *				variable. The current thread must hold this
     *				lock whenever it uses <tt>sleep()</tt>,
     *				<tt>wake()</tt>, or <tt>wakeAll()</tt>.
     */
    public Condition2(Lock conditionLock) {
	this.conditionLock = conditionLock;
    }
    
    LinkedList<KThread> waitinQueue = new LinkedList<KThread>();

    /**
     * Atomically release the associated lock and go to sleep on this condition
     * variable until another thread wakes it using <tt>wake()</tt>. The
     * current thread must hold the associated lock. The thread will
     * automatically reacquire the lock before <tt>sleep()</tt> returns.
     */
    public void sleep() {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());

	//boolean = busy
	boolean status = Machine.interrupt().disable();
	//add thread to waitQueue to wake later
	waitinQueue.add(KThread.currentThread());
	conditionLock.release();
	//current threads to sleep
	KThread.sleep();
	conditionLock.acquire();
	Machine.interrupt().restore(status);
	
    }

    /**
     * Wake up at most one thread sleeping on this condition variable. The
     * current thread must hold the associated lock.
     */
    public void wake() {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	
	//status here defined is the initial starting status 
	boolean status = Machine.interrupt().disable();
    KThread wakeThread = waitinQueue.poll();
    if (wakeThread != null) {
    	// ready the thread
    		wakeThread.ready();
    	}
    //restore interrupt
    Machine.interrupt().restore(status);
    }

    /**
     * Wake up all threads sleeping on this condition variable. The current
     * thread must hold the associated lock.
     */
    public void wakeAll() {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	
	boolean status = Machine.interrupt().disable();
	KThread wakeThread = waitinQueue.poll();
	// if queue is not empty, threads are ready
	while(wakeThread != null ) {
		wakeThread.ready();
		break;
		
    }
	 //restore interrupt
    Machine.interrupt().restore(status);
   }

    private Lock conditionLock;
}
