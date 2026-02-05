package hu.webarticum.minibase.test.launch;

import java.util.List;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "MinibaseTests", mixinStandardHelpOptions = true)
public class QueryTestMain implements Runnable {

    @Parameters(
            paramLabel = "<test-case-paths>",
            description = "Test case description file paths.",
            arity = "1..")
    private List<String> testCasePaths;

    @Override
    public void run() {
        System.out.println(testCasePaths);
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new QueryTestMain()).execute(args);
        System.exit(exitCode);
    }

}