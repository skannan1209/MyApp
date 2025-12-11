package dataservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.Map;

@RestController
public class DataController {

    private final ObjectMapper objectMapper;

    public DataController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @GetMapping("/data")
    public StreamingResponseBody getLargeData() {
        return outputStream -> {
            outputStream.write('[');
            // Generating ~10MB of data
            // 50,000 items * approx 200 bytes = 10 MB
            for (int i = 0; i < 50000; i++) {
                Map<String, Object> data = Map.of(
                        "id", i,
                        "name", "Item " + i,
                        "description", "This is a detailed description for item " + i
                                + " to ensure the payload size is significant. " + "x".repeat(100));

                outputStream.write(objectMapper.writeValueAsBytes(data));
                if (i < 49999) {
                    outputStream.write(',');
                }

                // Flush occasionally to avoid holding everything in buffers if needed,
                // though StreamingResponseBody outputStream is usually buffered.
                if (i % 1000 == 0) {
                    outputStream.flush();
                }
            }
            outputStream.write(']');
        };
    }
}
