package com.example.demo.service;

import com.example.demo.model.Donation;
import com.example.demo.model.Fulfillment;
import com.example.demo.model.Need;
import com.example.demo.model.Provider;
import com.example.demo.repository.DonationRepository;
import com.example.demo.repository.FulfillmentRepository;
import com.example.demo.repository.NeedRepository;
import com.example.demo.repository.ProviderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class MatchingService {

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private NeedRepository needRepository;

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private FulfillmentRepository fulfillmentRepository;

    // ✅ Category to provider org_type mapping
    private static final Map<String, String>
        CATEGORY_TO_PROVIDER = new HashMap<>();

    static {
        CATEGORY_TO_PROVIDER.put("MEDICAL",   "HOSPITAL");
        CATEGORY_TO_PROVIDER.put("GROCERIES", "GROCERY");
        CATEGORY_TO_PROVIDER.put("STUDY",     "SCHOOL");
        // Add more mappings as needed
        CATEGORY_TO_PROVIDER.put("medical",   "HOSPITAL");
        CATEGORY_TO_PROVIDER.put("groceries", "GROCERY");
        CATEGORY_TO_PROVIDER.put("study",     "SCHOOL");
    }

    // ✅ Find eligible providers for a confirmed donation
    @Transactional
    public Object matchProviders(UUID donationId) {

        // 1. Find donation
        Optional<Donation> donOpt =
            donationRepository.findById(donationId);
        if (donOpt.isEmpty()) return "DONATION_NOT_FOUND";

        Donation donation = donOpt.get();

        // 2. Check donation is confirmed
        if (!Donation.STATUS_CONFIRMED
                .equals(donation.getStatus()))
            return "DONATION_NOT_CONFIRMED";

        // 3. Find need
        Optional<Need> needOpt =
            needRepository.findById(donation.getNeedId());
        if (needOpt.isEmpty()) return "NEED_NOT_FOUND";

        Need need = needOpt.get();

        // 4. Map category to provider type
        String category = need.getCategory();
        if (category == null)
            return "NEED_CATEGORY_MISSING";

        String providerType =
            CATEGORY_TO_PROVIDER.get(
                category.toUpperCase());

        if (providerType == null)
            return "NO_PROVIDER_TYPE_FOR_CATEGORY";

        // 5. Find eligible verified providers
        List<Provider> allProviders =
            providerRepository
                .findByIsVerifiedAndIsDeletedFalse(true);

        List<Provider> eligible = new ArrayList<>();
        for (Provider p : allProviders) {
            if (providerType.equals(p.getOrgType())) {
                eligible.add(p);
            }
        }

        if (eligible.isEmpty())
            return "NO_ELIGIBLE_PROVIDERS";

        // 6. Create PENDING fulfillment for each
        //    eligible provider
        List<Fulfillment> created = new ArrayList<>();
        for (Provider provider : eligible) {
            // Avoid duplicate fulfillments
            boolean exists = fulfillmentRepository
                .existsByDonationIdAndSelectedBy(
                    donationId, provider.getProviderId());
            if (exists) continue;

            Fulfillment f = new Fulfillment();
            f.setNeedId(need.getNeedId());
            f.setDonationId(donationId);
            f.setRecordedBy(donation.getDonorId());
            f.setQtyApplied(donation.getAmountOrQty());
            f.setNgoId(need.getNgoId());
            f.setSelectedBy(provider.getProviderId());
            f.setStatus(Fulfillment.STATUS_PENDING);
            f.setFulfilledAt(LocalDateTime.now());
            f.setNotes("Auto-matched: " +
                category + " → " + providerType);

            created.add(fulfillmentRepository.save(f));
        }

        if (created.isEmpty())
            return "FULFILLMENTS_ALREADY_EXIST";

        System.out.println(
            "✅ MATCHED " + created.size() +
            " provider(s) for donation: " + donationId +
            " | Category: " + category +
            " → Provider Type: " + providerType);

        return created;
    }

    // ✅ Get matching info without creating fulfillments
    public Object previewMatch(UUID donationId) {

        Optional<Donation> donOpt =
            donationRepository.findById(donationId);
        if (donOpt.isEmpty()) return "DONATION_NOT_FOUND";

        Donation donation = donOpt.get();

        Optional<Need> needOpt =
            needRepository.findById(donation.getNeedId());
        if (needOpt.isEmpty()) return "NEED_NOT_FOUND";

        Need need = needOpt.get();
        String category = need.getCategory();

        if (category == null)
            return "NEED_CATEGORY_MISSING";

        String providerType =
            CATEGORY_TO_PROVIDER.get(
                category.toUpperCase());

        List<Provider> allProviders =
            providerRepository
                .findByIsVerifiedAndIsDeletedFalse(true);

        List<Map<String, Object>> eligible =
            new ArrayList<>();

        for (Provider p : allProviders) {
            if (providerType != null &&
                providerType.equals(p.getOrgType())) {
                Map<String, Object> info =
                    new HashMap<>();
                info.put("providerId",
                    p.getProviderId().toString());
                info.put("orgName",   p.getOrgName());
                info.put("orgType",   p.getOrgType());
                info.put("isVerified", p.getIsVerified());
                eligible.add(info);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("donationId",   donationId.toString());
        result.put("needCategory", category);
        result.put("providerType", providerType);
        result.put("eligibleProviders", eligible);
        result.put("eligibleCount", eligible.size());
        return result;
    }
}