package xktz.exam.examine;

import xktz.exam.environment.Environment;

import java.util.Optional;

/**
 * Examiner, comparing two codes are correct or not
 *
 * @author XKTZ
 * @date 2022-10-20
 */
public interface Examiner {

    /**
     * Examine success
     */
    int SUCCESS = 0;
    /**
     * Examine exception
     */
    int ERROR = 2;
    /**
     * Examine failed
     */
    int FAILED = 1;

    ExamineResult examine(byte[] input, byte[] output);

    Environment.SystemOutput build();

    /**
     * The result for an examine.
     *
     * @param state   success, failed, or running exception
     * @param message message
     * @param info    information written into the files.
     */
    public record ExamineResult(int state, String message, Optional<byte[]> info) {
    }
}
