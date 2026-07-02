package com.example.demo;

import com.example.demo.model.Ngo;
import com.example.demo.repository.NgoRepository;
import com.example.demo.service.NgoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
public class NgoApprovalTest {

    @Container
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(
            DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",
            postgres::getJdbcUrl);
        registry.add("spring.datasource.username",
            postgres::getUsername);
        registry.add("spring.datasource.password",
            postgres::getPassword);
        registry.add("spring.flyway.enabled",
            () -> "false");
        registry.add("spring.jpa.hibernate.ddl-auto",
            () -> "create-drop");
    }

    @Autowired
    private NgoService ngoService;

    @Autowired
    private NgoRepository ngoRepository;

    private Ngo createTestNgo(String regNo) {
        Ngo ngo = new Ngo();
        ngo.setName("Test NGO " + regNo);
        ngo.setRegistrationNo(regNo);
        ngo.setDescription("Test NGO description");
        ngo.setAddress("Test Address");
        ngo.setCity("Delhi");
        ngo.setState("Delhi");
        ngo.setVerificationStatus("pending");
        ngo.setIsDeleted(false);
        return ngoRepository.save(ngo);
    }

    @Test
    void testApproveNgo_success() {
        Ngo ngo = createTestNgo("NGO-A001");
        UUID adminId = UUID.randomUUID();

        Object result =
            ngoService.approveNgo(
                ngo.getNgoId(), adminId);

        assertInstanceOf(Ngo.class, result);
        Ngo approved = (Ngo) result;
        assertEquals("active",
            approved.getVerificationStatus());
        assertNotNull(approved.getVerifiedBy());
        assertNotNull(approved.getVerifiedAt());
        System.out.println("✅ NGO approved successfully");
    }

    @Test
    void testRejectNgo_success() {
        Ngo ngo = createTestNgo("NGO-R001");
        UUID adminId = UUID.randomUUID();
        String reason = "Incomplete documents";

        Object result =
            ngoService.rejectNgo(
                ngo.getNgoId(), adminId, reason);

        assertInstanceOf(Ngo.class, result);
        Ngo rejected = (Ngo) result;
        assertEquals("rejected",
            rejected.getVerificationStatus());
        assertEquals(reason,
            rejected.getVerificationReason());
        System.out.println("✅ NGO rejected successfully");
    }

    @Test
    void testApproveAlreadyApproved_fails() {
        Ngo ngo = createTestNgo("NGO-AA001");
        UUID adminId = UUID.randomUUID();

        ngoService.approveNgo(ngo.getNgoId(), adminId);
        Object result =
            ngoService.approveNgo(
                ngo.getNgoId(), adminId);

        assertEquals("ALREADY_APPROVED", result);
        System.out.println(
            "✅ Correctly rejected double approval");
    }

    @Test
    void testRejectWithoutReason_fails() {
        Ngo ngo = createTestNgo("NGO-NR001");
        UUID adminId = UUID.randomUUID();

        Object result =
            ngoService.rejectNgo(
                ngo.getNgoId(), adminId, "");

        assertEquals("REASON_REQUIRED", result);
        System.out.println(
            "✅ Correctly required rejection reason");
    }

    @Test
    void testApproveNonExistentNgo_fails() {
        Object result =
            ngoService.approveNgo(
                UUID.randomUUID(), UUID.randomUUID());
        assertEquals("NOT_FOUND", result);
        System.out.println(
            "✅ Correctly handled not found NGO");
    }
}