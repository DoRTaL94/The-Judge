package services;

import configs.JavaConfig;
import configs.JavaScriptConfig;
import configs.langConfig;

import java.util.HashMap;
import java.util.Map;

public class LangConfigService {
    private Map<Language, langConfig> configs = new HashMap<>();

    public LangConfigService() {
        this.addLangConfig(Language.JAVA, new JavaConfig());
        this.addLangConfig(Language.JAVASCRIPT, new JavaScriptConfig());
    }

    public langConfig getLangConfig(Language programmingLanguage) {
        return this.configs.get(programmingLanguage);
    }

    public void addLangConfig(Language programmingLanguage, langConfig config) {
        this.configs.put(programmingLanguage, config);
    }
}