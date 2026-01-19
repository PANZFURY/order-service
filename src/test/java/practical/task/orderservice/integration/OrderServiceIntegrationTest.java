package practical.task.orderservice.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import practical.task.orderservice.model.Item;
import practical.task.orderservice.repository.ItemRepository;

import com.github.tomakehurst.wiremock.client.WireMock.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class OrderServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("orders")
                    .withUsername("test")
                    .withPassword("test");

    static WireMockServer wireMockServer;

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("user.service.url", () -> wireMockServer.baseUrl());
    }

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    @Autowired
    ItemRepository itemRepository;

    @BeforeEach
    void setupMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        Item item = new Item();
        item.setName("iPhone 12");
        item.setPrice(699);

        itemRepository.save(item);

        wireMockServer.stubFor(
                WireMock.get(WireMock.urlPathMatching("/api/user/get/1"))
                        .willReturn(WireMock.aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withStatus(200)
                                .withBody("""
                                        {
                                          "id": 1,
                                          "email": "test@example.com",
                                          "firstName": "Dave",
                                          "lastName": "Horrish"
                                        }
                                        """))
        );
    }


    @Test
    void testOrderCreation() throws Exception {
        String json = """
                {
                  "userId": 1,
                  "items": [
                    { "itemId": 1, "quantity": 2 }
                  ]
                }
                """;

        mockMvc.perform(post("/api/orders/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.email").value("test@example.com"))
                .andExpect(jsonPath("$.items[0].quantity").value(2));
    }

    @Test
    void testCreateOrder_itemNotFound() throws Exception {
        String json = """
            {
              "userId": 1,
              "items": [
                { "itemId": 999, "quantity": 1 }
              ]
            }
            """;

        mockMvc.perform(post("/api/orders/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Item not found: 999"));
    }

    @Test
    void testGetOrderById() throws Exception {
        String createJson = """
    {
      "userId": 1,
      "items": [
        { "itemId": 1, "quantity": 1 }
      ]
    }
    """;

        String response = mockMvc.perform(post("/api/orders/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number orderIdNum = JsonPath.read(response, "$.id");
        long orderId = orderIdNum.longValue();

        mockMvc.perform(get("/api/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    void testGetDeletedOrder_notFound() throws Exception {
        String createJson = """
        {
            "userId": 1,
            "items": [
                {
                 "itemId": 1,
                  "quantity": 1
                }
            ]
        }
        """;

        String response = mockMvc.perform(post("/api/orders/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number orderIdNum = JsonPath.read(response, "$.id");
        long orderId = orderIdNum.longValue();

        mockMvc.perform(delete("/api/orders/" + orderId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/orders/" + orderId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Order not found"));
    }

}