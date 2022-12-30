package xktz.exam.lang;

import org.apache.commons.io.FileUtils;
import xktz.exam.environment.Environment;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * A runtime for different language
 *
 * @author XKTZ
 * @date 2022-10-19
 */
public abstract class LanguageRuntime {

    /**
     * Key for project directory
     */
    public static final String KEY_PROJECT_DIRECTORY = "dir";

    /**
     * Key printing command to stderr
     */
    protected static final String KEY_COMMAND_OUTPUT = "commandOutput";

    /**
     * Temporary folder
     */
    private static final String TEMPORARY_DIRECTORY = ".tmp";

    /**
     * Work dir
     */
    protected final String workDirectory;

    /**
     * Directory get project
     */
    protected final String projectDirectory;

    /**
     * Temporary folder path
     */
    private final String temporaryDirectory;

    /**
     * Environment
     */
    protected final Environment environment;

    /**
     * The properties
     */
    private final Map<String, Object> config;

    /**
     * Default properties
     */
    private Map<String, Object> defaultProperties;

    /**
     * Default properties
     */
    private List<String> commandOutput;

    /**
     * Create a language runtime by providing working directory and environment
     *
     * @param workDirectory    working directory
     * @param projectDirectory project directory
     * @param environment      environment
     * @param config           the configuration
     */
    public LanguageRuntime(String workDirectory, String projectDirectory, Environment environment, Map<String, Object> config) {
        this.workDirectory = canonicalPath(workDirectory, false);
        this.projectDirectory = Path.of(projectDirectory).isAbsolute() ? canonicalPath(projectDirectory, false)
                : canonicalPath(workDirectory + File.separator + projectDirectory, false);
        this.temporaryDirectory = canonicalPath(workDirectory + File.separator + TEMPORARY_DIRECTORY, false);
        this.environment = environment;
        this.config = config;
        this.commandOutput = Optional.ofNullable(property(KEY_COMMAND_OUTPUT, new PropertyType<List<String>>() {
        })).orElse(List.of("STDERR"));
    }

    /**
     * Create a language runtime by providing working directory and environment
     *
     * @param workDirectory working directory
     * @param environment   environment
     */
    public LanguageRuntime(String workDirectory, String projectDirectory, Environment environment) {
        this(workDirectory, projectDirectory, environment, Map.of());
    }

    /**
     * Compile the language
     */
    public abstract Environment.SystemOutput compile();

    /**
     * Run the code providing input, output, and error. Assume charset of "input" is system charset
     *
     * @param input the input for the language, the thing written in stdin
     * @return the result after running (stdout, stderr)
     */
    public abstract Environment.SystemOutput run(byte[] input);

    /**
     * Run the code providing input. Use inherited IO.
     *
     * @param input the input for the language, the thing written in stdin
     * @return the result after running (stdout, stderr)
     */
    public abstract void runInherited(byte[] input);

    /**
     * Get a project file
     *
     * @param name name
     * @return project file
     */
    protected final String projectFile(String name) {
        return projectDirectory + File.separator + name;
    }

    /**
     * Request a temporary directory under work directory
     *
     * @return temporary directory
     */
    protected final String requestTemporary() {
        try {
            Files.createDirectories(Path.of(temporaryDirectory));
        } catch (IOException e) {
            throw new CompilationException(e);
        }
        return temporaryDirectory;
    }

    /**
     * Get the path of a file under temporary folder
     *
     * @param name file name
     * @return file
     */
    protected final String temporaryFileOf(String name) {
        return temporaryDirectory + File.separator + name;
    }

    /**
     * Delete the temporary folder
     */
    protected final void disposeTemporary() {
        if (new File(temporaryDirectory).exists()) {
            try {
                FileUtils.deleteDirectory(new File(temporaryDirectory));
            } catch (IOException e) {
                throw new CompilationException(e);
            }
        }
    }

    /**
     * Get a property from the config, use property type check
     *
     * @param key key
     * @return property value
     */
    protected final <T> T property(String key, PropertyType<T> type) {
        return property(key, type, true);
    }

    /**
     * Get a property get the config
     *
     * @param key        key
     * @param assertType assert type or not
     * @return property value
     */
    protected final <T> T property(String key, PropertyType<T> type, boolean assertType) {
        var obj = config.get(key);
        if (obj != null && assertType) {
            assertType(key, List.of(type.type), obj.getClass());
        }
        if (obj == null) {
            assertDefaultPropertyInited();
            obj = defaultProperties.get(key);
        }
        return (T) obj;
    }

    /**
     * Assert that the actual object is same as one get the required type. If it is not, return false
     *
     * @param requires required
     * @param actual   actual
     */
    protected static void assertType(String fieldName, Collection<Class<?>> requires, Class<?> actual) {
        for (var require : requires) {
            if (require.isAssignableFrom(actual)) {
                return;
            }
        }
        throw new ConfigurationErrorTypeException(fieldName, requires, actual);
    }

    /**
     * Default properties
     *
     * @return default properties
     */
    protected Map<String, Object> defaultProperties() {
        return Map.of();
    }

    /**
     * Assert that default property is inited
     */
    private void assertDefaultPropertyInited() {
        if (defaultProperties == null) {
            this.defaultProperties = defaultProperties();
        }
    }

    /**
     * Get a canonical path of a file
     *
     * @param path file
     * @return canonical path
     */
    protected static String canonicalPath(String path, boolean formatted) {
        try {
            if (formatted) {
                return "\"" + new File(path).getCanonicalPath() + "\"";
            } else {
                return new File(path).getCanonicalPath();
            }
        } catch (IOException e) {
            throw new CompilationException(e.getMessage());
        }
    }

    /**
     * Get a canonical path of a file
     *
     * @param path file
     * @return canonical path
     */
    protected static String canonicalPath(String path) {
        return canonicalPath(path, true);
    }

    /**
     * Exception happens in compilation
     */
    public static class CompilationException extends RuntimeException {
        /**
         * Create compilation exception by message
         *
         * @param msg
         */
        public CompilationException(String msg) {
            super(msg);
        }

        /**
         * Create compilation exception by parent exception
         *
         * @param e
         */
        public CompilationException(Throwable e) {
            super(e);
        }
    }

    /**
     * Exception when there exists error in language configuration
     */
    public static abstract class LanguageConfigurationException extends RuntimeException {
        public LanguageConfigurationException(String msg) {
            super("Error in language configuration: " + msg);
        }
    }

    /**
     * Exception that the type is error in configuration variable types
     */
    public static class ConfigurationErrorTypeException extends LanguageConfigurationException {

        private static final String ERROR_MESSAGE_FORMAT = "Field <%s> expected to be <%s>, gotten <%s>";

        public ConfigurationErrorTypeException(String fieldName, Collection<Class<?>> expected, Class<?> error) {
            super(ERROR_MESSAGE_FORMAT.formatted(fieldName,
                    String.join("/", expected.stream().map(Class::getCanonicalName).toList()),
                    error.getCanonicalName()));
        }
    }

    protected static abstract class PropertyType<T> {
        private final Class<?> type;

        public PropertyType() {
            var tp = getClass().getGenericSuperclass();
            if (tp instanceof Class<?>) {
                throw new RuntimeException();
            }
            var param = (ParameterizedType) tp;
            var tpGeneric = param.getActualTypeArguments()[0];
            if (tpGeneric instanceof Class<?> typeGeneric) {
                this.type = typeGeneric;
            } else {
                this.type = (Class<?>) ((ParameterizedType) tpGeneric).getRawType();
            }
        }

        public static <T> PropertyType<T> get() {
            return new PropertyType<T>() {
            };
        }
    }

    /**
     * Output a command into different positions
     *
     * @param command message
     */
    protected void outputCommand(String command) {
        for (var out : commandOutput) {
            try {
                var output = Output.valueOf(out.toUpperCase());
                if (output == Output.STDOUT) {
                    System.out.println(command);
                }
                if (output == Output.STDERR) {
                    System.err.println(command);
                }
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Different output paths
     */
    protected enum Output {
        STDOUT,
        STDERR
    }
}
