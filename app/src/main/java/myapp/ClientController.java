package myapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class ClientController {

    private final RestTemplate restTemplate;

    // In a real K8s/Docker env, this would be an env var or service name
    private static final String DATA_SERVICE_URL = "http://localhost:8081/data";

    @Autowired
    public ClientController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @org.springframework.web.bind.annotation.CrossOrigin(origins = "http://localhost:5173") // Allow Vite default port
    @GetMapping(value = "/fetch-remote-data", produces = "application/json")
    public String fetchData() {
        // Fetching large data
        ResponseEntity<String> response = restTemplate.getForEntity(DATA_SERVICE_URL, String.class);
        return response.getBody();
    }
}
