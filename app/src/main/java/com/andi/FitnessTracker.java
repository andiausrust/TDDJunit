package com.andi;

/**
 * Created by AndreasMayer on Dec, 2018
 */
public class FitnessTracker {

    public static final int RUN_STEPS_FACTOR = 2;

    public void step() {
        Counter.getInstance().add();
    }

    public void runStep() {
        Counter.getInstance().add(RUN_STEPS_FACTOR);
    }

    public int getTotalSteps() {
        return Counter.getInstance().getTotal();
    }
}
