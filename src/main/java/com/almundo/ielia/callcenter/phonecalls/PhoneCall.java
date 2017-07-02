package com.almundo.ielia.callcenter.phonecalls;

import java.util.Random;
import java.util.function.Consumer;

/**
 * <p>A Phone Call.</p>
 * <p>Note: Using synchronized and volatile members instead of {@link java.util.concurrent.locks.ReadWriteLock} because
 * of simplicity. In production conditions, there should be a stress/load test deciding this.</p>
 */
public class PhoneCall {
    /**
     * A phone call starts on hold. This is the way it gets to the dispatcher. Consider this state to be something like
     * a musical "on hold" tune.
     */
    private volatile CallState state = CallState.ON_HOLD;

    // The following instance members are set here for the purposes of this demo.
    private volatile Thread callThread;

    private int MAX_CALL_DURATION = 10000;
    private int MIN_CALL_DURATION = 5000;
    private int CALL_DURATION_BOUNDS_SPAN = MAX_CALL_DURATION - MIN_CALL_DURATION + 1;

    public CallState getState() {
        return state;
    }

    /**
     * Hangs up--finishes--the call.
     */
    public synchronized void hangUp() {
        state = CallState.FINISHED;
        if (callThread != null) {
            callThread.interrupt();
            callThread = null;
        }
    }

    /**
     * Picks up the call.
     * Could receive the Employee picking up the call as an argument, for logging purposes.
     */
    public void pickUp() {
        if (state != CallState.FINISHED) {
            synchronized (this) {
                if (state != CallState.FINISHED) {
                    state = CallState.ACTIVE;
                }
            }
        }
    }

    /**
     * Waits for the call to finish.
     * If called after call has hung up, it will return immediately.
     */
    public void waitFinish() {
        if (state != CallState.FINISHED) {
            synchronized (this) {
                callThread = Thread.currentThread();
            }
            try {
                Thread.sleep((long) (MIN_CALL_DURATION + new Random().nextInt(CALL_DURATION_BOUNDS_SPAN)));
            } catch (InterruptedException e) {
                // Do nothing.
            }
            synchronized (this) {
                state = CallState.FINISHED;
                callThread = null;
            }
        }
    }
}
