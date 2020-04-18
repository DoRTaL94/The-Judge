package models;

import services.TemplateService;

import java.io.IOException;

public interface ICodeExecuter {
    void init(TemplateService templateService, String filesDest, String solCode, String tests) throws IOException, InterruptedException;
    ProgramOutput compile() throws Exception;
    ProgramOutput run() throws Exception;
}
