package com.wordtrail;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Prevent accidental dependency inversion regressions as teammates add features. */
class ArchitectureTest {
    @Test void businessLayersDoNotDependOnDeliveryOrInfrastructure() throws Exception {
        for (String layer : List.of("entity", "usecase")) {
            try (Stream<Path> paths = Files.walk(Path.of("src/main/java/com/wordtrail", layer))) {
                for (Path file : paths.filter(p -> p.toString().endsWith(".java")).collect(Collectors.toList())) {
                    String source = Files.readString(file);
                    for (String forbidden : List.of("import javax.swing", "import java.awt", "import org.json",
                            "import java.net", "import com.wordtrail.data", "import com.wordtrail.view",
                            "import com.wordtrail.adapter", "import com.wordtrail.app")) {
                        assertFalse(source.contains(forbidden), file + " depends on an outer layer: " + forbidden);
                    }
                    if (layer.equals("entity")) { assertFalse(source.contains("import com.wordtrail.usecase"), file.toString()); }
                }
            }
        }
    }

    @Test void swingViewsDependOnlyOnDisplayAdapters() throws Exception {
        try (Stream<Path> paths = Files.walk(Path.of("src/main/java/com/wordtrail/view"))) {
            for (Path file : paths.filter(p -> p.toString().endsWith(".java")).collect(Collectors.toList())) {
                String source = Files.readString(file);
                for (String forbidden : List.of("import com.wordtrail.entity", "import com.wordtrail.usecase",
                        "import com.wordtrail.data")) { assertFalse(source.contains(forbidden), file.toString()); }
            }
        }
    }
}
