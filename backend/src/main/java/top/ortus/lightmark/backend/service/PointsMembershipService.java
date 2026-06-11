package top.ortus.lightmark.backend.service;

import org.springframework.stereotype.Service;
import top.ortus.lightmark.backend.dao.User;
import top.ortus.lightmark.backend.dao.UserRepositoryImpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
public class PointsMembershipService {

    private final UserRepositoryImpl userRepository;
    private final MembershipService membershipService;

    public PointsMembershipService(UserRepositoryImpl userRepository, MembershipService membershipService) {
        this.userRepository = userRepository;
        this.membershipService = membershipService;
    }

    public int calculateRewardPoints(BigDecimal paidAmount) {
        if (paidAmount == null || paidAmount.signum() <= 0) {
            return 0;
        }
        return paidAmount.divide(new BigDecimal("50"), 0, RoundingMode.DOWN).intValue();
    }

    public void awardPoints(String userId, String orderId, String source, BigDecimal paidAmount) {
        changePoints(userId, orderId, source, paidAmount, 1);
    }

    public void revokePoints(String userId, String orderId, String source, BigDecimal paidAmount) {
        changePoints(userId, orderId, source, paidAmount, -1);
    }

    public void refreshLevel(String userId) {
        User user = userRepository.findById(userId);
        user.setLevel(membershipService.resolveLevelByPoints(user.getPoints()));
        user.setUpdate_time(LocalDateTime.now());
        userRepository.update(user);
    }

    private void changePoints(String userId, String orderId, String source, BigDecimal paidAmount, int direction) {
        int basePoints = calculateRewardPoints(paidAmount);
        if (basePoints <= 0) {
            return;
        }
        User user = userRepository.findById(userId);
        int delta = basePoints * direction;
        int nextPoints = Math.max(0, user.getPoints() + delta);
        user.setPoints(nextPoints);
        user.setLevel(membershipService.resolveLevelByPoints(nextPoints));
        user.setUpdate_time(LocalDateTime.now());
        userRepository.update(user);
        userRepository.insertPointsLog(userId, direction > 0 ? 1 : 2, delta, source, orderId);
    }
}
