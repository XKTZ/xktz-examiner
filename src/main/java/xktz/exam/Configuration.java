package xktz.exam;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * @author XKTZ
 * @date 2022-11-28
 */
public class Configuration {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static Exam.ExaminerConfiguration getConfiguration(String file) {
        try {
            return OBJECT_MAPPER.readValue(new File(file), Exam.ExaminerConfiguration.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
