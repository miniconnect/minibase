package hu.webarticum.minibase.test.launch;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import hu.webarticum.minibase.test.matcher.TableMatcher;
import hu.webarticum.minibase.test.runner.QueryTestController;
import hu.webarticum.miniconnect.lang.ImmutableList;
import hu.webarticum.miniconnect.record.ResultTable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "QueryTestMain", mixinStandardHelpOptions = true)
public class QueryTestMain implements Callable<Integer> {

    @Parameters(
            paramLabel = "<test-suite-list-resource>",
            description = "Resource path to a file containing the list of test suites.",
            arity = "1")
    private String testSuiteListResourcePath;

    @Option(
            names = "--quiet",
            paramLabel = "<quiet>",
            description = "Enables quiet mode that prints no output.",
            arity = "0..1",
            defaultValue = "false")
    private boolean isQuiet;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new QueryTestMain()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        AtomicInteger totalCounter = new AtomicInteger(0);
        AtomicInteger successCounter = new AtomicInteger(0);
        QueryTestController
                .ofResource(testSuiteListResourcePath)
                .runSuites((path, name, matcher, table, result) -> {
                    boolean success = acceptCase(path, name, matcher, table, result);
                    totalCounter.incrementAndGet();
                    if (success) {
                        successCounter.incrementAndGet();
                    }
                });
        int totalCount = totalCounter.get();
        int successCount = successCounter.get();
        if (!isQuiet) {
            System.out.println();
            System.out.println(String.format(
                    "%2$s/%1$s query test cases passed",
                    totalCount, successCount));
        }
        return totalCount == successCount ? 0 : 1;
    }

    private boolean acceptCase(
            String resourcePath,
            String caseName,
            TableMatcher tableMatcher,
            ResultTable givenTable,
            Iterable<ImmutableList<Object>> expectedResult) {
        try {
            tableMatcher.match(givenTable, expectedResult);
        } catch (Exception e) {
            if (!isQuiet) {
                System.err.println();
                System.err.println("FAILED query test");
                System.err.println("    resource:  " + resourcePath);
                System.err.println("    case:      " + caseName);
                System.err.println("    message:   " + e.getMessage());
            }
            return false;
        }
        return true;
    }

}
