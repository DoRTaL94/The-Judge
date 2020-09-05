package services;

import javax.servlet.ServletContext;

public class ServletsService {
    private static final String CODE_SERVICE_ATTRIBUTE_NAME = "CodeService";
    private static final String TEMPLATE_SERVICE_ATTRIBUTE_NAME = "TemplateService";
    private static final Object codeServiceLock = new Object();
    private static final Object templateServiceLock = new Object();

    public static CodeService getCodeService(ServletContext i_ServletContext) {

        synchronized (codeServiceLock) {
            if (i_ServletContext.getAttribute(CODE_SERVICE_ATTRIBUTE_NAME) == null) {
                i_ServletContext.setAttribute(CODE_SERVICE_ATTRIBUTE_NAME, new CodeService(getTemplateService(i_ServletContext)));
            }
        }

        return (CodeService) i_ServletContext.getAttribute(CODE_SERVICE_ATTRIBUTE_NAME);
    }

    public static LangConfigService getTemplateService(ServletContext i_ServletContext) {

        synchronized (templateServiceLock) {
            if (i_ServletContext.getAttribute(TEMPLATE_SERVICE_ATTRIBUTE_NAME) == null) {
                i_ServletContext.setAttribute(TEMPLATE_SERVICE_ATTRIBUTE_NAME, new LangConfigService());
            }
        }

        return (LangConfigService) i_ServletContext.getAttribute(TEMPLATE_SERVICE_ATTRIBUTE_NAME);
    }
}
