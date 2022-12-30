package xktz.exam.environment;

import org.apache.commons.io.FileUtils;
import xktz.exam.environment.system.SystemType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeUnit;

/**
 * Standard environment
 *
 * @author XKTZ
 * @date 2022-10-25
 */
public class StandardEnvironment implements Environment {

    /**
     * temporary folder, needs to be used for saving in & out files
     */
    private String tmp;

    public StandardEnvironment(String tmp) {
        this.tmp = tmp;
    }

    @Override
    public SystemType system() {
        return SystemType.detectSystemType();
    }

    @Override
    public SystemOutput executeCommand(String dir, long timeLimit, byte[] input, String... commands) {
        boolean timeout = false;
        SystemOutput result = null;
        try (var param = new SystemExecutionParameters(this.tmp, input)) {
            var processBuilder = new ProcessBuilder(commands).directory(new File(dir));
            processBuilder.redirectInput(ProcessBuilder.Redirect.from(param.in));
            processBuilder.redirectOutput(ProcessBuilder.Redirect.appendTo(param.out));
            processBuilder.redirectError(ProcessBuilder.Redirect.appendTo(param.err));
            var process = processBuilder.start();
            if (!process.waitFor(timeLimit, TimeUnit.MILLISECONDS)) {
                process.destroyForcibly().waitFor();
                timeout = true;
            } else {
                process.destroyForcibly().waitFor();
                var stdout = Files.readAllBytes(param.out.toPath());
                var stderr = Files.readAllBytes(param.err.toPath());
                result = new SystemOutput(process.exitValue(), stdout, stderr);
            }
        } catch (IOException | InterruptedException e) {
            throw new EnvironmentExecutionException(e);
        }
        if (timeout) {
            throw new TimeoutException(timeLimit);
        }
        return result;
    }

    @Override
    public void executeInheritIOCommand(String... commands) {
        try {
            var processBuilder = new ProcessBuilder(commands).inheritIO();
            var process = processBuilder.start();
            process.waitFor();
        } catch (InterruptedException | IOException e) {
            throw new EnvironmentExecutionException(e);
        }
    }

    private static class SystemExecutionParameters implements AutoCloseable {
        public File in;
        public File out;
        public File err;

        public SystemExecutionParameters(String tmp, byte[] input) {
            in = new File(tmp + File.separator + ".in");
            out = new File(tmp + File.separator + ".out");
            err = new File(tmp + File.separator + ".err");
            try {
                Files.write(in.toPath(), input, StandardOpenOption.CREATE);
            } catch (IOException e) {
                throw new EnvironmentExecutionException(e);
            }
        }

        @Override
        public void close() throws IOException {
            FileUtils.delete(in);
            FileUtils.delete(out);
            FileUtils.delete(err);
        }
    }
}
