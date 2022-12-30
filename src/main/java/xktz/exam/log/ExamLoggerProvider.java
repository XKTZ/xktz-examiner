package xktz.exam.log;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * @author XKTZ
 * @date 2022-10-24
 */
public class ExamLoggerProvider {

    private static Map<String, Function<ExamLogger.LogConfiguration, ExamLogger>> logMap = new ConcurrentHashMap<>();

    static {
        logMap.put("print", (conf) -> new PrintStreamLogger(
                ((List<String>) conf.config.getOrDefault("to", List.of("stdout"))).stream()
                        .map(ExamLoggerProvider::getPrintStream)
                        .toList()
        ));
    }

    public static ExamLogger getExamLogger(String workDirectory, ExamLogger.LogConfiguration config) {
        return logMap.get(config.type).apply(config);
    }

    private static ImmutablePair<PrintStream, Boolean> getPrintStream(String s) {
        if (s.equalsIgnoreCase("stdout")) {
            return ImmutablePair.of(System.out, false);
        } else if (s.equalsIgnoreCase("stderr")) {
            return ImmutablePair.of(System.err, false);
        } else {
            try {
                return ImmutablePair.of(new PrintStream(s), true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public class LoggerNotSupportedException extends RuntimeException {
        public LoggerNotSupportedException(String type) {
            super("Logger %s is not supported".formatted(type));
        }
    }
}
