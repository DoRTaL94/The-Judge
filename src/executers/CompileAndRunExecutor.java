package executers;

import configs.langConfig;
import exceptions.ExecuterException;
import models.ICodeExecuter;
import models.ProgramOutput;
import services.LangConfigService;
import services.Language;
import utils.FileUtilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.concurrent.*;

public abstract class CompileAndRunExecutor implements ICodeExecuter {
    private static final Object RUN_LOCK = new Object();
    private static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();
    private final int TIMEOUT_SECONDS = 3;
    private boolean isInit = false;
    private boolean isCompiled = false;
    private String filesDest = null;
    private Language lang;
    LangConfigService langConfigService;

    @Override
    public void init(LangConfigService langConfigService, Language lang, String filesDest, String solCode, String tests) throws IOException, ExecuterException {
        FileUtilities.createFoldersInPath(filesDest);
        this.filesDest = filesDest;
        this.langConfigService = langConfigService;
        this.lang = lang;
        langConfig langConfig = langConfigService.getLangConfig(lang);
        String mainDest = Paths.get(filesDest, langConfig.getMainFileName()).toString();
        String solDest = Paths.get(filesDest, langConfig.getSolutionFileName()).toString();
        String testsDest = Paths.get(filesDest, langConfig.getTestsFileName()).toString();

        FileUtilities.WriteToFile(mainDest, langConfigService.getLangConfig(lang).getMainTemplate());
        FileUtilities.WriteToFile(solDest, langConfig.addCodeToSolution(solCode));
        FileUtilities.WriteToFile(testsDest, addSecurityManager(langConfig.addCodeToTests(tests)));
        isInit = true;
    }

    protected abstract String addSecurityManager(String tests) throws ExecuterException;

    @Override
    public ProgramOutput compile() throws Exception {
        ProgramOutput output = new ProgramOutput();

        if(isInit) {
            Process toClass = Runtime.getRuntime().exec(String.format(langConfigService.getLangConfig(lang).getCompilationCommand(), filesDest));
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
            String command = langConfigService.getLangConfig(lang).getExecuteCommand(Paths.get(filesDest));
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

    protected class StreamReader implements Callable<String> {
        InputStream is;

        StreamReader(InputStream is) {
            this.is = is;
        }

        @Override
        public String call() throws Exception {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            langConfig langConfig = langConfigService.getLangConfig(lang);

            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                int i = line.indexOf(langConfig.getSolutionFileName());

                if(i == -1) {
                    i = line.indexOf(langConfig.getMainFileName());

                    if(i == -1) {
                        i = line.indexOf(langConfig.getTestsFileName());

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
