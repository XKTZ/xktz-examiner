package xktz.exam;

import com.fasterxml.jackson.databind.ObjectMapper;
import xktz.exam.examine.ExaminerProvider;
import xktz.exam.lang.LanguageRuntimeProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Command line
 *
 * @author XKTZ
 * @date 2022-11-28
 */
public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            // test the native image
            System.out.printf(Configuration.getConfiguration(Main.class.getResourceAsStream("/config.json")).toString());
            return;
        }
        var command = args[0];
        final var properties = new Properties();

        for (int i = 1; i < args.length; i++) {
            var a = Arrays.stream(args[i].split("=")).map(String::trim).toArray(String[]::new);
            properties.put(a[0], a[1]);
        }

        BiConsumer<String, Consumer<String>> consumeIfPresent = (key, consumer) -> {
            if (properties.containsKey(key)) {
                consumer.accept(properties.getProperty(key));
            }
        };

        if (command.equals("init")) {
            Map<String, Object> config = new HashMap<>();
            config.put("epoch", 10);
            config.put("charset", "byte");

            // Initialize the project in the folder
            consumeIfPresent.accept("epoch", (epoch) -> config.put("epoch", Integer.parseInt(epoch)));
            consumeIfPresent.accept("project", (lang) -> {
                config.put("project", LanguageRuntimeProvider.initConfiguration("project", lang));
            });
            consumeIfPresent.accept("generator", (lang) -> {
                config.put("generator", LanguageRuntimeProvider.initConfiguration("generator", lang));
            });
            consumeIfPresent.accept("examiner", (lang) -> {
                config.put("examiner", ExaminerProvider.initConfiguration(lang));
            });
            var writer = new ObjectMapper()
                    .writerWithDefaultPrettyPrinter();
            writer.writeValue(new File("config.json"), config);
            return;
        }

        var conf = Configuration.getConfiguration("config.json");
        var exam = new Exam(System.getProperty("user.dir"), conf);

        switch (command) {
            case "run" -> {
                var data = (properties.containsKey("input") && Files.exists(Path.of(properties.getProperty("input")))) ?
                        Files.readAllBytes(Path.of(properties.getProperty("input"))) : new byte[0];
                exam.build(true, false, false);
                exam.run(data);
            }
            case "build" -> {
                exam.build(true, true, true);
            }
            case "exam" -> {
                exam.examine();
            }
            case "bexam" -> {
                exam.build(true, true, true);
                exam.examine();
            }
            default -> {
            }
        }
    }
}
