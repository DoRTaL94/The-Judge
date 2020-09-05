package executers;

import exceptions.ExecuterException;

public class JavaExecutor extends CompileAndRunExecutor {
    @Override
    protected String addSecurityManager(String tests) throws ExecuterException {
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
}
