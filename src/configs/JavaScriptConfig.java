package configs;

import executers.CompileAndRunExecutor;
import executers.JavaScriptExecutor;
import java.nio.file.Path;
import java.util.regex.Pattern;

public class JavaScriptConfig implements langConfig {
    @Override
    public String getMainTemplate() {
        return  "import java.io.*;\n" +
                "import java.nio.charset.StandardCharsets;\n" +
                "import java.util.Arrays;\n" +
                "import java.util.LinkedList;\n" +
                "import java.util.concurrent.*;\n" +
                "import java.util.regex.Pattern;\n" +
                "import java.util.stream.Collectors;\n" +
                "public class Main {\n" +
                "    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {\n" +
                "        String command = String.format(\"node C:/apache-tomcat-8.5.54/bin/javascript_executions/node_modules/jest/bin/jest.js %s\", args[0]);\n" +
                "        Process p = Runtime.getRuntime().exec(command, null, new File(\"C:/apache-tomcat-8.5.54/bin/javascript_executions/tmp\"));\n" +
                "        FutureTask<String> readIsTask = new FutureTask<>(new StreamReader(p.getInputStream()));\n" +
                "        FutureTask<String> readEsTask = new FutureTask<>(new StreamReader(p.getErrorStream()));\n" +
                "        new Thread(readIsTask).start();\n" +
                "        new Thread(readEsTask).start();\n" +
                "        p.waitFor(15, TimeUnit.SECONDS);\n" +
                "        String passed = readIsTask.get();\n" +
                "        String failed = readEsTask.get();\n" +
                "        String output = passed.equals(\"\") ? failed : passed;\n" +
                "        LinkedList<String> lines = new LinkedList<String>(Arrays.asList(output.split(System.lineSeparator())));\n" +
                "        String firstLine = lines.removeFirst();\n" +
                "        lines.removeLast();\n" +
                "        lines.removeLast();\n" +
                "        lines.removeLast();\n" +
                "        double totalTime = 0;\n" +
                "        try {\n" +
                "            for (String line : lines) {\n" +
                "                if (line.equals(\"\")) {\n" +
                "                    break;\n" +
                "                }\n" +
                "                String parts[] = line.split(Pattern.quote(\"(\"));\n" +
                "                String timeParts[] = parts[1].split(\" \");\n" +
                "                totalTime += Double.parseDouble(timeParts[0]);\n" +
                "            }\n" +
                "        } catch (Exception e) {}\n" +
                "        lines.addFirst(firstLine);\n" +
                "        output = lines.stream().collect(Collectors.joining(System.lineSeparator()));\n" +
                "        output = output.replaceAll(\"tmp/code\\\\d+/\", \"\")\n" +
                "                .replaceAll(\"code\\\\d+/\", \"\");\n" +
                "        System.out.print(output + System.lineSeparator());\n" +
                "        p.destroy();\n" +
                "        p.waitFor();\n" +
                "        final double cpuTimeSec = totalTime / 1000;\n" +
                "        System.out.print(\"--cut-here--\");\n" +
                "        System.out.print(String.format(\"%f\", cpuTimeSec));\n" +
                "        System.out.print(\"--cut-here--\");" +
                "}\n" +
                "\n" +
                "    protected static class StreamReader implements Callable<String> {\n" +
                "        InputStream is;\n" +
                "        StreamReader(InputStream is) {\n" +
                "            this.is = is;\n" +
                "        }\n" +
                "        @Override\n" +
                "        public String call() throws Exception {\n" +
                "            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));\n" +
                "            StringBuilder sb = new StringBuilder();\n" +
                "            for (String line = reader.readLine(); line != null; line = reader.readLine()) {\n" +
                "                sb.append(line);\n" +
                "                sb.append(System.lineSeparator());\n" +
                "            }\n" +
                "            return sb.toString();\n" +
                "        }\n" +
                "    }\n" +
                "}";
    }

    @Override
    public String getFilesSuffix() {
        return "js";
    }

    @Override
    public String getRootFolderName() {
        return "javascript_executions";
    }

    @Override
    public String getExecuteCommand(Path filesDestPath) {
        String codeFolder = filesDestPath.getFileName().toString();
        String[] parts = filesDestPath.toString().split(Pattern.quote("\\"));
        int length = parts.length;
        String path = String.format("./%s/%s/%s", parts[length - 3], parts[length - 2], parts[length - 1]);
        return String.format("java -Dfile.encoding=UTF-8 -cp ./%s/tmp/%s;. Main %s", getRootFolderName(), codeFolder, path);
    }

    @Override
    public String getMainFileName() {
        return "Main.java";
    }

    @Override
    public String getSolutionFileName() {
        return "solution.js";
    }

    @Override
    public String getTestsFileName() {
        return "test.js";
    }

    @Override
    public String getCompilationCommand() {
        return "javac -encoding utf8 %s/Main.java";
    }

    @Override
    public CompileAndRunExecutor getExecutor() {
        return new JavaScriptExecutor();
    }

    @Override
    public String addCodeToSolution(String solCode) {
        solCode = solCode.replaceAll("[\\s]*require[\\s]*\\([\\s\\S]+?\\)", " 'require is not allowed';");
        return String.format("%s module.exports = solution", solCode);
    }

    @Override
    public String addCodeToTests(String tests) {
        return String.format("const solution = require('./solution'); %s", tests);
    }
}
