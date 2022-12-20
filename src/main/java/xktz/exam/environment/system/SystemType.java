package xktz.exam.environment.system;

import org.apache.commons.lang3.SystemUtils;

/**
 * The system types
 *
 * @author XKTZ
 * @date 2022-10-18
 */
public interface SystemType {

    /**
     * Detect a system type by built-in method
     *
     * @return the system type
     */
    static SystemType detectSystemType() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return BasicSystemTypes.WINDOWS;
        } else if (SystemUtils.IS_OS_LINUX) {
            return BasicSystemTypes.LINUX;
        }
        throw new SystemTypeNotSupportedException();
    }

    /**
     * Get the extension of executable under system
     *
     * @return executable extension
     */
    static String executableExtension() {
        var type = detectSystemType();
        if (type == BasicSystemTypes.WINDOWS) {
            return ".exe";
        } else if (type == BasicSystemTypes.LINUX) {
            return "";
        } else {
            return "";
        }
    }

    /**
     * Exception that this system type is not supported for the application
     */
    class SystemTypeNotSupportedException extends RuntimeException {
        public SystemTypeNotSupportedException() {
            super("System type <%s> is not supported".formatted(System.getProperty("os.name")));
        }
    }
}
