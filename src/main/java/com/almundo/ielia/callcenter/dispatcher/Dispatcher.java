package com.almundo.ielia.callcenter.dispatcher;

import com.almundo.ielia.callcenter.employees.Employee;
import com.almundo.ielia.callcenter.phonecalls.CallState;
import com.almundo.ielia.callcenter.phonecalls.PhoneCall;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * A Call Dispatcher.
 */
public class Dispatcher {
    private static final int MAX_CONCURRENT_CALLS = 10;

    private final ExecutorService linePool = Executors.newFixedThreadPool(MAX_CONCURRENT_CALLS);

    /**
     * This set may be changed to a Map from employeeId to Employee instance in the future, if Employee gets to be
     * mutable at some point.
     */
    private final Set<Employee> allEmployees = new HashSet<>();

    private final BlockingQueue<Employee> availableEmployees = new PriorityBlockingQueue<>();

    /**
     * Registers employees.
     *
     * @param employees Employees being registered.
     */
    public void addAll(List<Employee> employees) {
        this.allEmployees.addAll(employees);
        this.availableEmployees.addAll(employees);
    }

    /**
     * Dispatches a phone call--i.e., it takes the call 'on-hold' and tries to assign it to the first employee, in
     * priority order.
     *
     * @param phoneCall The phone call being dispatched.
     */
    public void dispatchCall(PhoneCall phoneCall) {
        linePool.submit(new CallConsumer(this, phoneCall));
    }

    /**
     * @return A list of all employees registered in this dispatcher.
     */
    public List<Employee> getAllEmployees() {
        return new ArrayList<>(allEmployees);
    }

    /**
     * @return A list of available employees in this dispatcher.
     */
    public List<Employee> getAvailableEmployees() {
        return new ArrayList<>(availableEmployees);
    }

    /**
     * Shuts down nicely.
     */
    public void shutdown() {
        linePool.shutdown();
    }

    /**
     * Shuts down immediately.
     */
    public void shutdownNow() {
        linePool.shutdownNow();
    }

    private static class CallConsumer implements Runnable {
        private Dispatcher dispatcher;
        private PhoneCall phoneCall;

        CallConsumer(Dispatcher dispatcher, PhoneCall phoneCall) {
            this.dispatcher = dispatcher;
            this.phoneCall = phoneCall;
        }

        @Override
        public void run() {
            Employee employee = null;
            try {
                employee = dispatcher.availableEmployees.take();
                employee.receive(phoneCall);
                phoneCall.waitFinish();
            } catch (InterruptedException exception) {
                if (phoneCall.getState() != CallState.FINISHED) {
                    phoneCall.hangUp();
                }
            } finally {
                if (employee != null) {
                    dispatcher.availableEmployees.offer(employee);
                }
            }
        }
    }
}
