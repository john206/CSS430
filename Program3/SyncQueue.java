public class SyncQueue {
    private QueueNode[] queue;
    private final int DEFAULT_COND_MAX = 10;

    public SyncQueue() {
        initialize(DEFAULT_COND_MAX);
    }

    public SyncQueue(int condMax) {
        initialize(condMax);
    }
    
    private void initialize(int condMax) {
        queue = new QueueNode[condMax];
        for (int i = 0; i < condMax; i++) {
            queue[i] = new QueueNode();
        }
    }
    
    // makes a thread sleep until a condition is satisfied
    public int enqueueAndSleep(int condition) {
        if (condition >= 0 && condition < queue.length) {
            return queue[condition].sleep();
        }
        else {
            return -1;
        }
    }
    
    // awakens a single thread when a condition is satisfied
    public void dequeueAndWakeup(int condition, int pid) {
        if (condition >= 0 && condition < queue.length) {
            queue[condition].wakeup(pid);
        }
    }
    
    // awakens a single thread when a condition is satisfied
    public void dequeueAndWakeup(int condition) {
        dequeueAndWakeup(condition, 0);
    }
}