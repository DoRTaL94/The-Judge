package services;

public class TemplateService {
    private final String javaMain = "import org.junit.runner.JUnitCore;\n" +
            "import org.junit.runner.Result;\n" +
            "import org.junit.runner.notification.Failure;\n" +
            "import java.util.List;\n" +
            "public class Main {\n" +
            "    public static void main(String[] args) {\n" +
            "        long start = System.nanoTime();\n" +
            "        Result result = JUnitCore.runClasses(Tests.class);\n" +
            "        long end = System.nanoTime();\n" +
            "        float duration = (end - start) / 1000000000f;\n" +
            "        duration = Math.round(duration * 100f) / 100f;\n" +
            "        System.out.print(\"--cut-here--\");\n" +
            "        System.out.print(String.format(\"%s\", duration));\n" +
            "        System.out.print(\"--cut-here--\");\n" +
            "        List<Failure> failures = result.getFailures();\n" +
            "        for (Failure failure: failures) {\n" +
            "            String testHeader = failure.getTestHeader();\n" +
            "            System.out.print(testHeader.substring(0, testHeader.indexOf('(')) + \" \");\n" +
            "        }\n" +
            "    }\n" +
            "}";

    public String getMainTemplate(String programmingLanguage) {
        String mainTemplate;
        programmingLanguage = programmingLanguage.toLowerCase();

        switch (programmingLanguage) {
            case "java":
                mainTemplate = javaMain;
                break;
            default:
                mainTemplate = null;
                break;
        }

        return mainTemplate;
    }
}