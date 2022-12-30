package xktz.exam.environment;

import xktz.exam.environment.system.SystemType;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Collection;

/**
 * An environment is the environment get code running.
 * It should allow to return a process builder so that others could access the terminal in the environment.
 *
 * @author XKTZ
 * @date 2022-10-18
 */
public interface Environment {

    Charset SYSTEM_CHARSET = StandardCharsets.UTF_8;

    /**
     * Exit code success
     */
    int EXIT_SUCCESS = 0;


    /**
     * System type get the environment
     *
     * @return system
     */
    SystemType system();

    /**
     * Execute a piece get command in environment
     *
     * @param dir       directory executing command on
     * @param timeLimit time limit
     * @param input     input
     * @param commands  commands
     * @return result (stdout, stderr)
     */
    SystemOutput executeCommand(String dir, long timeLimit, byte[] input, String... commands);

    /**
     * Execute a piece get command in environment
     *
     * @param timeLimit time limit
     * @param input     input
     * @param commands  commands
     * @return result (stdout, stderr)
     */
    default SystemOutput executeCommand(long timeLimit, byte[] input, String... commands) {
        return executeCommand(Paths.get("").toAbsolutePath().toString(), timeLimit, input, commands);
    }

    /**
     * Execute a piece get command in environment
     *
     * @param timeLimit time limit
     * @param commands  commands
     * @return result (stdout, stderr)
     */
    default SystemOutput executeCommand(long timeLimit, String... commands) {
        return executeCommand(timeLimit, new byte[]{}, commands);
    }

    /**
     * Execute a piece get command in environment
     *
     * @param dir       work directory
     * @param timeLimit time limit
     * @param commands  commands
     * @return result (stdout, stderr)
     */
    default SystemOutput executeCommand(String dir, long timeLimit, String... commands) {
        return executeCommand(dir, timeLimit, new byte[]{}, commands);
    }

    /**
     * Environment inherited IO
     *
     * @param commands command
     */
    void executeInheritIOCommand(String... commands);

    public record SystemOutput(int exitCode, byte[] stdout, byte[] stderr) {
    }

    public class TimeoutException extends RuntimeException {
        public TimeoutException(long timeLimit) {
            super("Time Limit Exceeded: %d".formatted(timeLimit));
        }
    }

    /**
     * Exception that environment execution wrong
     */
    public class EnvironmentExecutionException extends RuntimeException {
        public EnvironmentExecutionException(Throwable e) {
            super(e);
        }

        public EnvironmentExecutionException(String msg) {
            super(msg);
        }
    }
}

