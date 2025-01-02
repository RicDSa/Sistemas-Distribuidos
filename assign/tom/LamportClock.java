package ds.assign.tom;

/**
 * This class represents a Lamport Clock, which is used to order events in a distributed system.
 */
public class LamportClock {
    private int time = 0;

    /**
     * Constructs a LamportClock with the specified initial time.
     *
     * @param time the initial time
     */
    public LamportClock(int time){
        this.time = time;
    }


    /**
     * Returns the current time of the Lamport Clock.
     *
     * @return the current time
     */
    public int getTime() {
        return time;
    }

    /**
     * Updates the Lamport Clock with the given timestamp.
     * The clock time is set to the maximum of the current time and the given timestamp, plus one.
     *
     * @param timestamp the timestamp to update the clock with
     */
    public synchronized void update(int timestamp) {
        this.time = Math.max(this.time, timestamp) + 1;
    }


    /**
     * Sets the time of the Lamport Clock to the specified value.
     *
     * @param time the new time
     */
    public void setTime(int time) {
        this.time = time;
    }


     /**
     * Increments the time of the Lamport Clock by one.
     */
    public void increment(){
        this.time++;
    }
}
