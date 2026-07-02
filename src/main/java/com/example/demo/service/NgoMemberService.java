package com.example.demo.service;

import com.example.demo.model.NgoMember;
import com.example.demo.repository.NgoMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class NgoMemberService {

    @Autowired
    private NgoMemberRepository ngoMemberRepository;

    // ✅ Add member to NGO
    public Object addMember(NgoMember member) {

        if (member.getNgoId() == null)
            return "NGO_ID_REQUIRED";

        if (member.getUserId() == null)
            return "USER_ID_REQUIRED";

        // Validate role
        if (member.getRole() == null ||
            (!member.getRole().equals(NgoMember.ROLE_NGO_STAFF) &&
             !member.getRole().equals(NgoMember.ROLE_NGO_VOLUNTEER)))
            return "INVALID_ROLE";

        // Check if already a member
        if (ngoMemberRepository.existsByNgoIdAndUserId(
                member.getNgoId(), member.getUserId()))
            return "ALREADY_MEMBER";

        member.setJoinedAt(LocalDateTime.now());
        member.setIsActive(true);
        member.setIsDeleted(false);
        return ngoMemberRepository.save(member);
    }

    // ✅ Get all members of an NGO
    public List<NgoMember> getMembersByNgo(UUID ngoId) {
        return ngoMemberRepository
            .findByNgoIdAndIsDeletedFalse(ngoId);
    }

    // ✅ Get all NGOs a user belongs to
    public List<NgoMember> getNgosByUser(UUID userId) {
        return ngoMemberRepository
            .findByUserIdAndIsDeletedFalse(userId);
    }

    // ✅ Get members by role in NGO
    public List<NgoMember> getMembersByRole(
            UUID ngoId, String role) {
        return ngoMemberRepository
            .findByNgoIdAndRoleAndIsDeletedFalse(ngoId, role);
    }

    // ✅ Change member role
    public Object changeRole(UUID memberId,
                              String newRole,
                              UUID updatedBy) {

        Optional<NgoMember> opt =
            ngoMemberRepository.findById(memberId);
        if (opt.isEmpty()) return "NOT_FOUND";

        // Validate new role
        if (!newRole.equals(NgoMember.ROLE_NGO_STAFF) &&
            !newRole.equals(NgoMember.ROLE_NGO_VOLUNTEER))
            return "INVALID_ROLE";

        NgoMember member = opt.get();
        member.setRole(newRole);
        member.setUpdatedBy(updatedBy);
        return ngoMemberRepository.save(member);
    }

    // ✅ Remove member (soft delete)
    public Object removeMember(UUID memberId, UUID deletedBy) {
        Optional<NgoMember> opt =
            ngoMemberRepository.findById(memberId);
        if (opt.isEmpty()) return "NOT_FOUND";

        NgoMember member = opt.get();
        member.setIsDeleted(true);
        member.setIsActive(false);
        member.setDeletedAt(LocalDateTime.now());
        member.setUpdatedBy(deletedBy);
        return ngoMemberRepository.save(member);
    }

    // ✅ Check if user is NGO_STAFF in a given NGO
    public boolean isNgoStaff(UUID ngoId, UUID userId) {
        return ngoMemberRepository
            .existsByNgoIdAndUserIdAndRoleAndIsDeleted(
                ngoId, userId,
                NgoMember.ROLE_NGO_STAFF, false);
    }
}