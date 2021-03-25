package util;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/*
 * Generic class for ensuring that a method is run at most once for each
 * possible argument (key) and that any simultaneous calls for the same key
 * wait for the first run to finish.
 *
 * Use this class by creating a subclass that implements the `work()` and 
 * `rollback()` methods.
 */

public abstract class SideEffectManager {

    /*
     * Methods for subclasses to fill in
     */
    public abstract void work(String key) throws Exception;
    public abstract void rollback(String key) throws Exception;

    /*
     * Generic behaviour from this point on
     */
    protected Map<String, ReentrantLock> inProgress = new HashMap<String, ReentrantLock>();
    protected List<String> completeKeys = new ArrayList<String>();

    public boolean run(String key, int timeout)
        throws Exception
    {
        boolean needToRun = false;
        ReentrantLock myLock; // "myLock" is a poor name... it's the lock for this key.
        
        // This loop exists so that if this thread waits for a thread which dies without
        // completing then this thread will have another go, possibly doing the work
        // itself this time.
        while (true) {
            // Guarded block to ensure only no other thread starts work on the same key
            synchronized(this) {
                // If work was already done for this key, then return.
                if (completeKeys.stream().anyMatch(str -> str.equals(key))) {
                    //System.err.println("Already done");
                    return true;
                }
                
                if (inProgress.get(key) != null) {
                    // Another thread is already working on it, get a copy of the lock to wait on.
                    //System.err.println("Have to wait");
                    myLock = inProgress.get(key);
                } else {
                    // This thread needs to do the work, create a lock, lock it, and save it for other threads to find.
                    needToRun = true;
                    myLock = new ReentrantLock();
                    myLock.lock();
                    inProgress.put(key, myLock);
                }
            }
            
            if (needToRun) {
                // Do the work, if it fails then roll it back.
                // Either way, release the lock and delete from "in progress" list.
                try {
                    this.work(key);
                    //System.err.println("Work complete on key " + key + ".");
                    synchronized(this) {
                        this.completeKeys.add(key);
                    }
                    return true;
                } catch (Exception e) {
                    this.rollback(key);
                    throw e;
                } finally {
                    //System.err.println("Finally " + key + ".");
                    synchronized(this) {
                        myLock.unlock();
                        inProgress.remove(key);
                    }
                }
            }
            
            // Wait for other thread's lock
            //System.err.println("Waiting");
            myLock.lock();
            myLock.unlock();
            // Now let control loop around - will check for completeness, if not then this thread will do work etc.
        }
    }
}
