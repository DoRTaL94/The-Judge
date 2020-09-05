package configs;

import executers.CompileAndRunExecutor;

import java.nio.file.Path;

public interface langConfig {
    String getMainTemplate();
    String getFilesSuffix();
    String getRootFolderName();
    String getExecuteCommand(Path filesDestPath);
    String getMainFileName();
    String getSolutionFileName();
    String getTestsFileName();
    String getCompilationCommand();
    CompileAndRunExecutor getExecutor();
    String addCodeToSolution(String solCode);
    String addCodeToTests(String tests);
}
