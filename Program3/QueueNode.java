import java.util.Vector;

public class QueueNode {
    private Vector<Integer> parents;

    public QueueNode() {
        parents = new Vector<>();
    }
    
    // puts a thread to sleep
    public synchronized int sleep() {
        if (parents.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException e) {}
            // removes the thread from active threads once it's been woken up
            return parents.remove(0);
        }       
        return -1; 
    }
    
    // wakes up a thread
    public synchronized void wakeup(int pid) {
        // adds the thread to the list of active threads
        parents.add(pid);
        // wakes up the thread who is waiting
        notify();
    }
}