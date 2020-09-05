package executers;

import exceptions.ExecuterException;

public class JavaScriptExecutor extends CompileAndRunExecutor {
    @Override
    protected String addSecurityManager(String tests) throws ExecuterException {
        return tests;
    }
}
