package xktz.exam.examine;

import xktz.exam.environment.Environment;
import xktz.exam.environment.StandardEnvironment;
import xktz.exam.lang.LanguageRuntime;
import xktz.exam.lang.LanguageRuntimeProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Provider get examiner based on map config
 *
 * @author XKTZ
 * @date 2022-10-20
 */
public class ExaminerProvider {

    /**
     * Map get examiner creators
     */
    private static final Map<String, ExaminerGenerator<?>> examinerMap;

    /**
     * key for the type get examiner
     */
    private static final String EXAMINER_TYPE_KEY = "type";

    static {
        examinerMap = new ConcurrentHashMap<>();
        examinerMap.put("code", (workDirectory, environment, config) -> new CodeExaminer(LanguageRuntimeProvider.getRuntime(
                workDirectory,
                environment,
                (Map<String, Object>) config.get("runner")
        )));
        examinerMap.put("compare", (workDirectory, environment, config) -> new ComparisonExaminer(
                ComparisonExaminer.ExaminerMode.valueOf(config.get("mode").toString().toUpperCase()),
                LanguageRuntimeProvider.getRuntime(
                        workDirectory,
                        environment,
                        (Map<String, Object>) config.get("runner")
                )
        ));
    }

    /**
     * Get examiner with default environment
     *
     * @param workDirectory work directory
     * @param config        config
     * @return examiner
     */
    public static Examiner getExaminer(String workDirectory, Map<String, Object> config) {
        return getExaminer(workDirectory, null, config);
    }


    /**
     * Get examiner by config
     *
     * @param workDirectory work directory
     * @param environment   environment
     * @param config        config
     * @return examiner
     */
    public static Examiner getExaminer(String workDirectory, Environment environment, Map<String, Object> config) {
        if (!config.containsKey(EXAMINER_TYPE_KEY)) {
            config.put(EXAMINER_TYPE_KEY, "compare");
            config.put("mode", ComparisonExaminer.ExaminerMode.STRICT);
        }
        var type = config.get(EXAMINER_TYPE_KEY).toString();
        var generator = examinerMap.get(type);
        if (generator == null) {
            throw new ExaminerNotSupportedException(type);
        }
        if (environment == null) {
            environment = new StandardEnvironment(workDirectory);
        }
        return generator.generate(workDirectory, environment, config);
    }

    /**
     * Init config of examiner
     *
     * @return examiner conf
     */
    public static Map<String, Object> initConfiguration(String lang) {
        return Map.of(
                "runner", LanguageRuntimeProvider.initConfiguration("examiner", lang)
        );
    }

    /**
     * Exception that examiner is not supported
     */
    public static class ExaminerNotSupportedException extends RuntimeException {
        public ExaminerNotSupportedException(String examiner) {
            super("Examiner with type <%s> is not supported".formatted(examiner));
        }
    }

    /**
     * Examiner generator
     */
    public static interface ExaminerGenerator<T extends Examiner> {
        public T generate(String workDirectory, Environment environment, Map<String, Object> config);
    }
}
