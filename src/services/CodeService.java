package services;

import com.google.gson.Gson;
import configs.langConfig;
import exceptions.RequestBodyFieldsException;
import executers.JavaExecutor;
import models.*;
import org.apache.commons.io.FileUtils;
import utils.FileUtilities;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class CodeService {
    private final long MAX_BYTES_IN_TMP = 1000000000;
    private final LangConfigService langConfigService;
    private final String programExecutedSuccessfully = "Program executed successfully.%sDuration: %.2f s.";
    private final String runtimeError = "Oops... Runtime error.%sPlease check the raw output.";
    private final String compileError = "There are compilation errors.%sPlease check if you changed our solution template structure.%sIf not check the raw output.";
    private final String timeoutError = "Your solution takes a long time to execute.%sMaybe there is an infinite loop in your code.";
    private final String codeFolderName = "code%d";
    private final String rootDir;
    private String tmpFolderPath;
    private final float nanosInOneSec = 1000000000;
    private final Object fileCounterLock = new Object();
    private int fileCounter = 0;
    private String solutionPath;

    public CodeService(LangConfigService langConfigService) {
        this.langConfigService = langConfigService;
        rootDir = System.getProperty("user.dir");
        cleanTmp();
    }

    private void cleanTmp() {
        if(tmpFolderPath != null) {
            File tmpFolder = new File(tmpFolderPath);
            boolean isTmpExists = tmpFolder.exists();

            if (isTmpExists) {
                try {
                    FileUtils.cleanDirectory(tmpFolder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                tmpFolder.mkdir();
            }
        }
    }

    public ExecuteRequestModel getJSONObject(HttpServletRequest request) throws IOException, RequestBodyFieldsException {
        String jsonData =
                new BufferedReader(new InputStreamReader(request.getInputStream()))
                        .lines()
                        .collect(Collectors.joining("\n"));
        Gson gson = new Gson();
        ExecuteRequestModel requestData = gson.fromJson(jsonData, ExecuteRequestModel.class);

        if(requestData.getCode() == null || requestData.getLang() == null || requestData.getTests() == null || requestData.getTests().equals("")) {
            throw new RequestBodyFieldsException(requestData);
        }

        return requestData;
    }

    private int getFileCount() {
        int fileCount;

        synchronized (fileCounterLock) {
            fileCount = fileCounter;
            fileCounter++;
        }

        return fileCount;
    }

    // Update solution path so each run code request will execute on a different path.
    // Clean tmp dir if the folder size is over 1GB and resets files counter.
    private void updateSolutionPath() throws IOException {
        File tmpFolder = new File(tmpFolderPath);

        if(FileUtilities.getFolderSize(tmpFolder) > MAX_BYTES_IN_TMP) {
            synchronized (fileCounterLock) {
                if(FileUtilities.getFolderSize(tmpFolder) > MAX_BYTES_IN_TMP) {
                    fileCounter = 0;
                    FileUtils.cleanDirectory(tmpFolder);
                }
            }
        }

        solutionPath = Paths.get(tmpFolderPath, String.format(codeFolderName, getFileCount())).toString();
    }

    public ExecutionCallable compileAndRunCode(HttpServletRequest request) throws Exception {
        ExecuteRequestModel requestData = getJSONObject(request);
        String lang = requestData.getLang().toLowerCase().replace(" ", "");
        String code = requestData.getCode();
        String tests = requestData.getTests();
        langConfig langConfig = langConfigService.getLangConfig(Language.valueOf(lang.toUpperCase()));
        tmpFolderPath = Paths.get(rootDir, langConfig.getRootFolderName() + "/tmp").toString();
        updateSolutionPath();

        return new ExecutionCallable(langConfig.getExecutor(), Language.valueOf(lang.toUpperCase()), code, tests);
    }

    private void setProperExecuteMessagesForResponse(Response response, ProgramOutput executedProgOutput) {
        if(executedProgOutput.isTerminatedBeforeTime()) {
            String runErrors = executedProgOutput.getErrors();
            response.setErrors(runErrors);
            response.setOutput(executedProgOutput.getOutput());
            response.setTestsFailed(executedProgOutput.getTestsFailed());

            if (runErrors.equals("")) {
                response.setDurationInSeconds(executedProgOutput.getDurationInSeconds());
                String message = String.format(programExecutedSuccessfully, System.lineSeparator(), response.getDurationInSeconds());

                if(!response.getTestsFailed().equals("")) {
                    message += String.format("%sBut the following test cases failed:%s%s", System.lineSeparator(), System.lineSeparator(), executedProgOutput.getTestsFailed());
                }

                response.setMessage(message);
            } else {
                response.setMessage(String.format(runtimeError, System.lineSeparator()));
            }
        } else {
            response.setMessage(String.format(timeoutError, System.lineSeparator()));
        }
    }

    public class ExecutionCallable implements Callable<Response> {

        private ICodeExecuter executor;
        private String code;
        private String tests;
        private Language lang;

        public ExecutionCallable(ICodeExecuter executor, Language lang, String code, String tests) {
            this.executor = executor;
            this.code = code;
            this.tests = tests;
            this.lang = lang;
        }

        @Override
        public Response call() throws Exception {
            Response response = new Response();
            executor.init(langConfigService, lang, solutionPath, code, tests);
            ProgramOutput compileOutput = executor.compile();
            String compileErrors = compileOutput.getErrors();
            response.setErrors(compileErrors);

            if(compileErrors.equals("")) {
                ProgramOutput runOutput = executor.run();
                setProperExecuteMessagesForResponse(response, runOutput);
            } else {
                response.setMessage(String.format(compileError, System.lineSeparator(), System.lineSeparator()));
            }

            return response;
        }
    }
}
