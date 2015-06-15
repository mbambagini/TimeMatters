package org.timematters.exceptions;

import org.timematters.misc.SavingProblems;

public class JobsNotSaved extends Exception {

    private SavingProblems error;

    public JobsNotSaved() {
        error = SavingProblems.GenericError;
    }

    public JobsNotSaved(SavingProblems arg) {
        error = arg;
    }

    public SavingProblems getError () {
        return error;
    }
}
