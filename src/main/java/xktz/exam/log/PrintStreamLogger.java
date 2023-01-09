package xktz.exam.log;

import org.apache.commons.lang3.tuple.ImmutablePair;
import xktz.exam.environment.Environment;
import xktz.exam.examine.CodeExaminer;
import xktz.exam.examine.Examiner;
import xktz.exam.lang.LanguageRuntime;

import java.io.PrintStream;
import java.util.List;

/**
 * Logger logResult by printstream
 *
 * @author XKTZ
 * @date 2022-10-24
 */
public class PrintStreamLogger implements ExamLogger {
    /**
     * Print stream. first item is print or not, second one is close after logger is closed or not
     */
    private List<ImmutablePair<PrintStream, Boolean>> streams;

    public PrintStreamLogger(List<ImmutablePair<PrintStream, Boolean>> streams) {
        this.streams = streams;
    }

    @Override
    public void logStdErrMessage(String process, String error) {
        var msgProcess = "On process <%s>:".formatted(process);
        for (var stream : streams) {
            stream.getLeft().println(msgProcess);
            stream.getLeft().println(error);
        }
    }

    @Override
    public void logStart(int epoch) {
        var msg = "Epoch %d started.".formatted(epoch);
        for (var stream : streams) {
            stream.getLeft().println(msg);
        }
    }

    @Override
    public void logError(int epoch, String error) {
        var finishMsg = "Epoch %d error: %s".formatted(epoch, error);
        for (var stream : streams) {
            stream.getLeft().println(finishMsg);
            // divider
            stream.getLeft().println();
        }
    }

    @Override
    public void logResult(int epoch, Examiner.ExamineResult result) {
        var finishMsg = "Epoch %d %s: %s".formatted(
                epoch,
                result.state() == Examiner.ERROR ?
                        "error" :
                        result.state() == Examiner.SUCCESS ?
                                "success" : "failed",
                result.message());
        for (var stream : streams) {
            stream.getLeft().println(finishMsg);
            // divider
            stream.getLeft().println();
        }
    }

    @Override
    public void logStatistic(int total, int success, int failed, int error) {
        var msg = """
                Exam Result Total: \n
                Total: %d \n
                Success: %d \n
                Failed: %d \n
                Error: %d
                """.formatted(total, success, failed, error);
        for (var stream: streams) {
            stream.getLeft().println(msg);
            stream.getLeft().println();
        }
    }

    @Override
    public void close() throws Exception {
        for (var stream : streams) {
            if (stream.getRight()) {
                stream.getLeft().close();
            }
        }
    }
}
