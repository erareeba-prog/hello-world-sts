package com.example.demo.service;

import com.example.demo.model.Donation;
import com.example.demo.model.Fulfillment;
import com.example.demo.model.Need;
import com.example.demo.model.Ngo;
import com.example.demo.model.NgoMember;
import com.example.demo.repository.DonationRepository;
import com.example.demo.repository.FulfillmentRepository;
import com.example.demo.repository.NeedRepository;
import com.example.demo.repository.NgoMemberRepository;
import com.example.demo.repository.NgoRepository;
import com.example.demo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class DashboardService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NgoRepository ngoRepository;

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private FulfillmentRepository fulfillmentRepository;

    @Autowired
    private NeedRepository needRepository;

    @Autowired
    private NgoMemberRepository ngoMemberRepository;

    // ✅ ADMIN DASHBOARD
    public Map<String, Object> getAdminDashboard() {

        Map<String, Object> dashboard = new HashMap<>();

        // Total users
        long totalUsers =
            userRepository.countActiveUsers();
        dashboard.put("totalUsers", totalUsers);

        // Active NGOs
        long activeNgos =
            ngoRepository.countActiveNgos();
        dashboard.put("activeNgos", activeNgos);

        // Total donation amount (confirmed)
        BigDecimal totalDonations =
            donationRepository.getTotalDonationAmount();
        dashboard.put("totalDonationAmount",
            totalDonations);

        // Fulfillment rate %
        long totalNeeds =
            needRepository.countAllNeeds();
        long fulfilledNeeds =
            needRepository.countAllFulfilledNeeds();
        double fulfillmentRate = totalNeeds > 0
            ? (fulfilledNeeds * 100.0 / totalNeeds)
            : 0.0;
        dashboard.put("fulfillmentRatePercent",
            Math.round(fulfillmentRate * 100.0) / 100.0);
        dashboard.put("totalNeeds", totalNeeds);
        dashboard.put("fulfilledNeeds", fulfilledNeeds);

        // Pending approvals count
        long pendingApprovals =
            fulfillmentRepository.countPendingApprovals();
        dashboard.put("pendingApprovalsCount",
            pendingApprovals);

        // Extra stats
        long confirmedDonations =
            donationRepository.countByStatus("confirmed");
        dashboard.put("confirmedDonationsCount",
            confirmedDonations);

        long pendingDonations =
            donationRepository.countByStatus("pending");
        dashboard.put("pendingDonationsCount",
            pendingDonations);

        return dashboard;
    }

    // ✅ NGO DASHBOARD
    public Object getNgoDashboard(UUID ngoId) {

        Optional<Ngo> ngoOpt =
            ngoRepository.findById(ngoId);
        if (ngoOpt.isEmpty()) return "NGO_NOT_FOUND";

        Ngo ngo = ngoOpt.get();
        Map<String, Object> dashboard = new HashMap<>();

        dashboard.put("ngoId", ngoId.toString());
        dashboard.put("ngoName", ngo.getName());
        dashboard.put("status",
            ngo.getVerificationStatus());

        // Total funds received
        BigDecimal totalFunds =
            donationRepository
                .getTotalFundsReceivedByNgo(ngoId);
        dashboard.put("totalFundsReceived", totalFunds);

        // Open needs count
        long openNeeds =
            needRepository.countOpenNeedsByNgo(ngoId);
        dashboard.put("openNeedsCount", openNeeds);

        // Fulfilled needs count
        long fulfilledNeeds =
            needRepository.countFulfilledNeedsByNgo(ngoId);
        dashboard.put("fulfilledNeedsCount",
            fulfilledNeeds);

        // NGO member list
        List<NgoMember> members =
            ngoMemberRepository
                .findByNgoIdAndIsDeletedFalse(ngoId);
        List<Map<String, Object>> memberList =
            new ArrayList<>();
        for (NgoMember m : members) {
            Map<String, Object> memberInfo =
                new HashMap<>();
            memberInfo.put("memberId",
                m.getMemberId().toString());
            memberInfo.put("userId",
                m.getUserId().toString());
            memberInfo.put("role", m.getRole());
            memberInfo.put("isActive", m.getIsActive());
            memberInfo.put("joinedAt",
                m.getJoinedAt() != null
                    ? m.getJoinedAt().toString() : null);
            memberList.add(memberInfo);
        }
        dashboard.put("members", memberList);
        dashboard.put("memberCount", memberList.size());

        // Top donors (anonymous-aware)
        // ✅ Fixed: safe casting from Object[]
        List<Object[]> topDonorRaw =
            donationRepository.getTopDonorsByNgo(ngoId);
        List<Map<String, Object>> topDonors =
            new ArrayList<>();
        int rank = 1;
        for (Object[] row : topDonorRaw) {
            if (rank > 5) break;
            Map<String, Object> donor = new HashMap<>();
            UUID donorId = (UUID) row[0];
            BigDecimal total = (BigDecimal) row[1];

            // ✅ Safe boolean cast
            boolean isAnon = false;
            if (row[2] instanceof Boolean) {
                isAnon = (Boolean) row[2];
            } else if (row[2] instanceof Number) {
                isAnon = ((Number) row[2])
                    .intValue() > 0;
            }

            if (isAnon) {
                donor.put("donorId", "Anonymous");
                donor.put("donorName", "Anonymous");
            } else {
                donor.put("donorId",
                    donorId.toString());
                donor.put("donorName",
                    "Donor-" + donorId.toString()
                        .substring(0, 8));
            }
            donor.put("totalAmount", total);
            donor.put("rank", rank++);
            topDonors.add(donor);
        }
        dashboard.put("topDonors", topDonors);

        // Per-need breakdown
        List<Need> needs =
            needRepository
                .findByNgoIdAndIsDeletedFalse(ngoId);
        List<Map<String, Object>> needBreakdown =
            new ArrayList<>();
        for (Need need : needs) {
            Map<String, Object> needInfo =
                new HashMap<>();
            needInfo.put("needId",
                need.getNeedId().toString());
            needInfo.put("title", need.getTitle());
            needInfo.put("category",
                need.getCategory());
            needInfo.put("status", need.getStatus());
            needInfo.put("urgency", need.getUrgency());
            needInfo.put("qtyRequired",
                need.getQtyRequired());
            needInfo.put("qtyFulfilled",
                need.getQtyFulfilled());

            double pct = 0.0;
            if (need.getQtyRequired() != null &&
                need.getQtyRequired().compareTo(
                    BigDecimal.ZERO) > 0 &&
                need.getQtyFulfilled() != null) {
                pct = need.getQtyFulfilled()
                    .divide(need.getQtyRequired(),
                        4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
            }
            needInfo.put("fulfillmentPercent",
                Math.round(pct * 100.0) / 100.0);

            List<Donation> needDonations =
                donationRepository
                    .findByNeedIdAndIsDeletedFalse(
                        need.getNeedId());
            needInfo.put("donationCount",
                needDonations.size());
            needBreakdown.add(needInfo);
        }
        dashboard.put("needBreakdown", needBreakdown);

        return dashboard;
    }

    // ✅ DONOR DASHBOARD
    public Object getDonorDashboard(UUID donorId) {

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("donorId", donorId.toString());

        // Total amount donated
        BigDecimal totalDonated =
            donationRepository
                .getTotalAmountByDonor(donorId);
        dashboard.put("totalDonated", totalDonated);

        // NGOs supported count
        long ngosSupportedCount =
            donationRepository
                .countNgosSupportedByDonor(donorId);
        dashboard.put("ngosSupportedCount",
            ngosSupportedCount);

        // Donation history
        List<Donation> donations =
            donationRepository
                .findByDonorIdAndIsDeletedFalse(donorId);
        List<Map<String, Object>> donationHistory =
            new ArrayList<>();
        List<Map<String, Object>> receipts =
            new ArrayList<>();

        for (Donation d : donations) {
            Map<String, Object> info = new HashMap<>();
            info.put("donationId",
                d.getDonationId().toString());
            info.put("needId",
                d.getNeedId().toString());
            info.put("amountOrQty",
                d.getAmountOrQty());
            info.put("currency",     d.getCurrency());
            info.put("status",       d.getStatus());
            info.put("type",         d.getType());
            info.put("isAnonymous",  d.getIsAnonymous());
            info.put("taxDeductible",
                d.getTaxDeductible());
            info.put("onBehalfOf",   d.getOnBehalfOf());
            info.put("receiptId",    d.getReceiptId());
            info.put("donatedAt",
                d.getDonatedAt() != null
                    ? d.getDonatedAt().toString()
                    : null);
            donationHistory.add(info);

            // Receipts list
            if (d.getReceiptId() != null) {
                Map<String, Object> receipt =
                    new HashMap<>();
                receipt.put("receiptId",
                    d.getReceiptId());
                receipt.put("donationId",
                    d.getDonationId().toString());
                receipt.put("amount",
                    d.getAmountOrQty());
                receipt.put("status", d.getStatus());
                receipt.put("donatedAt",
                    d.getDonatedAt() != null
                        ? d.getDonatedAt().toString()
                        : null);
                receipts.add(receipt);
            }
        }
        dashboard.put("donationHistory",
            donationHistory);
        dashboard.put("totalDonationsCount",
            donationHistory.size());
        dashboard.put("receipts", receipts);

        // Impact summary
        List<Fulfillment> confirmedFulfillments =
            fulfillmentRepository
                .findConfirmedByDonorId(donorId);
        List<Map<String, Object>> impactSummary =
            new ArrayList<>();
        for (Fulfillment f : confirmedFulfillments) {
            Map<String, Object> impact = new HashMap<>();
            impact.put("fulfillmentId",
                f.getFulfillmentId().toString());
            impact.put("needId",
                f.getNeedId().toString());
            impact.put("qtyApplied",
                f.getQtyApplied());
            impact.put("fulfilledAt",
                f.getFulfilledAt() != null
                    ? f.getFulfilledAt().toString()
                    : null);

            Optional<Need> needOpt =
                needRepository.findById(f.getNeedId());
            if (needOpt.isPresent()) {
                Need need = needOpt.get();
                impact.put("needTitle",
                    need.getTitle());
                impact.put("needCategory",
                    need.getCategory());
                impact.put("ngoId",
                    need.getNgoId().toString());

                Optional<Ngo> ngoOpt =
                    ngoRepository.findById(
                        need.getNgoId());
                if (ngoOpt.isPresent())
                    impact.put("ngoName",
                        ngoOpt.get().getName());
            }
            impactSummary.add(impact);
        }
        dashboard.put("impactSummary", impactSummary);
        dashboard.put("needsFulfilledCount",
            impactSummary.size());

        return dashboard;
    }
}