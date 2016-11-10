import java.util.*;

public class Scheduler extends Thread
{
    private Vector[] queueArr;
    private int[] sliceCountArr;
    private int timeSlice;
    private static final int DEFAULT_TIME_SLICE = 500;

    // New data added to p161 
    private boolean[] tids; // Indicate which ids have been used
    private static final int DEFAULT_MAX_THREADS = 10000;

    // A new feature added to p161 
    // Allocate an ID array, each element indicating if that id has been used
    private int nextId = 0;
    private void initTid( int maxThreads ) {
	    tids = new boolean[maxThreads];
	    for ( int i = 0; i < maxThreads; i++ )
	        tids[i] = false;
    }

    // A new feature added to p161 
    // Search an available thread ID and provide a new thread with this ID
    private int getNewTid( ) {
        for ( int i = 0; i < tids.length; i++ ) {
            int tentative = ( nextId + i ) % tids.length;
            if ( tids[tentative] == false ) {
                tids[tentative] = true;
                nextId = ( tentative + 1 ) % tids.length;
                return tentative;
            }
        }
        return -1;
    }

    // A new feature added to p161 
    // Return the thread ID and set the corresponding tids element to be unused
    private boolean returnTid( int tid ) {
        if ( tid >= 0 && tid < tids.length && tids[tid] == true ) {
            tids[tid] = false;
            return true;
        }
        return false;
    }

    // A new feature added to p161 
    // Retrieve the current thread's TCB from the queue
    public TCB getMyTcb( ) {
        Thread myThread = Thread.currentThread( ); // Get my thread object
        synchronized( queueArr ) {
            for (int i = 0; i < 3; i++) {
                for ( int j = 0; j < queueArr[i].size( ); j++ ) {
                    TCB tcb = ( TCB )queueArr[i].elementAt( j );
                    Thread thread = tcb.getThread( );
                    if ( thread == myThread ) // if this is my TCB, return it
                        return tcb;
                }
            }  
        }
        return null;
    }

    // A new feature added to p161 
    // Return the maximal number of threads to be spawned in the system
    public int getMaxThreads( ) {
	    return tids.length;
    }

    public Scheduler( ) {
        timeSlice = DEFAULT_TIME_SLICE;
        queueArr = new Vector[] { new Vector(), new Vector(), new Vector() };
        sliceCountArr = new int[3];
        initTid( DEFAULT_MAX_THREADS );
    }

    public Scheduler( int quantum ) {
        timeSlice = quantum;
        queueArr = new Vector[] { new Vector(), new Vector(), new Vector() };
        sliceCountArr = new int[3];
        initTid( DEFAULT_MAX_THREADS );
    }

    // A new feature added to p161 
    // A constructor to receive the max number of threads to be spawned
    public Scheduler( int quantum, int maxThreads ) {
        timeSlice = quantum;
        queueArr = new Vector[] { new Vector(), new Vector(), new Vector() };
        sliceCountArr = new int[3];
        initTid( maxThreads );
    }

    private void schedulerSleep( ) {
        try {
            Thread.sleep( timeSlice );
        } catch ( InterruptedException e ) {
        }
    }

    // A modified addThread of p161 example
    public TCB addThread( Thread t ) {
        TCB parentTcb = getMyTcb( ); // get my TCB and find my TID
        int pid = ( parentTcb != null ) ? parentTcb.getTid( ) : -1;
        int tid = getNewTid( ); // get a new TID
        if ( tid == -1)
            return null;
        TCB tcb = new TCB( t, tid, pid ); // create a new TCB
        queueArr[0].add( tcb ); // new threads go to queue 0
        return tcb;
    }

    // A new feature added to p161
    // Removing the TCB of a terminating thread
    public boolean deleteThread( ) {
        TCB tcb = getMyTcb( ); 
        if ( tcb!= null )
            return tcb.setTerminated( );
        else
            return false;
    }

    public void sleepThread( int milliseconds ) {
        try {
            sleep( milliseconds );
        } catch ( InterruptedException e ) { }
    }
    
    public int getActiveQueue() {
        for (int i = 0; i < 3; i++) {
            if (!queueArr[i].isEmpty()) {
                return i; // Return the first queue found containing threads
            }
        }
        return -1; // All queues empty
    }
    
    // A modified run of p161
    public void run( ) {
        Thread current = null;        
        while ( true ) {
            try {
                // get the next TCB and its thread
                int qid = getActiveQueue();
                if ( qid < 0 )
                    continue;
                TCB currentTCB = (TCB)queueArr[qid].firstElement( );
                if ( currentTCB.getTerminated( ) == true ) {
                    queueArr[qid].remove( currentTCB );
                    returnTid( currentTCB.getTid( ) );
                    continue;
                }
                current = currentTCB.getThread( );
                if ( current != null ) {
                    if ( current.isAlive( ) )
                        current.resume();
                    else {
                        // Spawn must be controlled by Scheduler
                        // Scheduler must start a new thread
                        current.start( );
                    }
                }
                
                schedulerSleep( );
                // System.out.println("* * * Context Switch * * * ");
                
                sliceCountArr[qid]++; // Keep track of how many default slice times were processed for each queue

                synchronized ( queueArr ) {
                    if ( current != null && current.isAlive( ) )
                        current.suspend();
                    
                        if (qid == 0) { 
                            // A thread in queue 0 only gets one slice, no need to check slice count
                            queueArr[0].remove(currentTCB); // remove thread from queue 0
                            queueArr[1].add(currentTCB); // bump it to queue 1
                        }
                        else if (qid == 1) {
                            if (sliceCountArr[1] % 2 == 0) { // if 2 slices have been processed
                                queueArr[1].remove(currentTCB); // remove thread from queue 1
                                queueArr[2].add(currentTCB); // bump it to queue 2
                            }
                        }
                        else { // qid == 2
                            if (sliceCountArr[2] % 4 == 0) { // if 4 slices have been processed
                                queueArr[2].remove(currentTCB); // remove thread from queue 2
                                queueArr[2].add(currentTCB); // bump it back to the end of queue 2
                            }
                        }                    
                }
            } catch ( NullPointerException e3 ) { };
        }
    }
}
