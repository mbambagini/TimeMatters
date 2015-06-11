package org.timematters.exceptions;

import org.timematters.misc.SavingProblems;

/**
 * Created by mario on 21/05/15.
 */
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
