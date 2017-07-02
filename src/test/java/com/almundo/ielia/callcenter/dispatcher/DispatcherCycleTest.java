package com.almundo.ielia.callcenter.dispatcher;

import com.almundo.ielia.callcenter.phonecalls.CallState;
import com.almundo.ielia.callcenter.phonecalls.PhoneCall;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

/**
 * Test for dispatcher cycle.
 */
public class DispatcherCycleTest extends AbstractDispatcherTest {
    private int concurrentCalls;
    private CountDownLatch startedCalls;

    @Parameterized.Parameters(name = "{index}: #concurrency={0}, #calls={1}, #ops={2}, #sups={3}, #dirs={4}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {10, 30, 15, 15, 15},
                {5, 20, 2, 1, 2},
                {10, 25, 3, 3, 4}
        });
    }

    public DispatcherCycleTest(int concurrentCalls, int nCalls, int nOps, int nSups, int nDirs) {
        super(nOps, nSups, nDirs);
        this.concurrentCalls = concurrentCalls;
        calls = createCalls(() -> spy(new PhoneCall() {
            @Override
            public void pickUp() {
                super.pickUp();
                countDown();
            }
        }), nCalls);
    }

    private void countDown() {
        startedCalls.countDown();
    }

    @Test(timeout = 60000)
    public void test_dispatcher_dispatches_calls_as_new_employees_become_available() throws InterruptedException {
        // Given:
        int cycles = (int) Math.ceil((double) calls.size() / (double) concurrentCalls);
        int currentlyOnHold = calls.size() - concurrentCalls;
        startedCalls = new CountDownLatch(concurrentCalls);

        // When:
        calls.forEach(dispatcher::dispatchCall);

        for (int cycle = 0; cycle < cycles; ++cycle) {
            // Then:
            try {
                startedCalls.await();
                assertEquals(currentlyOnHold, calls.stream().filter(call -> call.getState() == CallState.ON_HOLD).count());
            } catch (Throwable error) {
                System.err.println("Error on cycle " + (cycle + 1) + " of " + cycles);
                throw error;
            }

            // When:
            int nextBatch = Math.min(concurrentCalls, currentlyOnHold);
            currentlyOnHold -= nextBatch;
            startedCalls = new CountDownLatch(nextBatch);
            calls.stream().filter(call -> call.getState() == CallState.ACTIVE).collect(toList()).forEach(PhoneCall::hangUp);
        }

        // Then:
        calls.forEach(call -> verify(call, timeout(1000)).pickUp());
    }
}
