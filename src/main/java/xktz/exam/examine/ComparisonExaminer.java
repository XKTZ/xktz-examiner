package xktz.exam.examine;

import xktz.exam.environment.Environment;
import xktz.exam.lang.LanguageRuntime;

import java.util.Arrays;
import java.util.Optional;
import java.util.Scanner;

/**
 * Examine a code by others' code's output
 * After running the examiner
 *
 * @author XKTZ
 * @date 2022-10-20
 */
public class ComparisonExaminer implements Examiner {

    private static final String SUCCESS = "success";

    private static final String FAILED = "failed";

    /**
     * Language runtime for the examiner
     */
    private final LanguageRuntime lang;

    /**
     * Ignore ending space or nto
     */
    private final ExaminerMode mode;

    public ComparisonExaminer(ExaminerMode mode, LanguageRuntime lang) {
        this.lang = lang;
        this.mode = mode;
    }

    @Override
    public ExamineResult examine(byte[] input, byte[] output) {
        var correctResult = lang.run(input);
        if (correctResult.exitCode() != Environment.EXIT_SUCCESS) {
            return new ExamineResult(
                    Examiner.ERROR,
                    "Checker error: %s".formatted(new String(correctResult.stderr(), Environment.SYSTEM_CHARSET)),
                    Optional.empty()
            );
        }
        var correct = correctResult.stdout();
        if (mode == ExaminerMode.STRICT) {
            return compareByStrict(output, correct);
        } else if (mode == ExaminerMode.TRIM) {
            return compareByTrim(output, correct);
        } else if (mode == ExaminerMode.TOKEN) {
            return compareByToken(output, correct);
        }
        return new ExamineResult(Examiner.FAILED, "Mode %s Not Found".formatted(mode.toString()), Optional.empty());
    }

    @Override
    public Environment.SystemOutput build() {
        return lang.compile();
    }

    /**
     * Compare two byte arrays exact by their values
     *
     * @param a byte array a
     * @param b byte array b
     * @return
     */
    private ExamineResult compareByStrict(byte[] a, byte[] b) {
        var success = Arrays.compare(a, b) == 0;
        return new ExamineResult(success ? Examiner.SUCCESS : Examiner.FAILED,
                "Strict Mode Comparison: " + messageBySuccess(success), Optional.of(b));
    }

    /**
     * Compare two byte arrays by converting them to string, then split by line, then examine after trim each line
     *
     * @param a a
     * @param b b
     * @return
     */
    private ExamineResult compareByTrim(byte[] a, byte[] b) {
        String[] x = new String(a, Environment.SYSTEM_CHARSET).split("\n");
        String[] y = new String(b, Environment.SYSTEM_CHARSET).split("\n");
        if (x.length != y.length) {
            return new ExamineResult(Examiner.FAILED, "Trim Mode Comparison: length get output not same", Optional.of(b));
        }
        int len = x.length;
        for (int i = 0; i < len; i++) {
            if (!x[i].trim().equals(y[i].trim())) {
                return new ExamineResult(Examiner.FAILED,
                        "Trim Mode Comparison: '%s' != '%s'".formatted(x[i].trim(), y[i].trim()),
                        Optional.of(b));
            }
        }
        return new ExamineResult(Examiner.SUCCESS, "Trim Mode Comparison: " + messageBySuccess(true), Optional.of(b));
    }

    private static String messageBySuccess(boolean success) {
        return success ? SUCCESS : FAILED;
    }

    /**
     * Compare two byte array by converting them into string, then into each token (word), comparing the words
     *
     * @param a a
     * @param b b
     * @return
     */
    private static ExamineResult compareByToken(byte[] a, byte[] b) {
        Scanner scannerA = new Scanner(new String(a, Environment.SYSTEM_CHARSET)),
                scannerB = new Scanner(new String(b, Environment.SYSTEM_CHARSET));
        while (scannerA.hasNext()) {
            if (!scannerB.hasNext()) {
                return new ExamineResult(Examiner.FAILED, "Token Comparison: input too long", Optional.of(b));
            }
            var x = scannerA.next();
            var y = scannerB.next();
            if (!x.equals(y)) {
                return new ExamineResult(Examiner.FAILED, "Token Comparison: %s is not %s".formatted(x, y), Optional.of(b));
            }
        }
        if (!scannerB.hasNext()) {
            return new ExamineResult(Examiner.SUCCESS, "Token Comparison: " + messageBySuccess(true), Optional.of(b));
        } else {
            return new ExamineResult(Examiner.FAILED, "Token Comparison: " + messageBySuccess(false), Optional.of(b));
        }
    }

    public enum ExaminerMode {
        STRICT,
        TRIM,
        TOKEN;
    }
}
