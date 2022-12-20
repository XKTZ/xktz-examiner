package xktz.exam.lang.java;

import xktz.exam.environment.Environment;
import xktz.exam.lang.LanguageRuntime;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Runtime for java
 *
 * @author XKTZ
 * @date 2022-11-08
 */
public class JavaRuntime extends LanguageRuntime {

    private static final String KEY_SRC = "src";

    private static final String KEY_OUT = "out";

    private static final String KEY_CP = "cp";

    private static final String KEY_MAIN = "main";

    private static final String KEY_TIME_LIMIT = "timeLimit";

    /**
     * Source directory
     */
    private final String src;

    /**
     * out directory
     */
    private final String out;

    /**
     * class path
     */
    private final List<String> classPath;

    /**
     * main class directory
     */
    private final String main;

    /**
     * Limit for compilation
     */
    private final int compileLimit;

    /**
     * time limit
     */
    private final int timeLimit;

    public JavaRuntime(String workDirectory, String projectDirectory,
                       Environment environment, Map<String, Object> config) {
        super(workDirectory, projectDirectory, environment, config);
        var srcProperty = property(KEY_SRC, new PropertyType<String>() {
        });
        src = new File(srcProperty).isAbsolute() ? srcProperty : projectFile(srcProperty);
        var outProperty = property(KEY_OUT, new PropertyType<String>() {
        });
        out = new File(outProperty).isAbsolute() ? outProperty : projectFile(outProperty);
        classPath = property(KEY_CP, new PropertyType<>() {
        });
        main = property(KEY_MAIN, new PropertyType<>() {
        });
        compileLimit = property(KEY_TIME_LIMIT, new PropertyType<>() {
        });
        timeLimit = property(KEY_TIME_LIMIT, new PropertyType<>() {
        });
    }

    @Override
    public Environment.SystemOutput compile() {
        List<String> commands = new ArrayList<>();
        var mainPath = src + File.separator + main.replace(".", File.separator) + ".java";
        commands.add("javac");
        if (!this.classPath.isEmpty()) {
            commands.add("-cp");
            commands.add(String.join(";", this.classPath));
        }
        commands.add("-sourcepath");
        commands.add(src);
        commands.add(mainPath);
        commands.add("-d");
        commands.add(out);
        System.err.println(String.join(" ", commands));
        return environment.executeCommand(compileLimit,
                commands.toArray(String[]::new));
    }

    @Override
    public Environment.SystemOutput run(byte[] input) {
        return environment.executeCommand(out,
                timeLimit, input,
                "java", main);
    }

    @Override
    protected Map<String, Object> defaultProperties() {
        return Map.of(
                "compileLimit", 10000,
                "timeLimit", 1000,
                "cp", new ArrayList<>()
        );
    }

    /**
     * Initialize configuration
     *
     * @return init config
     */
    public static Function<String, Map<String, Object>> initConfiguration() {
        return (name) -> Map.of(
                "lang", "java",
                "src", "",
                "main", ""
        );
    }
}
