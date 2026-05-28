package top.ortus.timemark.backend.service;

import org.springframework.stereotype.Service;
import top.ortus.timemark.backend.config.TimemarkMembershipProperties;
import top.ortus.timemark.backend.dto.UserDTO;
import top.ortus.timemark.backend.dto.user.UserLevelUpgradeInfoDTO;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class MembershipService {

    private final TimemarkMembershipProperties membershipProperties;

    public MembershipService(TimemarkMembershipProperties membershipProperties) {
        this.membershipProperties = membershipProperties;
    }

    public UserLevelUpgradeInfoDTO getUpgradeInfo(UserDTO user) {
        List<TimemarkMembershipProperties.LevelRule> rules = effectiveRules();
        short currentLevel = user == null ? 0 : user.getLevel();
        int currentPoints = user == null ? 0 : user.getPoints();

        TimemarkMembershipProperties.LevelRule nextRule = rules.stream()
                .filter(r -> r.getLevel() > currentLevel)
                .min(Comparator.comparingInt(TimemarkMembershipProperties.LevelRule::getLevel))
                .orElse(null);

        if (nextRule == null) {
            return new UserLevelUpgradeInfoDTO(currentLevel, 0, List.of());
        }

        int pointsNeeded = nextRule.getPointsThreshold() - currentPoints;
        if (pointsNeeded < 0) {
            pointsNeeded = 0;
        }
        return new UserLevelUpgradeInfoDTO(currentLevel, pointsNeeded, nextRule.getBenefits());
    }

    private List<TimemarkMembershipProperties.LevelRule> effectiveRules() {
        List<TimemarkMembershipProperties.LevelRule> rules = membershipProperties.getLevels();
        if (rules != null && !rules.isEmpty()) {
            return rules;
        }
        List<TimemarkMembershipProperties.LevelRule> defaults = new ArrayList<>();
        defaults.add(rule((short) 0, 0, List.of()));
        defaults.add(rule((short) 1, 100, List.of("VIP 折扣", "积分加成")));
        defaults.add(rule((short) 2, 500, List.of("更高折扣", "专属客服")));
        defaults.add(rule((short) 3, 1000, List.of("最高折扣", "专属客服", "生日礼包")));
        return defaults;
    }

    private TimemarkMembershipProperties.LevelRule rule(short level, int pointsThreshold, List<String> benefits) {
        TimemarkMembershipProperties.LevelRule rule = new TimemarkMembershipProperties.LevelRule();
        rule.setLevel(level);
        rule.setPointsThreshold(pointsThreshold);
        rule.setBenefits(benefits);
        return rule;
    }
}

