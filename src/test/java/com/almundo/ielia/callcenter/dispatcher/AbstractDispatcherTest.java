package com.almundo.ielia.callcenter.dispatcher;

import com.almundo.ielia.callcenter.employees.Director;
import com.almundo.ielia.callcenter.employees.Employee;
import com.almundo.ielia.callcenter.employees.Operator;
import com.almundo.ielia.callcenter.employees.Supervisor;
import com.almundo.ielia.callcenter.phonecalls.PhoneCall;
import org.junit.After;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

/**
 * Abstract Dispatcher data-driven test.
 */
@RunWith(Parameterized.class)
public abstract class AbstractDispatcherTest {
    protected List<PhoneCall> calls;
    // If Java 8 had Scala's type conformance, we would have lists of different element classes.
    protected List<Employee> directors;
    protected List<Employee> operators;
    protected List<Employee> supervisors;
    protected Dispatcher dispatcher;

    protected AbstractDispatcherTest(int nOps, int nSups, int nDirs) {
        operators = createEmployees(Operator::new, nOps);
        supervisors = createEmployees(Supervisor::new, nSups);
        directors = createEmployees(Director::new, nDirs);
        dispatcher = createDispatcher(operators, supervisors, directors);
    }

    @After
    public void after() {
        calls.forEach(PhoneCall::hangUp);
        dispatcher.shutdownNow();
    }

    protected <C, O> List<O> create(Supplier<C> constructor, Class<O> outputClass, int n) {
        return IntStream.range(0, n).mapToObj(i -> outputClass.cast(constructor.get())).collect(toList());
    }

    protected List<PhoneCall> createCalls(Supplier<PhoneCall> constructor, int n) {
        return create(constructor, PhoneCall.class, n);
    }

    protected Dispatcher createDispatcher(Employee...employees) {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.addAll(Arrays.asList(employees));
        return dispatcher;
    }

    protected Dispatcher createDispatcher(List<Employee> operators, List<Employee> supervisors, List<Employee> directors) {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.addAll(directors);
        dispatcher.addAll(operators);
        dispatcher.addAll(supervisors);
        return dispatcher;
    }

    protected <C> List<Employee> createEmployees(Supplier<C> constructor, int n) {
        return create(constructor, Employee.class, n);
    }
}
