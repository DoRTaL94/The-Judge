package models;

import exceptions.ExecuterException;
import services.TemplateService;

import java.io.IOException;

public interface ICodeExecuter {
    void init(TemplateService templateService, String filesDest, String solCode, String tests) throws IOException, InterruptedException, ExecuterException;
    ProgramOutput compile() throws Exception;
    ProgramOutput run() throws Exception;
}
