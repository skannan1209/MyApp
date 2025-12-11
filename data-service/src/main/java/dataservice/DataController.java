package dataservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.stream.Stream;

@RestController
public class DataController {

    private final BigItemRepository repository;
    private final ObjectMapper objectMapper;
    private final org.springframework.transaction.support.TransactionTemplate transactionTemplate;

    public DataController(BigItemRepository repository, ObjectMapper objectMapper,
            org.springframework.transaction.PlatformTransactionManager transactionManager) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.transactionTemplate = new org.springframework.transaction.support.TransactionTemplate(transactionManager);
    }

    @GetMapping("/data")
    public StreamingResponseBody getLargeData() {
        return outputStream -> {
            transactionTemplate.setReadOnly(true);
            transactionTemplate.executeWithoutResult(status -> {
                try (Stream<BigItem> itemStream = repository.streamAll()) {
                    outputStream.write('[');

                    // Use a wrapper to handle the comma separation state
                    var iterator = itemStream.iterator();
                    boolean first = true;

                    while (iterator.hasNext()) {
                        BigItem item = iterator.next();
                        if (!first) {
                            outputStream.write(',');
                        }
                        first = false;
                        outputStream.write(objectMapper.writeValueAsBytes(item));
                    }

                    outputStream.write(']');
                } catch (java.io.IOException e) {
                    throw new RuntimeException("Error writing JSON stream", e);
                }
            });
        };
    }
}
