import xktz.exam.Exam;
import xktz.exam.log.ExamLogger;

import java.util.List;
import java.util.Map;

/**
 * @author XKTZ
 * @date 2022-10-25
 */
public class Test {

    public static void main(String[] args) throws Exception {
        Exam exam = new Exam("d://test", new Exam.ExaminerConfiguration(
                1,
                new ExamLogger.LogConfiguration("log", "print", Map.of(
                        "to", List.of("stdout")
                )),
                Map.of(
                        "lang", "c",
                        "compiler", "gcc",
                        "dir", "project",
                        "files", List.of("main.c"),
                        "out", "main"
                ),
                Map.of(
                        "lang", "c++",
                        "dir", "test",
                        "files", List.of("main.cpp"),
                        "out", "testgen"
                ),
                Map.of(
                        "type", "compare",
                        "mode", "token",
                        "runner", Map.of(
                                "lang", "c++",
                                "dir", "exam",
                                "files", List.of("main.cpp"),
                                "out", "examine"
                        )
                ),
                "byte"
        ));
        exam.build(true, true, true);
        exam.examine();
        // JavaRuntime javaRuntime = new JavaRuntime("D:\\main", ".", new StandardEnvironment("D:\\main"),
        //         Map.of(
        //                 "src", "src",
        //                 "main", "main.Test",
        //                 "out", "target",
        //                 "timeLimit", 3000
        //         ));
        // javaRuntime.compile();
        // var oup = javaRuntime.examine(new byte[0]);
        // System.out.println(new String(oup.stdout()));
    }
}
