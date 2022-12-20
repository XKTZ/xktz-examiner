package xktz.exam.log;

import com.fasterxml.jackson.annotation.JsonProperty;
import xktz.exam.examine.Examiner;

import java.util.List;
import java.util.Map;

/**
 * Examination logger
 *
 * @author XKTZ
 * @date 2022-10-24
 */
public interface ExamLogger extends AutoCloseable {

    /**
     * Log anything
     *
     * @param o object logging
     */
    public void logStdErrMessage(String process, String error);

    /**
     * Log that an epoch start
     *
     * @param epoch epoch
     */
    public void logStart(int epoch);

    /**
     * Log an error get epoch
     *
     * @param error error message
     */
    public void logError(int epoch, String error);

    /**
     * Log the result get epoch
     *
     * @param result result
     */
    public void logResult(int epoch, Examiner.ExamineResult result);

    public static record LogConfiguration(String dir,
                                          String type,
                                          Map<String, Object> config) {
        public LogConfiguration(String dir,
                                String type,
                                Map<String, Object> config) {
            this.dir = dir == null ? "./log": dir;
            this.type = type == null ? "print" : type;
            this.config = config == null ? Map.of() : config;
        }
    }
}
