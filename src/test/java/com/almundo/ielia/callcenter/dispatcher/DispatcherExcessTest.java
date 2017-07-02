package com.almundo.ielia.callcenter.dispatcher;

import com.almundo.ielia.callcenter.phonecalls.CallState;
import com.almundo.ielia.callcenter.phonecalls.PhoneCall;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

/**
 * Tests for boundaries.
 */
public class DispatcherExcessTest extends AbstractDispatcherTest {
    private int nOnHoldCalls;
    private CountDownLatch startedCalls;

    @Parameterized.Parameters(name = "{index}: #calls={1}, #ops={2}, #sups={3}, #dirs={4} ({5})")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {3, 4, 1, 1, 1, "less employees than calls"},
                {10, 15, 20, 20, 20, "more calls than allowed concurrently"}
        });
    }

    public DispatcherExcessTest(int concurrentCalls, int nCalls, int nOps, int nSups, int nDirs, String suffix) {
        super(nOps, nSups, nDirs);
        nOnHoldCalls = nCalls - concurrentCalls;
        startedCalls = new CountDownLatch(concurrentCalls);
        calls = createCalls(() -> spy(new PhoneCall() {
            @Override
            public void pickUp() {
                super.pickUp();
                startedCalls.countDown();
            }
        }), nCalls);
    }

    @Test(timeout = 10000)
    public void test_dispatcher_holds_excess_calls() {
        // When:
        calls.forEach(dispatcher::dispatchCall);

        // Then:
        try {
            startedCalls.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assertEquals(nOnHoldCalls, calls.stream().filter(call -> call.getState() == CallState.ON_HOLD).count());
        calls.stream().filter(call -> call.getState() == CallState.ACTIVE).forEach(PhoneCall::hangUp);
        calls.forEach(call -> {
            verify(call, timeout(1000)).pickUp();
        });

        // Cleanup:
        calls.forEach(PhoneCall::hangUp);
        dispatcher.shutdownNow();
    }
}
