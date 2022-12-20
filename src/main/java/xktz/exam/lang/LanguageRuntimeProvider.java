package xktz.exam.lang;

import xktz.exam.environment.Environment;
import xktz.exam.environment.StandardEnvironment;
import xktz.exam.lang.cpp.CppRuntime;
import xktz.exam.lang.java.JavaRuntime;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Static method give language runtime by setting
 *
 * @author XKTZ
 * @date 2022-10-20
 */
public class LanguageRuntimeProvider {

    /**
     * map mapping language name to function
     */
    private static final Map<String, LanguageRuntimeGenerator<?>> languageRuntimeMap;

    /**
     * Init configuration map
     */
    private static final Map<String, Function<String, Map<String, Object>>> initConfig;

    /**
     * key for language in config
     */
    private static final String CONFIG_KEY_LANG = "lang";

    /**
     * Language for text runtime, it is the default runtime when there isn't other settings
     */
    private static final String LANG_TEXT = "text";

    static {
        languageRuntimeMap = new ConcurrentHashMap<>();
        initConfig = new ConcurrentHashMap<>();
        languageRuntimeMap.put("c", (workDirectory, environment, config) ->
                new CppRuntime(workDirectory,
                        config.getOrDefault(LanguageRuntime.KEY_PROJECT_DIRECTORY, "").toString(),
                        environment,
                        config));
        languageRuntimeMap.put("cpp", (workDirectory, environment, config) ->
                new CppRuntime(workDirectory,
                        config.getOrDefault(LanguageRuntime.KEY_PROJECT_DIRECTORY, "").toString(),
                        environment,
                        config));
        languageRuntimeMap.put("c++", (workDirectory, environment, config) ->
                new CppRuntime(workDirectory,
                        config.getOrDefault(LanguageRuntime.KEY_PROJECT_DIRECTORY, "").toString(),
                        environment,
                        config));
        languageRuntimeMap.put("java", (workDirectory, environment, config) ->
                new JavaRuntime(workDirectory,
                        config.getOrDefault(LanguageRuntime.KEY_PROJECT_DIRECTORY, "").toString(),
                        environment,
                        config));
        initConfig.put("c", CppRuntime.initConfiguration());
        initConfig.put("cpp", CppRuntime.initConfiguration());
        initConfig.put("c++", CppRuntime.initConfiguration());
        initConfig.put("java", JavaRuntime.initConfiguration());
    }

    /**
     * Get a runtime with default environment
     *
     * @param workDirectory work directory
     * @param config        config
     * @return runtime
     */
    public static LanguageRuntime getRuntime(String workDirectory, Map<String, Object> config) {
        return getRuntime(workDirectory, null, config);
    }

    /**
     * Get a runtime with specified environment
     *
     * @param workDirectory work directory
     * @param environment   environment
     * @param config        config
     * @return runtime
     */
    public static LanguageRuntime getRuntime(String workDirectory, Environment environment, Map<String, Object> config) {
        if (environment == null) {
            environment = new StandardEnvironment(workDirectory);
        }
        var lang = config.getOrDefault(CONFIG_KEY_LANG, LANG_TEXT).toString().toLowerCase();
        if (languageRuntimeMap.containsKey(lang)) {
            return languageRuntimeMap.get(lang).generate(workDirectory, environment, config);
        }
        throw new LanguageNotSupportedException(lang);
    }

    /**
     * Get the language configuration from the language name
     *
     * @param lang language
     * @return init config
     */
    public static Map<String, Object> initConfiguration(String name, String lang) {
        return initConfig.getOrDefault(lang, (x) -> Map.of()).apply(name);
    }

    public static class LanguageNotSupportedException extends RuntimeException {
        public LanguageNotSupportedException(String languageName) {
            super("Language <%s> is not supported".formatted(languageName));
        }
    }

    /**
     * A language runtime generator
     *
     * @param <T> type of runtime generating
     */
    public static interface LanguageRuntimeGenerator<T extends LanguageRuntime> {
        T generate(String workDirectory, Environment environment, Map<String, Object> config);
    }
}
