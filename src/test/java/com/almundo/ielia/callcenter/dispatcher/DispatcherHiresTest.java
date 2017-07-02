package com.almundo.ielia.callcenter.dispatcher;

import com.almundo.ielia.callcenter.employees.Director;
import com.almundo.ielia.callcenter.employees.Employee;
import com.almundo.ielia.callcenter.employees.Operator;
import com.almundo.ielia.callcenter.phonecalls.CallState;
import com.almundo.ielia.callcenter.phonecalls.PhoneCall;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

/**
 * Test for dispatcher cycle.
 */
public class DispatcherHiresTest extends AbstractDispatcherTest {
    private List<Employee> extraEmployees;
    private int nOnHoldCalls;
    private CountDownLatch startedCalls;

    @Parameterized.Parameters(name = "{index}: #calls={0}, #ops={1}, #sups={2}, #dirs={3}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {1, 0, 0, 0, Collections.singletonList((Employee) new Operator())},
                {10, 1, 1, 1, IntStream.range(0, 7).mapToObj(n -> (Employee) new Director()).collect(toList())}
        });
    }

    public DispatcherHiresTest(int nCalls, int nOps, int nSups, int nDirs, List<Employee> extraEmployees) {
        super(nOps, nSups, nDirs);
        this.extraEmployees = extraEmployees;
        nOnHoldCalls = nCalls - nOps - nSups - nDirs;
        startedCalls = new CountDownLatch(nCalls - nOnHoldCalls);
        calls = createCalls(() -> spy(new PhoneCall() {
            @Override
            public void pickUp() {
                super.pickUp();
                startedCalls.countDown();
            }
        }), nCalls);
    }

    @Test(timeout = 10000)
    public void test_dispatcher_dispatches_calls_as_new_employees_become_available() throws InterruptedException {
        // When:
        calls.forEach(dispatcher::dispatchCall);
        startedCalls.await();

        // Then:
        assertEquals(nOnHoldCalls, calls.stream().filter(call -> call.getState() == CallState.ON_HOLD).count());

        // When:
        dispatcher.addAll(extraEmployees);

        // Then:
        calls.forEach(call -> verify(call, timeout(1000)).pickUp());
    }
}
