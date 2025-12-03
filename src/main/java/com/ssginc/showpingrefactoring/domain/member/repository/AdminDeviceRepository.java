package com.ssginc.showpingrefactoring.domain.member.repository;

import com.ssginc.showpingrefactoring.domain.member.entity.AdminDevice;
import com.ssginc.showpingrefactoring.domain.member.entity.AdminDeviceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface AdminDeviceRepository extends JpaRepository<AdminDevice, UUID> {
    List<AdminDevice> findAllByMember_MemberNoAndStatus(Long memberNo, AdminDeviceStatus status);
    Optional<AdminDevice> findByCredentialId(byte[] credentialId);
    boolean existsByIdAndMember_MemberNoAndStatus(UUID id, Long memberNo, AdminDeviceStatus status);
    boolean existsByMember_MemberNoAndStatus(Long memberNo, AdminDeviceStatus status);
}
