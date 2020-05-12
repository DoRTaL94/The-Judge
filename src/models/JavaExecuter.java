package models;

import exceptions.ExecuterException;
import services.Language;
import services.TemplateService;
import utils.FileUtilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.*;

public class JavaExecuter implements ICodeExecuter {
    private static final Object RUN_LOCK = new Object();
    private static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();
    private final int TIMEOUT_SECONDS = 3;
    private boolean isInit = false;
    private boolean isCompiled = false;
    private String filesDest = null;
    TemplateService templateService;

    @Override
    public void init(TemplateService templateService, String filesDest, String solCode, String tests) throws IOException, ExecuterException {
        FileUtilities.createFoldersInPath(filesDest);
        this.filesDest = filesDest;
        this.templateService = templateService;
        String mainDest = Paths.get(filesDest, "Main.java").toString();
        String solDest = Paths.get(filesDest, "Solution.java").toString();
        String testsDest = Paths.get(filesDest, "Tests.java").toString();

        FileUtilities.WriteToFile(mainDest, templateService.getMainTemplate(Language.JAVA));
        FileUtilities.WriteToFile(solDest, solCode);
        FileUtilities.WriteToFile(testsDest, addSecurityManager(addImports(tests)));
        isInit = true;
    }

    private String addImports(String tests) {
        return "import org.junit.Test;import static org.junit.Assert.*;" + tests;
    }

    private String addSecurityManager(String tests) throws ExecuterException {
        String classDec = "public class Tests {";
        int lastAppearance = tests.lastIndexOf(classDec) + classDec.length();
        int firstAppearance = tests.indexOf(classDec) + classDec.length();

        if(lastAppearance != firstAppearance) {
            throw new ExecuterException(
                    "There are two appearances of the following string in your code 'public class Tests'. " +
                    "Make sure to have only one.");
        }

        String before = tests.substring(0, firstAppearance);
        String toEnter = "public Tests() {System.setSecurityManager(new SecurityManager());}";
        String after = tests.substring(firstAppearance);
        return String.format("%s%s%s", before, toEnter, after);
    }

    @Override
    public ProgramOutput compile() throws Exception {
        ProgramOutput output = new ProgramOutput();

        if(isInit) {
            Process toClass = Runtime.getRuntime().exec(String.format("javac -cp ./jars/*; %s/*", filesDest));
            FutureTask<String> readIsTask = new FutureTask<>(new StreamReader(toClass.getInputStream()));
            FutureTask<String> readEsTask = new FutureTask<>(new StreamReader(toClass.getErrorStream()));
            THREAD_POOL.execute(readIsTask);
            THREAD_POOL.execute(readEsTask);
            output.setOutput(readIsTask.get());
            output.setErrors(readEsTask.get());
            toClass.waitFor();
            isCompiled = true;
        } else {
            throw new Exception("Init should execute before calling the compile method.");
        }

        return output;
    }

    @Override
    public ProgramOutput run() throws Exception {
        ProgramOutput output = new ProgramOutput();

        if(isInit && isCompiled) {
            Path filesDestPath =  Paths.get(filesDest);
            String codeFolder = filesDestPath.getFileName().toString();
            String command = String.format("java -cp ./jars/*;./tmp/%s;. Main", codeFolder);
            Process p;
            boolean isTerminatedBeforeTime;
            FutureTask<String> readIsTask;
            FutureTask<String> readEsTask;

            synchronized (RUN_LOCK) {
                p = Runtime.getRuntime().exec(command);
                readIsTask = new FutureTask<>(new StreamReader(p.getInputStream()));
                readEsTask = new FutureTask<>(new StreamReader(p.getErrorStream()));
                THREAD_POOL.execute(readIsTask);
                THREAD_POOL.execute(readEsTask);
                isTerminatedBeforeTime = p.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);

                if(isTerminatedBeforeTime) {
                    output.setTerminatedBeforeTime(true);
                    output.setOutput(readIsTask.get());
                    output.setErrors(readEsTask.get());
                }

                p.destroy();
                p.waitFor();
            }

        } else {
            throw new Exception("Init and compiled should execute before calling the run method.");
        }

        return output;
    }

    class StreamReader implements Callable<String> {
        InputStream is;

        StreamReader(InputStream is) {
            this.is = is;
        }

        @Override
        public String call() throws Exception {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();

            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                int i = line.indexOf("Solution.java");

                if(i == -1) {
                    i = line.indexOf("Main.java");

                    if(i == -1) {
                        i = line.indexOf("Tests.java");

                        if(i == -1) {
                            i = 0;
                        }
                    }
                }

                sb.append(line.substring(i));
                sb.append(System.lineSeparator());
            }

            return sb.toString();
        }
    }
}
