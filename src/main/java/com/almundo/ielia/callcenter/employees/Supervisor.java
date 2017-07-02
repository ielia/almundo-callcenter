package com.almundo.ielia.callcenter.employees;

/**
 * A Supervisor.
 */
public class Supervisor extends Employee {
    @Override
    public int priority() {
        return 1;
    }
}
