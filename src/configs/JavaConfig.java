package configs;

import executers.CompileAndRunExecutor;
import executers.JavaExecutor;

import java.nio.file.Path;

public class JavaConfig implements langConfig {
    @Override
    public String getMainTemplate() {
        return  "import org.junit.runner.JUnitCore;\n" +
                "import org.junit.runner.Result;\n" +
                "import org.junit.runner.notification.Failure;\n" +
                "import java.util.List;\n" +
                "import java.lang.management.ManagementFactory;\n" +
                "public class Main {\n" +
                "    public static void main(String[] args) {\n" +
                "        Result result = JUnitCore.runClasses(Tests.class);\n" +
                "        final double nanosInSecond = 1000000000;\n" +
                "        final long cpuTimeNanos = ManagementFactory.getThreadMXBean().getThreadCpuTime(Thread.currentThread().getId());\n" +
                "        final double cpuTimeSec = cpuTimeNanos / nanosInSecond;" +
                "        System.out.print(\"--cut-here--\");\n" +
                "        System.out.print(String.format(\"%f\", cpuTimeSec));\n" +
                "        System.out.print(\"--cut-here--\");\n" +
                "        List<Failure> failures = result.getFailures();\n" +
                "        for (Failure failure: failures) {\n" +
                "            String testHeader = failure.getTestHeader();\n" +
                "            System.out.print(testHeader.substring(0, testHeader.indexOf('(')) + \" \");\n" +
                "        }\n" +
                "    }\n" +
                "}";
    }

    @Override
    public String getFilesSuffix() {
        return "java";
    }

    @Override
    public String getRootFolderName() {
        return "java_executions";
    }

    @Override
    public String getExecuteCommand(Path filesDestPath) {
        String codeFolder = filesDestPath.getFileName().toString();
        return String.format("java -Dfile.encoding=UTF-8 -cp ./jars/*;./%s/tmp/%s;. Main", getRootFolderName(), codeFolder);
    }

    @Override
    public String getMainFileName() {
        return "Main.java";
    }

    @Override
    public String getSolutionFileName() {
        return "Solution.java";
    }

    @Override
    public String getTestsFileName() {
        return "Tests.java";
    }

    @Override
    public String getCompilationCommand() {
        return "javac -encoding utf8 -cp ./jars/*; %s/*.java";
    }

    @Override
    public CompileAndRunExecutor getExecutor() {
        return new JavaExecutor();
    }

    @Override
    public String addCodeToSolution(String solCode) {
        return solCode;
    }

    @Override
    public String addCodeToTests(String tests) {
        return String.format("import org.junit.Test;import static org.junit.Assert.*; %s", tests);
    }
}
