package com.realestatecrm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class PropertyAttributeControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String createAttributeJson(String name, String dataType, String category, Boolean required, Boolean searchable, Integer displayOrder) throws Exception {
        ObjectMapper om = objectMapper;
        JsonNode node = om.readTree("{}");
        ((com.fasterxml.jackson.databind.node.ObjectNode) node).put("name", name);
        ((com.fasterxml.jackson.databind.node.ObjectNode) node).put("dataType", dataType);
        ((com.fasterxml.jackson.databind.node.ObjectNode) node).put("isRequired", required);
        ((com.fasterxml.jackson.databind.node.ObjectNode) node).put("isSearchable", searchable);
        ((com.fasterxml.jackson.databind.node.ObjectNode) node).put("category", category);
        if (displayOrder != null) {
            ((com.fasterxml.jackson.databind.node.ObjectNode) node).put("displayOrder", displayOrder);
        }
        return om.writeValueAsString(node);
    }

    @Test
    @DisplayName("POST as AGENT should be forbidden (role-based access)")
    @WithMockUser(username = "agent", roles = {"AGENT"})
    void createAttribute_forbiddenForNonAdmin() throws Exception {
        String body = createAttributeJson("Bedrooms", "NUMBER", "BASIC", true, true, 1);

        mockMvc.perform(post("/api/property-attributes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Full CRUD + options + reorder flow for property attributes")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void propertyAttributes_fullFlow_admin() throws Exception {
        // Create Attribute 1
        String body1 = createAttributeJson("Bedrooms", "NUMBER", "BASIC", true, true, 1);
        MvcResult createRes1 = mockMvc.perform(post("/api/property-attributes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body1))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Bedrooms"))
                .andReturn();

        JsonNode created1 = objectMapper.readTree(createRes1.getResponse().getContentAsString());
        long id1 = created1.get("id").asLong();

        // Create Attribute 2
        String body2 = createAttributeJson("Bathrooms", "NUMBER", "BASIC", false, true, 2);
        MvcResult createRes2 = mockMvc.perform(post("/api/property-attributes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body2))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Bathrooms"))
                .andReturn();
        long id2 = objectMapper.readTree(createRes2.getResponse().getContentAsString()).get("id").asLong();

        // GET all
        mockMvc.perform(get("/api/property-attributes"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$[?(@.id==" + id1 + ")]").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$[?(@.id==" + id2 + ")]").exists());

        // GET by category BASIC (as any role allowed GET). Switch to AGENT for variety
        // But keep the same security context for simplicity in this test.
        mockMvc.perform(get("/api/property-attributes/category/BASIC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("BASIC"));

        // GET by id
        mockMvc.perform(get("/api/property-attributes/" + id1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value((int) id1));

        // Update attribute 1
        String updateBody = createAttributeJson("BedroomsUpdated", "NUMBER", "BASIC", true, true, 10);
        mockMvc.perform(put("/api/property-attributes/" + id1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("BedroomsUpdated"))
                .andExpect(jsonPath("$.displayOrder").value(10));

        // Add option to attribute 1
        String optionBody = "{\"optionValue\":\"1+\",\"displayOrder\":1}";
        MvcResult optionRes = mockMvc.perform(post("/api/property-attributes/" + id1 + "/options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(optionBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.attributeId").value((int) id1))
                .andExpect(jsonPath("$.optionValue").value("1+"))
                .andReturn();
        long optionId = objectMapper.readTree(optionRes.getResponse().getContentAsString()).get("id").asLong();

        // Get options
        mockMvc.perform(get("/api/property-attributes/" + id1 + "/options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value((int) optionId))
                .andExpect(jsonPath("$[0].optionValue").value("1+"));

        // Reorder attributes for category BASIC [id2, id1]
        String reorderBody = objectMapper.writeValueAsString(
                objectMapper.readTree("{\"attributeIds\":[" + id2 + "," + id1 + "]}"));
        mockMvc.perform(put("/api/property-attributes/category/BASIC/reorder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reorderBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Attributes reordered successfully"));

        // Delete option
        mockMvc.perform(delete("/api/property-attributes/options/" + optionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Attribute option deleted successfully"));

        // Delete attributes
        mockMvc.perform(delete("/api/property-attributes/" + id1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Property attribute deleted successfully"));

        mockMvc.perform(delete("/api/property-attributes/" + id2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Property attribute deleted successfully"));
    }

    @Test
    @DisplayName("GET endpoints should be accessible to AGENT role")
    @WithMockUser(username = "agent", roles = {"AGENT"})
    void getEndpoints_allowedForAgent() throws Exception {
        // Even if empty, endpoints should return 200.
        mockMvc.perform(get("/api/property-attributes"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/property-attributes/searchable"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/property-attributes/category/BASIC"))
                .andExpect(status().isOk());
    }
}
