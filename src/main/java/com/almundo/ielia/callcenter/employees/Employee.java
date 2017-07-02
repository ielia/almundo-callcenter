package com.almundo.ielia.callcenter.employees;

import com.almundo.ielia.callcenter.phonecalls.PhoneCall;

/**
 * <p>An Employee. Each employee will have a pick-up priority when calls are dispatched. No employees will pick-up calls
 * if there are any other employees with lower-value priority still available.</p>
 * <p>Note: The instances of this class are going to be used in a HashSet. For the purposes of this demo, we don't need
 * to override {@code hashCode} nor {@code equals} methods.</p>
 */
public abstract class Employee implements Comparable<Employee> {
    /**
     * @return A priority value that will indicate what batch of employees it belongs to.
     */
    public abstract int priority();

    /**
     * Compares employees based on pick-up priority.
     *
     * @param other The employee instance we will compare to.
     * @return -1 if current instance comes before in the line of picking calls up, 0 if same, 1 otherwise.
     */
    public int compareTo(Employee other) {
        return Integer.compare(this.priority(), other.priority());
    }

    /**
     * Receives a call being dispatched to this instance.
     *
     * @param phoneCall The phone call being received.
     */
    public void receive(PhoneCall phoneCall) {
        phoneCall.pickUp();
    }
}
