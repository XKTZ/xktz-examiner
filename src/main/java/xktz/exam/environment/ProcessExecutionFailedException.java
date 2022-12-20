package xktz.exam.environment;

/**
 * @author XKTZ
 * @date 2022-10-28
 */
public class ProcessExecutionFailedException extends RuntimeException {
    public ProcessExecutionFailedException(String msg) {
        super(msg);
    }
}