package hu.webarticum.minibase.test.runner;

import hu.webarticum.minibase.test.model.fixture.QueryTestFixture;

public class QueryTestCaseRunner implements Runnable {

    private final QueryTestFixture caseDescription;

    public QueryTestCaseRunner(QueryTestFixture caseDescription) {
        this.caseDescription = caseDescription;
    }

    public QueryTestFixture caseDescription() {
        return caseDescription;
    }

    @Override
    public void run() {
        // TODO
    }

}