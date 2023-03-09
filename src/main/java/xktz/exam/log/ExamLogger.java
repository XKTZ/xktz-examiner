package xktz.exam.log;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import xktz.exam.Main;
import xktz.exam.examine.Examiner;

import java.io.IOException;
import java.io.InputStream;
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
     * Log final statistic of running
     *
     * @param total   total
     * @param success success
     * @param failed  failed
     * @param error   error
     */
    public void logStatistic(int total, int success, int failed, int error);

    /**
     * Log the result get epoch
     *
     * @param result result
     */
    public void logResult(int epoch, Examiner.ExamineResult result);

    public static class LogConfiguration {
        private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

        public String dir = "./log";

        public String type = "print";

        public Map<String, Object> config = Map.of();

        /**
         * Get configuration
         * @param stream stream
         */
        public static LogConfiguration getConfiguration(InputStream stream) throws IOException {
            return OBJECT_MAPPER.readValue(stream, LogConfiguration.class);
        }

    }
}
