package com.example.demo.service;

import com.example.demo.model.Donation;
import com.example.demo.model.Fulfillment;
import com.example.demo.model.Need;
import com.example.demo.model.NgoMember;
import com.example.demo.repository.DonationRepository;
import com.example.demo.repository.FulfillmentRepository;
import com.example.demo.repository.NeedRepository;
import com.example.demo.repository.NgoMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FulfillmentService {

    @Autowired
    private FulfillmentRepository fulfillmentRepository;

    @Autowired
    private NgoMemberRepository ngoMemberRepository;

    @Autowired
    private NeedRepository needRepository;

    @Autowired
    private DonationRepository donationRepository;

    public List<Fulfillment> getAllFulfillments() {
        return fulfillmentRepository.findAll();
    }

    public Optional<Fulfillment> getFulfillmentById(
            UUID id) {
        return fulfillmentRepository.findById(id);
    }

    public Fulfillment createFulfillment(
            Fulfillment fulfillment) {
        fulfillment.setFulfilledAt(LocalDateTime.now());
        fulfillment.setStatus(Fulfillment.STATUS_PENDING);
        return fulfillmentRepository.save(fulfillment);
    }

    public Object updateFulfillment(UUID id,
                                     Fulfillment updated) {
        Optional<Fulfillment> opt =
            fulfillmentRepository.findById(id);
        if (opt.isEmpty()) return "NOT_FOUND";

        Fulfillment f = opt.get();
        if (updated.getQtyApplied() != null)
            f.setQtyApplied(updated.getQtyApplied());
        if (updated.getProofUrl() != null)
            f.setProofUrl(updated.getProofUrl());
        if (updated.getNotes() != null)
            f.setNotes(updated.getNotes());
        f.setUpdatedAt(LocalDateTime.now());
        return fulfillmentRepository.save(f);
    }

    public Object deleteFulfillment(UUID id) {
        if (!fulfillmentRepository.existsById(id))
            return "NOT_FOUND";
        fulfillmentRepository.deleteById(id);
        return "DELETED";
    }

    public List<Fulfillment> getFulfillmentsByNeed(
            UUID needId) {
        return fulfillmentRepository.findByNeedId(needId);
    }

    // ✅ CONFIRM fulfillment — NGO_STAFF only
    // Updates need.qty_fulfilled and donation status
    @Transactional
    public Object confirmFulfillment(UUID fulfillmentId,
                                      UUID staffId,
                                      UUID providerId) {
        // 1. Find fulfillment
        Optional<Fulfillment> opt =
            fulfillmentRepository.findById(fulfillmentId);
        if (opt.isEmpty()) return "NOT_FOUND";

        Fulfillment f = opt.get();

        // 2. Check NGO_STAFF role
        if (f.getNgoId() == null)
            return "NGO_ID_MISSING";

        boolean isStaff = ngoMemberRepository
            .existsByNgoIdAndUserIdAndRoleAndIsDeleted(
                f.getNgoId(), staffId,
                NgoMember.ROLE_NGO_STAFF, false);

        if (!isStaff) return "NOT_NGO_STAFF";

        // 3. Check not already confirmed
        if (Fulfillment.STATUS_CONFIRMED
                .equals(f.getStatus()))
            return "ALREADY_CONFIRMED";

        // 4. Set confirmed fields
        f.setStatus(Fulfillment.STATUS_CONFIRMED);
        f.setFulfilledAt(LocalDateTime.now());
        f.setSelectedBy(providerId);
        f.setUpdatedAt(LocalDateTime.now());
        f.setUpdatedBy(staffId);

        fulfillmentRepository.save(f);

        // 5. Update need.qty_fulfilled += qty_applied
        //    (optimistic lock via @Version)
        Optional<Need> needOpt =
            needRepository.findById(f.getNeedId());
        if (needOpt.isPresent()) {
            Need need = needOpt.get();
            BigDecimal current =
                need.getQtyFulfilled() != null
                    ? need.getQtyFulfilled()
                    : BigDecimal.ZERO;
            need.setQtyFulfilled(
                current.add(f.getQtyApplied()));
            need.setUpdatedAt(LocalDateTime.now());

            // Auto-close need if fully fulfilled
            if (need.getQtyFulfilled().compareTo(
                    need.getQtyRequired()) >= 0) {
                need.setStatus("closed");
                System.out.println(
                    "✅ Need fully fulfilled — " +
                    "status set to CLOSED: " +
                    need.getNeedId());
            }

            needRepository.save(need);
        }

        // 6. Update donation status to CONFIRMED
        Optional<Donation> donOpt =
            donationRepository.findById(f.getDonationId());
        if (donOpt.isPresent()) {
            Donation donation = donOpt.get();
            donation.setStatus(
                Donation.STATUS_CONFIRMED);
            donation.setConfirmedAt(LocalDateTime.now());
            donationRepository.save(donation);
        }

        System.out.println(
            "✅ FULFILLMENT CONFIRMED: " + fulfillmentId +
            " | Provider: " + providerId +
            " | Staff: " + staffId);

        return f;
    }

    // ✅ Approve fulfillment — NGO_STAFF only
    public Object approveFulfillment(UUID fulfillmentId,
                                      UUID approverId) {
        Optional<Fulfillment> opt =
            fulfillmentRepository.findById(fulfillmentId);
        if (opt.isEmpty()) return "NOT_FOUND";

        Fulfillment f = opt.get();

        if (f.getNgoId() == null) return "NGO_ID_MISSING";

        boolean isStaff = ngoMemberRepository
            .existsByNgoIdAndUserIdAndRoleAndIsDeleted(
                f.getNgoId(), approverId,
                NgoMember.ROLE_NGO_STAFF, false);

        if (!isStaff) return "NOT_NGO_STAFF";

        if (Fulfillment.STATUS_APPROVED
                .equals(f.getStatus()))
            return "ALREADY_APPROVED";

        f.setStatus(Fulfillment.STATUS_APPROVED);
        f.setApprovedBy(approverId);
        f.setApprovedAt(LocalDateTime.now());
        f.setUpdatedAt(LocalDateTime.now());
        f.setUpdatedBy(approverId);

        return fulfillmentRepository.save(f);
    }

    // ✅ Reject fulfillment — NGO_STAFF only
    public Object rejectFulfillment(UUID fulfillmentId,
                                     UUID rejectorId,
                                     String reason) {
        Optional<Fulfillment> opt =
            fulfillmentRepository.findById(fulfillmentId);
        if (opt.isEmpty()) return "NOT_FOUND";

        Fulfillment f = opt.get();

        if (f.getNgoId() == null) return "NGO_ID_MISSING";

        boolean isStaff = ngoMemberRepository
            .existsByNgoIdAndUserIdAndRoleAndIsDeleted(
                f.getNgoId(), rejectorId,
                NgoMember.ROLE_NGO_STAFF, false);

        if (!isStaff) return "NOT_NGO_STAFF";

        if (Fulfillment.STATUS_REJECTED
                .equals(f.getStatus()))
            return "ALREADY_REJECTED";

        f.setStatus(Fulfillment.STATUS_REJECTED);
        f.setRejectionReason(reason);
        f.setUpdatedAt(LocalDateTime.now());
        f.setUpdatedBy(rejectorId);

        return fulfillmentRepository.save(f);
    }
}