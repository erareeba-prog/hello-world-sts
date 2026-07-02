package com.example.demo.service;

import com.example.demo.model.Donation;
import com.example.demo.model.Need;
import com.example.demo.model.Ngo;
import com.example.demo.model.Transaction;
import com.example.demo.repository.DonationRepository;
import com.example.demo.repository.NeedRepository;
import com.example.demo.repository.NgoRepository;
import com.example.demo.repository.TransactionRepository;
import com.example.demo.UserRepository;
import com.example.demo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class DonationService {

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private NeedRepository needRepository;

    @Autowired
    private NgoRepository ngoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    public List<Donation> getAllDonations() {
        return donationRepository.findByIsDeletedFalse();
    }

    public Optional<Donation> getDonationById(UUID id) {
        return donationRepository.findById(id);
    }

    private String generateTransactionId() {
        String uuid = UUID.randomUUID()
            .toString().replace("-", "").substring(0, 12)
            .toUpperCase();
        String timestamp = String.valueOf(
            System.currentTimeMillis());
        return "TXN_" + uuid + "_" + timestamp;
    }

    private String generateReceiptId() {
        String timestamp = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID()
            .toString().replace("-", "").substring(0, 8)
            .toUpperCase();
        return "RCPT_" + timestamp + "_" + uuid;
    }

    @Transactional
    public Object createDonation(Donation donation) {

        // 1. Validate amount > 0
        if (donation.getAmountOrQty() == null ||
            donation.getAmountOrQty()
                .compareTo(BigDecimal.ZERO) <= 0)
            return "INVALID_AMOUNT";

        // 2. Validate donor ID present
        if (donation.getDonorId() == null)
            return "DONOR_ID_REQUIRED";

        // 3. Validate need ID present
        if (donation.getNeedId() == null)
            return "NEED_ID_REQUIRED";

        // 4. Validate need exists and is OPEN
        Optional<Need> needOpt =
            needRepository.findById(donation.getNeedId());
        if (needOpt.isEmpty())
            return "NEED_NOT_FOUND";

        Need need = needOpt.get();

        if (!"open".equals(need.getStatus()))
            return "NEED_NOT_OPEN";

        if (Boolean.TRUE.equals(need.getIsDeleted()))
            return "NEED_NOT_FOUND";

        // 5. Check idempotency key
        if (donation.getIdempotencyKey() != null &&
            donationRepository.existsByIdempotencyKey(
                donation.getIdempotencyKey()))
            return "DUPLICATE_DONATION";

        // 6. Generate idempotency key if not provided
        if (donation.getIdempotencyKey() == null ||
            donation.getIdempotencyKey().isEmpty()) {
            donation.setIdempotencyKey(
                "don_" + donation.getDonorId() + "_"
                + donation.getNeedId() + "_"
                + System.currentTimeMillis());
        }

        // 7. Default flags if not set
        if (donation.getIsAnonymous() == null)
            donation.setIsAnonymous(false);
        if (donation.getTaxDeductible() == null)
            donation.setTaxDeductible(false);

        // 8. Generate IDs
        String transactionId = generateTransactionId();
        String receiptId = generateReceiptId();

        // 9. Set donation fields
        donation.setStatus(Donation.STATUS_PENDING);
        donation.setDonatedAt(LocalDateTime.now());
        donation.setIsDeleted(false);
        donation.setTransactionRef(transactionId);
        donation.setReceiptId(receiptId);
        donation.setCurrency(
            donation.getCurrency() != null
                ? donation.getCurrency() : "INR");

        // 10. Save donation
        Donation saved = donationRepository.save(donation);

        // 11. ✅ Fetch donor name safely
        // donorId in Donation is UUID but User.id is Long
        // so we use a display string instead
        String donorName = "Anonymous";
        if (!Boolean.TRUE.equals(saved.getIsAnonymous())) {
            donorName = "Donor-" +
                saved.getDonorId().toString()
                    .substring(0, 8);
        }

        // 12. Fetch NGO name
        String ngoName = "Unknown NGO";
        UUID ngoId = need.getNgoId();
        Optional<Ngo> ngoOpt =
            ngoRepository.findById(ngoId);
        if (ngoOpt.isPresent())
            ngoName = ngoOpt.get().getName();

        // 13. Create transaction record
        Transaction txn = new Transaction();
        txn.setTransactionId(transactionId);
        txn.setDonationId(saved.getDonationId());
        txn.setDonorId(saved.getDonorId());
        txn.setNeedId(saved.getNeedId());
        txn.setNgoId(ngoId);
        txn.setDonorName(donorName);
        txn.setNgoName(ngoName);
        txn.setNeedTitle(need.getTitle());
        txn.setAmountOrQty(saved.getAmountOrQty());
        txn.setCurrency(saved.getCurrency());
        txn.setType(saved.getType());
        txn.setStatus(saved.getStatus());
        txn.setReceiptId(receiptId);
        txn.setIsAnonymous(saved.getIsAnonymous());
        txn.setTaxDeductible(saved.getTaxDeductible());
        txn.setOnBehalfOf(saved.getOnBehalfOf());
        txn.setCreatedAt(LocalDateTime.now());

        transactionRepository.save(txn);

        System.out.println(
            "✅ TRANSACTION CREATED: " + transactionId +
            " | RECEIPT: " + receiptId +
            " | ANONYMOUS: " + saved.getIsAnonymous() +
            " | TAX DEDUCTIBLE: "
            + saved.getTaxDeductible());

        return saved;
    }

    public Object getReceipt(UUID donationId) {
        Optional<Donation> donOpt =
            donationRepository.findById(donationId);
        if (donOpt.isEmpty()) return "NOT_FOUND";

        Optional<Transaction> txnOpt =
            transactionRepository
                .findByDonationId(donationId);
        if (txnOpt.isEmpty()) return "RECEIPT_NOT_FOUND";

        Transaction txn = txnOpt.get();

        String displayDonorName =
            Boolean.TRUE.equals(txn.getIsAnonymous())
                ? "Anonymous" : txn.getDonorName();

        String onBehalfOf =
            txn.getOnBehalfOf() != null &&
            !txn.getOnBehalfOf().isEmpty()
                ? txn.getOnBehalfOf() : "N/A";

        Map<String, Object> receipt = new HashMap<>();
        receipt.put("receiptId",
            txn.getReceiptId());
        receipt.put("transactionId",
            txn.getTransactionId());
        receipt.put("donationId",
            donationId.toString());
        receipt.put("donorName",     displayDonorName);
        receipt.put("ngoName",       txn.getNgoName());
        receipt.put("needTitle",     txn.getNeedTitle());
        receipt.put("amountOrQty",   txn.getAmountOrQty());
        receipt.put("currency",      txn.getCurrency());
        receipt.put("status",        txn.getStatus());
        receipt.put("isAnonymous",   txn.getIsAnonymous());
        receipt.put("taxDeductible", txn.getTaxDeductible());
        receipt.put("onBehalfOf",    onBehalfOf);
        receipt.put("date",
            txn.getCreatedAt().toString());
        return receipt;
    }

    public Object getDonationForNgo(UUID donationId) {
        Optional<Donation> opt =
            donationRepository.findById(donationId);
        if (opt.isEmpty()) return "NOT_FOUND";

        Donation d = opt.get();

        String donorDisplay =
            Boolean.TRUE.equals(d.getIsAnonymous())
                ? "Anonymous"
                : d.getDonorId().toString();

        Map<String, Object> view = new HashMap<>();
        view.put("donationId",
            d.getDonationId().toString());
        view.put("donorId",       donorDisplay);
        view.put("needId",
            d.getNeedId().toString());
        view.put("amountOrQty",   d.getAmountOrQty());
        view.put("currency",      d.getCurrency());
        view.put("status",        d.getStatus());
        view.put("isAnonymous",   d.getIsAnonymous());
        view.put("taxDeductible", d.getTaxDeductible());
        view.put("onBehalfOf",
            d.getOnBehalfOf() != null
                ? d.getOnBehalfOf() : "N/A");
        view.put("donatedAt",
            d.getDonatedAt().toString());
        return view;
    }

    public Object updateDonation(UUID id,
                                  Donation updated) {
        Optional<Donation> opt =
            donationRepository.findById(id);
        if (opt.isEmpty()) return "NOT_FOUND";

        Donation donation = opt.get();
        if (updated.getStatus() != null)
            donation.setStatus(updated.getStatus());
        if (updated.getTransactionRef() != null)
            donation.setTransactionRef(
                updated.getTransactionRef());
        if (updated.getCancellationReason() != null)
            donation.setCancellationReason(
                updated.getCancellationReason());
        donation.setUpdatedAt(LocalDateTime.now());
        return donationRepository.save(donation);
    }

    public Object deleteDonation(UUID id,
                                  UUID deletedBy) {
        Optional<Donation> opt =
            donationRepository.findById(id);
        if (opt.isEmpty()) return "NOT_FOUND";

        Donation donation = opt.get();
        donation.setIsDeleted(true);
        donation.setDeletedBy(deletedBy);
        donation.setDeletedAt(LocalDateTime.now());
        return donationRepository.save(donation);
    }

    public List<Donation> getDonationsByDonor(
            UUID donorId) {
        return donationRepository
            .findByDonorIdAndIsDeletedFalse(donorId);
    }

    public List<Donation> getDonationsByNeed(
            UUID needId) {
        return donationRepository
            .findByNeedIdAndIsDeletedFalse(needId);
    }
}