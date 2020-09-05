package models;

import exceptions.ExecuterException;
import services.Language;
import services.LangConfigService;

import java.io.IOException;

public interface ICodeExecuter {
    void init(LangConfigService langConfigService, Language lang, String filesDest, String solCode, String tests) throws IOException, InterruptedException, ExecuterException;
    ProgramOutput compile() throws Exception;
    ProgramOutput run() throws Exception;
}
