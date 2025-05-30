package ru.tyabutov.social_network_authorization.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.tyabutov.social_network_authorization.configuration.TestSecurityConfiguration;
import ru.tyabutov.social_network_authorization.dto.RegistrationDto;
import ru.tyabutov.social_network_authorization.repository.UserRepository;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestSecurityConfiguration.class)
@ActiveProfiles("test")
class RegistrationControllerTest {

    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    static KafkaContainer kafka = new KafkaContainer(
            org.testcontainers.utility.DockerImageName.parse("confluentinc/cp-kafka:7.4.1"));

    @SuppressWarnings("resource")
    static GenericContainer<?> redis = new GenericContainer<>("redis:6.2.6")
            .withExposedPorts(6379);

    @BeforeAll
    static void startContainer() {
        postgres.start();
        kafka.start();
        redis.start();
    }

    @AfterAll
    static void stopContainer() {
        if (postgres != null) {
            postgres.stop();
            kafka.stop();
            redis.stop();
        }
    }
    @DynamicPropertySource
    public static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("app.kafka.kafkaMessageGroupId", () -> "test-group");
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
    }

    @Test
    void register_Success() throws Exception {
        RegistrationDto registrationDto = new RegistrationDto(
                "test@example.com", "password", "password", "John", "Doe", "1234"
        );

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDto))
                        .sessionAttr("captchaSecret", "1234"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Успешная регистрация"));

        // Проверяем, что пользователь создан в базе данных
        assertThat(userRepository.findByEmail("test@example.com")).isPresent();

        // Проверяем, что сообщение отправлено в Kafka
        Properties props = new Properties();
        props.put("bootstrap.servers", kafka.getBootstrapServers());
        props.put("group.id", "test-group");
        props.put("auto.offset.reset", "earliest");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        boolean messageReceived = false;
        String expectedEmail = "test@example.com";
        long endTime = System.currentTimeMillis() + 10000; // ждем до 10 секунд

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList("registration-events"));

            while (System.currentTimeMillis() < endTime && !messageReceived) {
                var records = consumer.poll(Duration.ofMillis(500));
                for (ConsumerRecord<String, String> oneRecord : records) {
                    if (oneRecord.value().contains(expectedEmail)) {
                        messageReceived = true;
                        break;
                    }
                }
            }
        }
    }
}