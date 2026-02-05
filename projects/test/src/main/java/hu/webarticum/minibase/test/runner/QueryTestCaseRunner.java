package hu.webarticum.minibase.test.runner;

import hu.webarticum.minibase.test.model.QueryTestCase;

public class QueryTestCaseRunner implements Runnable {

    private final QueryTestCase caseDescription;

    public QueryTestCaseRunner(QueryTestCase caseDescription) {
        this.caseDescription = caseDescription;
    }

    public QueryTestCase caseDescription() {
        return caseDescription;
    }

    @Override
    public void run() {
        // TODO
    }

}