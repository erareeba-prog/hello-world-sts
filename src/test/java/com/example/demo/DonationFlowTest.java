package com.example.demo;

import com.example.demo.model.Donation;
import com.example.demo.model.Need;
import com.example.demo.model.Ngo;
import com.example.demo.repository.DonationRepository;
import com.example.demo.repository.NeedRepository;
import com.example.demo.repository.NgoRepository;
import com.example.demo.service.DonationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
public class DonationFlowTest {

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
    private DonationService donationService;

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private NeedRepository needRepository;

    @Autowired
    private NgoRepository ngoRepository;

    @Test
    void testCreateDonation_withOpenNeed_succeeds() {
        // Arrange — create NGO
        Ngo ngo = new Ngo();
        ngo.setName("Test NGO");
        ngo.setRegistrationNo("REG001");
        ngo.setDescription("Test");
        ngo.setAddress("Delhi");
        ngo.setCity("Delhi");
        ngo.setState("Delhi");
        ngo.setVerificationStatus("active");
        ngo.setIsDeleted(false);
        ngo = ngoRepository.save(ngo);

        // Create need
        Need need = new Need();
        need.setNgoId(ngo.getNgoId());
        need.setCreatedBy(ngo.getNgoId());
        need.setTitle("Food for orphans");
        need.setCategory("GROCERIES");
        need.setQtyRequired(BigDecimal.valueOf(100));
        need.setQtyFulfilled(BigDecimal.ZERO);
        need.setStatus("open");
        need.setIsDeleted(false);
        need = needRepository.save(need);

        // Create donation
        Donation donation = new Donation();
        donation.setDonorId(
            java.util.UUID.randomUUID());
        donation.setNeedId(need.getNeedId());
        donation.setAmountOrQty(
            BigDecimal.valueOf(500));
        donation.setCurrency("INR");
        donation.setType("money");

        // Act
        Object result =
            donationService.createDonation(donation);

        // Assert
        assertInstanceOf(Donation.class, result);
        Donation saved = (Donation) result;
        assertNotNull(saved.getDonationId());
        assertEquals("pending", saved.getStatus());
        assertNotNull(saved.getReceiptId());
        assertNotNull(saved.getTransactionRef());
        assertNotNull(saved.getIdempotencyKey());

        System.out.println(
            "✅ Donation created: " +
            saved.getDonationId());
    }

    @Test
    void testCreateDonation_withClosedNeed_fails() {
        // Arrange
        Ngo ngo = new Ngo();
        ngo.setName("Test NGO 2");
        ngo.setRegistrationNo("REG002");
        ngo.setDescription("Test");
        ngo.setAddress("Mumbai");
        ngo.setCity("Mumbai");
        ngo.setState("Maharashtra");
        ngo.setVerificationStatus("active");
        ngo.setIsDeleted(false);
        ngo = ngoRepository.save(ngo);

        Need need = new Need();
        need.setNgoId(ngo.getNgoId());
        need.setCreatedBy(ngo.getNgoId());
        need.setTitle("Closed need");
        need.setCategory("MEDICAL");
        need.setQtyRequired(BigDecimal.valueOf(50));
        need.setQtyFulfilled(BigDecimal.ZERO);
        need.setStatus("closed"); // ← closed!
        need.setIsDeleted(false);
        need = needRepository.save(need);

        Donation donation = new Donation();
        donation.setDonorId(
            java.util.UUID.randomUUID());
        donation.setNeedId(need.getNeedId());
        donation.setAmountOrQty(
            BigDecimal.valueOf(100));

        // Act
        Object result =
            donationService.createDonation(donation);

        // Assert
        assertEquals("NEED_NOT_OPEN", result);
        System.out.println(
            "✅ Correctly rejected closed need");
    }

    @Test
    void testCreateDonation_invalidAmount_fails() {
        Donation donation = new Donation();
        donation.setDonorId(
            java.util.UUID.randomUUID());
        donation.setNeedId(
            java.util.UUID.randomUUID());
        donation.setAmountOrQty(
            BigDecimal.ZERO); // ← zero amount

        Object result =
            donationService.createDonation(donation);

        assertEquals("INVALID_AMOUNT", result);
        System.out.println(
            "✅ Correctly rejected zero amount");
    }

    @Test
    void testDuplicateDonation_idempotencyKey_fails() {
        // Arrange
        Ngo ngo = new Ngo();
        ngo.setName("Test NGO 3");
        ngo.setRegistrationNo("REG003");
        ngo.setDescription("Test");
        ngo.setAddress("Bangalore");
        ngo.setCity("Bangalore");
        ngo.setState("Karnataka");
        ngo.setVerificationStatus("active");
        ngo.setIsDeleted(false);
        ngo = ngoRepository.save(ngo);

        Need need = new Need();
        need.setNgoId(ngo.getNgoId());
        need.setCreatedBy(ngo.getNgoId());
        need.setTitle("Duplicate test need");
        need.setCategory("STUDY");
        need.setQtyRequired(BigDecimal.valueOf(10));
        need.setQtyFulfilled(BigDecimal.ZERO);
        need.setStatus("open");
        need.setIsDeleted(false);
        need = needRepository.save(need);

        // First donation
        Donation donation1 = new Donation();
        donation1.setDonorId(
            java.util.UUID.randomUUID());
        donation1.setNeedId(need.getNeedId());
        donation1.setAmountOrQty(
            BigDecimal.valueOf(200));
        donation1.setIdempotencyKey("test-key-123");
        donationService.createDonation(donation1);

        // Duplicate donation with same key
        Donation donation2 = new Donation();
        donation2.setDonorId(
            java.util.UUID.randomUUID());
        donation2.setNeedId(need.getNeedId());
        donation2.setAmountOrQty(
            BigDecimal.valueOf(200));
        donation2.setIdempotencyKey("test-key-123");

        Object result =
            donationService.createDonation(donation2);

        assertEquals("DUPLICATE_DONATION", result);
        System.out.println(
            "✅ Correctly rejected duplicate donation");
    }
}