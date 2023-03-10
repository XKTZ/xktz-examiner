package xktz.exam;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import xktz.exam.examine.Examiner;
import xktz.exam.examine.ExaminerProvider;
import xktz.exam.environment.Environment;
import xktz.exam.environment.ProcessExecutionFailedException;
import xktz.exam.lang.LanguageRuntime;
import xktz.exam.lang.LanguageRuntimeProvider;
import xktz.exam.log.ExamLogger;
import xktz.exam.log.ExamLoggerProvider;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;

/**
 * Exam exam the code
 *
 * @author XKTZ
 * @date 2022-10-20
 */
public class Exam {

    /**
     * Write byte into file, instead of charsetted string
     */
    private static final String CHARSET_BYTE = "byte";

    /**
     * Configuration
     */
    private ExaminerConfiguration config;

    /**
     * Work directory
     */
    private final String workDirectory;

    /**
     * logResult path
     */
    private final String logDir;

    /**
     * epoch runs each test
     */
    private final int epoch;

    /**
     * Project code
     */
    private final LanguageRuntime project;

    /**
     * Random case generator
     */
    private final LanguageRuntime generator;

    /**
     * Exam
     */
    private final Examiner examiner;

    /**
     * Logger
     */
    private final ExamLogger logger;

    /**
     * Transform output or not
     */
    private final boolean transformOutput;

    /**
     * Charset of output
     */
    private final Charset charset;

    /**
     * Create examiner by passing work directory and config
     *
     * @param workDirectory work directory
     * @param config        config
     */
    public Exam(String workDirectory, ExaminerConfiguration config) {
        this.workDirectory = workDirectory;
        new File(workDirectory).mkdirs();

        this.epoch = config.epoch;
        if (config.charset.equalsIgnoreCase(CHARSET_BYTE)) {
            transformOutput = false;
            this.charset = null;
        } else {
            transformOutput = true;
            this.charset = Charset.forName(config.charset);
        }

        this.project = LanguageRuntimeProvider.getRuntime(workDirectory, config.project);
        this.generator = LanguageRuntimeProvider.getRuntime(workDirectory, config.generator);
        this.examiner = ExaminerProvider.getExaminer(workDirectory, config.examiner);

        this.logger = ExamLoggerProvider.getExamLogger(workDirectory, config.log);
        this.logDir = Path.of(config.log.dir).isAbsolute() ? config.log.dir
                : canonicalPath(workDirectory + File.separator + config.log.dir);
        new File(logDir).mkdirs();

        this.config = config;
    }

    /**
     * Build the project
     *
     * @param buildProject   build project or not
     * @param buildGenerator build generator or not
     * @param buildExaminer  build examiner or not
     */
    public void build(boolean buildProject, boolean buildGenerator, boolean buildExaminer) {
        if (buildProject) {
            var pout = project.compile();
            handleSystemOutput("Project built", pout);
        }
        if (buildGenerator) {
            var gout = generator.compile();
            handleSystemOutput("Generator built", gout);
        }
        if (buildExaminer) {
            var eout = examiner.build();
            handleSystemOutput("Examiner built", eout);
        }
    }

    public void run(byte[] input) {
        project.runInherited(input);
    }

    /**
     * Test the codes
     */
    public void examine() throws IOException {
        var stat = new ExamStatus();
        for (int i = 1; i <= epoch; i++) {
            if (!epoch(i, stat)) {
                break;
            }
        }

        logger.logStatistic(stat.total, stat.success, stat.failed, stat.error);

    }

    /**
     * Run an epoch
     *
     * @return continue or not
     */
    private boolean epoch(int i, ExamStatus status) {
        status.total++;
        var pathInput = Paths.get(logDir + File.separator + i + ".in");
        var pathOutput = Paths.get(logDir + File.separator + i + ".out");
        var pathExpect = Paths.get(logDir + File.separator + i + ".expect");

        Environment.SystemOutput testCase = null, projectOutput = null;

        Examiner.ExamineResult examinerOutput = null;

        try {
            logger.logStart(i);

            // test the code
            testCase = generateTestCase();

            handleSystemOutput("Test Case Generation", testCase);

            projectOutput = project.run(testCase.stdout());

            handleSystemOutput("Project Running", projectOutput);

            examinerOutput = examiner.examine(testCase.stdout(), projectOutput.stdout());

            switch (examinerOutput.state()) {
                case Examiner.SUCCESS -> status.success++;
                case Examiner.FAILED -> status.failed++;
                case Examiner.ERROR -> status.error++;
            }
        } catch (Environment.TimeoutException e) {
            logger.logError(i, e.getMessage());
            status.failed ++;
        } catch (ProcessExecutionFailedException e) {
            logger.logError(i, "Process <%s> failed".formatted(e.getMessage()));
            status.failed ++;
        } catch (Exception e) {
            logger.logError(i, e.getMessage());
        }

        if (testCase != null) {
            try {
                // write the possible outputs into the system
                writeCase(pathInput, testCase.stdout());
            } catch (IOException e) {
                logger.logError(i, e.getMessage());
            }
        }

        if (projectOutput != null) {
            try {
                writeCase(pathOutput, projectOutput.stdout());
            } catch (IOException e) {
                logger.logError(i, e.getMessage());
            }
        }

        if (examinerOutput != null && examinerOutput.info().isPresent()) {
            try {
                writeCase(pathExpect, examinerOutput.info().get());
            } catch (IOException e) {
                logger.logError(i, e.getMessage());
            }
        }

        if (examinerOutput != null) {
            // logStdErrMessage the end
            logger.logResult(i, examinerOutput);
        }
        return true;
    }

    private Environment.SystemOutput generateTestCase() {
        return generator.run(config.generator.getOrDefault("input", "").toString().getBytes(Environment.SYSTEM_CHARSET));
    }

    /**
     * Handle a single system output
     * It will throw a process execution failed exception if the process is failed,
     *
     * @param process process
     * @param output  output of system
     */
    private void handleSystemOutput(String process, Environment.SystemOutput output) {
        logErrorStreamIfPresent(process, output);
        if (output.exitCode() != Environment.EXIT_SUCCESS) {
            throw new ProcessExecutionFailedException(process);
        }
    }

    /**
     * Log an error stream if its size >= 1
     *
     * @param output output
     */
    private void logErrorStreamIfPresent(String process, Environment.SystemOutput output) {
        if (output.stderr().length > 0) {
            logger.logStdErrMessage(process, new String(output.stderr(), Environment.SYSTEM_CHARSET));
        }
    }

    /**
     * Write test case into path
     *
     * @param path path
     * @param data data
     */
    private void writeCase(Path path, byte[] data) throws IOException {
        if (Files.exists(path)) {
            Files.delete(path);
        }
        if (transformOutput) {
            Files.writeString(path, new String(data, Environment.SYSTEM_CHARSET), charset, StandardOpenOption.CREATE_NEW);
        } else {
            Files.write(path, data, StandardOpenOption.CREATE_NEW);
        }
    }

    /**
     * Get a canonical path of a file
     *
     * @param path file
     * @return canonical path
     */
    protected static String canonicalPath(String path) {
        try {
            return new File(path).getCanonicalPath();
        } catch (IOException e) {
            throw new LanguageRuntime.CompilationException(e.getMessage());
        }
    }

    public static class ExaminerConfiguration {
        public int epoch;

        public ExamLogger.LogConfiguration log = new ExamLogger.LogConfiguration();

        public Map<String, Object> project;

        public Map<String, Object> generator;

        public Map<String, Object> examiner;

        public String charset;

        @Override
        public String toString() {
            return "ExaminerConfiguration{" +
                    "epoch=" + epoch +
                    ", log=" + log +
                    ", project=" + project +
                    ", generator=" + generator +
                    ", examiner=" + examiner +
                    ", charset='" + charset + '\'' +
                    '}';
        }
    }

    private static class ExamStatus {
        int success = 0;
        int failed = 0;
        int error = 0;
        int total = 0;
    }
}
