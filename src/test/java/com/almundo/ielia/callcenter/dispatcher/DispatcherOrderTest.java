package com.almundo.ielia.callcenter.dispatcher;

import com.almundo.ielia.callcenter.employees.Employee;
import com.almundo.ielia.callcenter.phonecalls.CallState;
import com.almundo.ielia.callcenter.phonecalls.PhoneCall;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

/**
 * Tests dispatcher functionality, in terms of ordering of pick-ups, works as expected.
 */
public class DispatcherOrderTest extends AbstractDispatcherTest {
    @Parameterized.Parameters(name = "{index}: #calls={0}, #ops={1}, #sups={2}, #dirs={3}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {1, 1, 0, 0},
                {1, 0, 1, 0},
                {1, 0, 0, 1},
                {2, 1, 1, 0},
                {2, 1, 0, 1},
                {2, 2, 1, 1},
                {2, 0, 2, 1},
                {3, 1, 1, 1},
                {5, 2, 2, 2},
                {10, 10, 10, 10}
        });
    }

    public DispatcherOrderTest(int nCalls, int nOps, int nSups, int nDirs) {
        super(nOps, nSups, nDirs);
        calls = createCalls(() -> spy(PhoneCall.class), nCalls);
    }

    private void assertOrder() {
        Map<Integer, Integer> available = getAddedCounters(dispatcher.getAvailableEmployees());
        Map<Integer, Integer> all = getAddedCounters(dispatcher.getAllEmployees());
        int numberOfCalls = calls.size();
        all.forEach((priority, accumulator) -> {
            String errorPrefix = "error on priority " + priority + ":";
            if (numberOfCalls >= accumulator) {
                assertNull(errorPrefix, available.get(priority));
            } else {
                assertTrue(errorPrefix, available.get(priority) == accumulator - numberOfCalls);
            }
        });
    }

    /**
     * @param employees List of employees.
     * @return Map of priority to accumulated count.
     */
    private Map<Integer, Integer> getAddedCounters(List<Employee> employees) {
        Collections.sort(employees);
        Map<Integer, Integer> counters = new TreeMap<>();
        int accumulator = 0;
        for (Employee employee : employees) {
            counters.put(employee.priority(), ++accumulator);
        }
        return counters;
    }

    @Test(timeout = 10000)
    public void test_dispatcher_obeys_employee_order() {
        // When: "calls are dispatched"
        calls.forEach(dispatcher::dispatchCall);

        // Then:
        calls.forEach(call -> {
            verify(call, timeout(1000)).pickUp();
            verify(call, timeout(1000)).waitFinish();
            assertTrue(call.getState() == CallState.ACTIVE);
        });
        assertOrder();
    }
}