package com.paypal.wallet.repository;

import com.paypal.wallet.model.DeviceInfo;
import com.paypal.wallet.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceInfoRepository extends JpaRepository<DeviceInfo, Long> {

    Optional<DeviceInfo> findByUserAndDeviceHash(User user, String deviceHash);

    List<DeviceInfo> findByUserOrderByLastSeenDesc(User user);

    List<DeviceInfo> findByUserAndTrustedTrue(User user);

    boolean existsByUserAndDeviceHash(User user, String deviceHash);
}
