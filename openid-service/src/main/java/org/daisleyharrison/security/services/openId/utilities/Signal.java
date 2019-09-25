package org.daisleyharrison.security.services.openId.utilities;

/**
 * 
 * Represents a flag which can be set or waited for - once signalled, things
 * waiting for it don't block
 * 
 */

public class Signal {

    private boolean signalled = false;
    private boolean aborted = false;

    /**
     * 
     * Set the done flag
     * 
     */

    public synchronized void setSignal() {

        signalled = true;

        notifyAll();

    }

    public synchronized void abortSignal() {

        aborted = true;

        notifyAll();
    }

    /**
     * 
     * Wait up to timeout for the signal
     * 
     * @param timeout timeout in milliseconds - this will be honoured unless wait
     *                wakes up spuriously
     * 
     * @throws InterruptedException
     * 
     */

    public synchronized void waitForSignal(long timeout) throws InterruptedException {

        if (!signalled && !aborted) {
            wait(timeout);
        }
        if (aborted) {
            throw new InterruptedException("signal aborted");
        }

    }

    /**
     * 
     * Wait indefinitely for the done signal
     * 
     * @throws InterruptedException on thread error
     * 
     */

    public synchronized void waitForSignal() throws InterruptedException {

        // as wait can wake up spuriously, put this in a loop

        while (!signalled && !aborted) {
            wait();
        }
        if (aborted) {
            throw new InterruptedException("signal aborted");
        }

    }

    /**
     * 
     * Peek at the signal
     * 
     * @return the state of the signal
     * 
     */

    public synchronized boolean isSignalled() {

        return signalled;

    }

    /**
     * see if a signal was aborted
     * 
     * @return boolean true if aborted
     */
    public synchronized boolean isAborted() {

        return aborted;

    }

    /**
     * reset a signal
     */
    public synchronized void reset() {

        this.aborted = false;

        clearSignal();
    }

    /**
     * clear a signal
     */
    public synchronized void clearSignal() {
        this.signalled = false;
    }

}