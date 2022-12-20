package xktz.exam.lang.cpp;

import xktz.exam.environment.Environment;

import java.util.List;

/**
 * @author XKTZ
 * @date 2022-10-29
 */
public class ClangUtils {

    /**
     * path of clang
     */
    private static String clangDirectory = "";

    /**
     * emit llvm
     */
    private static final String EMIT_LLVM = "-emit-llvm";

    /**
     * Output
     */
    private static final String OUTPUT = "-o";

    /**
     * Get a clang executable from the clang directory
     *
     * @param command clang executable name
     * @return executable
     */
    private static String clang(String command) {
        return clangDirectory + command;
    }

    /**
     * Compile llvm bc file to object file
     */
    public static Environment.SystemOutput compileLLVMTextTo(Environment environment, String file, String output) {
        return environment.executeCommand(10000, clang("llc"), "-filetype=obj", file, OUTPUT, output);
    }
}
