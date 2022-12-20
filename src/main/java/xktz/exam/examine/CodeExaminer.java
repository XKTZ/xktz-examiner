package xktz.exam.examine;

import xktz.exam.environment.Environment;
import xktz.exam.lang.LanguageRuntime;

import java.util.Arrays;
import java.util.Optional;

/**
 * Examine other's code by output either 0 or 1 in the Language Runtime's output
 * if it is 0, it means it is wrong
 * if it is 1, it means it is correct
 *
 * @author XKTZ
 * @date 2022-10-20
 */
public class CodeExaminer implements Examiner {

    private static final String SUCCESS = "1";
    private static final String FAILED = "0";

    private static final String MSG_CODE_EXAMINE_SUCCESS = "Code Examine Success";
    private static final String MSG_CODE_EXAMINE_FAILED = "Code Examine Failed";


    /**
     * Language runtime for the code
     */
    private LanguageRuntime lang;

    /**
     * @param lang language runtime
     */
    public CodeExaminer(LanguageRuntime lang) {
        this.lang = lang;
    }

    @Override
    public ExamineResult examine(byte[] input, byte[] output) {
        byte[] in = Arrays.copyOf(input, input.length + output.length);

        System.arraycopy(output, 0, in, input.length, output.length);
        var result = lang.run(in);

        var stdout = result.stdout();
        var stderr = result.stderr();

        var msg = new String(stderr, Environment.SYSTEM_CHARSET);

        if (result.exitCode() != Environment.EXIT_SUCCESS) {
            return new ExamineResult(Examiner.ERROR, msg, Optional.empty());
        }

        var ans = new String(stdout, Environment.SYSTEM_CHARSET).split("\n");
        var success = ans[0].trim().equals(SUCCESS);

        if (success) {
            return new ExamineResult(Examiner.SUCCESS, msg, Optional.empty());
        } else {
            return new ExamineResult(Examiner.FAILED, msg, Optional.empty());
        }
    }

    @Override
    public Environment.SystemOutput build() {
        return lang.compile();
    }
}
