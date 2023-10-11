package xktz.exam.lang.cpp;

import xktz.exam.environment.Environment;
import xktz.exam.environment.system.SystemType;
import xktz.exam.lang.LanguageRuntime;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Runtime for c/c++
 *
 * @author XKTZ
 * @date 2022-10-25
 */
public class CppRuntime extends LanguageRuntime {

    /**
     * Key for time limit
     */
    private static final String KEY_TIME_LIMIT = "time";

    /**
     * Key for includes
     */
    private static final String KEY_COMPILER = "compiler";

    /**
     * Key for files
     */
    private static final String KEY_FILES = "files";

    /**
     * Key for output
     */
    private static final String KEY_OUTPUT = "out";

    /**
     * Key for includes
     */
    private static final String KEY_INCLUDES = "includes";

    /**
     * Library directories
     */
    private static final String KEY_LIB_DIRS = "libdirs";

    /**
     * Key for libs
     */
    private static final String KEY_LIBS = "libs";

    /**
     * Key for llvm text files
     */
    private static final String KEY_LLVM_TEXT_FILES = "lls";

    /**
     * Key for command in running
     */
    private static final String KEY_RUN_BY = "runby";

    /**
     * Key for extra args
     */
    private static final String KEY_EXTRA_ARGS = "args";

    /**
     * G++ compiler
     */
    private static final String GPP_COMPILER = "g++";

    /**
     * Time limit
     */
    private final int timeLimit;

    /**
     * Compiler of the runtime. Init is g++
     */
    private final String compiler;

    /**
     * files
     */
    private final List<String> files;

    /**
     * Output directory
     */
    private final String out;

    /**
     * Include list
     */
    private final List<String> includes;

    /**
     * lib directories
     */
    private final List<String> libDirectories;

    /**
     * Libraries
     */
    private final List<String> libs;

    /**
     * LLVM text files need to link
     */
    private final List<String> llvmTextFiles;

    /**
     * Running using a specific command
     */
    private final String runBy;

    /**
     * Extra arguments
     */
    private final List<String> extraArgs;

    /**
     * @param workDirectory    work directory
     * @param projectDirectory project directory
     * @param environment      environment
     * @param config           configuration
     */
    public CppRuntime(String workDirectory, String projectDirectory,
                      Environment environment, Map<String, Object> config) {
        super(workDirectory, projectDirectory, environment, config);
        this.timeLimit = property(KEY_TIME_LIMIT, new PropertyType<>() {
        });
        this.compiler = property(KEY_COMPILER, new PropertyType<>() {
        });
        this.files = property(KEY_FILES, new PropertyType<>() {
        });
        this.out = workDirectory + File.separator + property(KEY_OUTPUT, new PropertyType<>() {
        });
        this.includes = property(KEY_INCLUDES, new PropertyType<>() {
        });
        this.libDirectories = property(KEY_LIB_DIRS, new PropertyType<>() {
        });
        this.libs = property(KEY_LIBS, new PropertyType<>() {
        });
        this.runBy = property(KEY_RUN_BY, new PropertyType<>() {
        });
        this.llvmTextFiles = property(KEY_LLVM_TEXT_FILES, new PropertyType<>() {
        });
        this.extraArgs = property(KEY_EXTRA_ARGS, new PropertyType<>() {
        });
    }

    @Override
    public Environment.SystemOutput compile() {
        requestTemporary();
        var temps = compileLLVM();

        // generate the compilation command
        List<String> commands = new ArrayList<>();
        commands.add(compiler);
        // add files and temps into
        commands.addAll(files.stream().map(s -> {
            var p = Path.of(s);
            if (p.isAbsolute()) {
                return canonicalPath(s);
            } else {
                return canonicalPath(projectFile(s));
            }
        }).toList());
        commands.addAll(temps);
        // add include
        commands.addAll(includes.stream().map(s -> {
            var p = Path.of(s);
            if (p.isAbsolute()) {
                return "-I" + canonicalPath(s);
            } else {
                return "-I" + canonicalPath(projectFile(s));
            }
        }).toList());
        // add lib directories file
        commands.addAll(libDirectories.stream().map(s -> {
            var p = Path.of(s);
            if (p.isAbsolute()) {
                return "-L" + canonicalPath(s);
            } else {
                return "-L" + canonicalPath(projectFile(s));
            }
        }).toList());
        // add libs
        commands.addAll(libs.stream().map(s -> "-l" + s).toList());
        // set output
        if (out != null) {
            commands.add("-o" + canonicalPath(out));
        }
        commands.addAll(extraArgs);

        outputCommand(String.join(" ", commands));

        try {
            // then, compile the code
            return environment.executeCommand(10000, commands.toArray(String[]::new));
        } finally {
            disposeTemporary();
        }
    }

    private Function<String, String[]> getParser() {
        return (s) -> {
            var cmd = runBy.split(" ");
            for (int i = 0; i <cmd.length; i ++) {
                if (cmd[i].equals("$")) {
                    cmd[i] = s;
                }
            }
            return cmd;
        };
    }

    @Override
    public Environment.SystemOutput run(byte[] input) {
        var cmd = getParser().apply(canonicalPath(out + SystemType.executableExtension()));
        outputCommand(
                String.join(" ", cmd)
        );
        return environment.executeCommand(timeLimit, input, cmd);
    }

    @Override
    public void runInherited(byte[] input) {
        var cmd = getParser().apply(canonicalPath(out + SystemType.executableExtension()));
        outputCommand(
                String.join(" ", cmd)
        );
        environment.executeInheritIOCommand(cmd);
    }

    /**
     * Compile the llvm text files
     *
     * @return compile the llvm .ll files
     */
    private List<String> compileLLVM() {
        List<String> files = new ArrayList<>();
        // firstly, compile the llvm text files into .o files
        for (var path : llvmTextFiles) {
            var f = Path.of(path);
            String outputPath = temporaryFileOf(f.getFileName().toString()) + ".o";

            Environment.SystemOutput result;
            if (f.isAbsolute()) {
                result = ClangUtils.compileLLVMTextTo(environment, f.toString(), outputPath);
            } else {
                result = ClangUtils.compileLLVMTextTo(environment, projectFile(path), outputPath);
            }
            if (result.exitCode() != Environment.EXIT_SUCCESS) {
                throw new CompilationException(
                        result.stderr().length > 0 ? new String(result.stderr(), Environment.SYSTEM_CHARSET) : ""
                );
            }
            files.add(canonicalPath(outputPath));
        }
        return files;
    }

    @Override
    protected Map<String, Object> defaultProperties() {
        return Map.of(
                KEY_TIME_LIMIT, 1000,
                KEY_COMMAND_OUTPUT, List.of("stderr"),
                KEY_COMPILER, GPP_COMPILER,
                KEY_FILES, List.of(),
                KEY_INCLUDES, List.of(),
                KEY_LIB_DIRS, List.of(),
                KEY_LIBS, List.of(),
                KEY_LLVM_TEXT_FILES, List.of(),
                KEY_RUN_BY, "$",
                KEY_EXTRA_ARGS, List.of()
        );
    }

    /**
     * Init config of c++ runtime
     *
     * @return init config
     */
    public static Function<String, Map<String, Object>> initConfiguration(String lang) {
        if (lang.equals("c")) {
            return (name) -> Map.of(
                    "lang", "c",
                    "compiler", "gcc",
                    "files", List.of(),
                    "out", name
            );
        } else {
            return (name) -> Map.of(
                    "lang", "cpp",
                    "compiler", "g++",
                    "files", List.of(),
                    "out", name
            );
        }
    }
}
