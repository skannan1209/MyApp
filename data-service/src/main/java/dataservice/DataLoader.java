package dataservice;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initDatabase(BigItemRepository repository) {
        return args -> {
            System.out.println("Initializing H2 Database with 50,000 records...");
            long start = System.currentTimeMillis();

            List<BigItem> batch = new ArrayList<>(1000);
            String filler = "x".repeat(300);

            for (int i = 0; i < 50000; i++) {
                batch.add(new BigItem("Item " + i,
                        "This is a detailed description for item " + i + " from H2 DB. " + filler));

                if (batch.size() >= 1000) {
                    repository.saveAll(batch);
                    batch.clear();
                }
            }
            if (!batch.isEmpty()) {
                repository.saveAll(batch);
            }

            System.out.println("Database initialization completed in " + (System.currentTimeMillis() - start) + "ms");
        };
    }
}
