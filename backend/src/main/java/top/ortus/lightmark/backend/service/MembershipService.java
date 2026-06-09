package top.ortus.lightmark.backend.service;

import org.springframework.stereotype.Service;
import top.ortus.lightmark.backend.config.lightmarkMembershipProperties;
import top.ortus.lightmark.backend.dto.UserDTO;
import top.ortus.lightmark.backend.dto.user.UserLevelUpgradeInfoDTO;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class MembershipService {

    private final lightmarkMembershipProperties membershipProperties;

    public MembershipService(lightmarkMembershipProperties membershipProperties) {
        this.membershipProperties = membershipProperties;
    }

    public UserLevelUpgradeInfoDTO getUpgradeInfo(UserDTO user) {
        List<lightmarkMembershipProperties.LevelRule> rules = effectiveRules();
        short currentLevel = user == null ? 0 : user.getLevel();
        int currentPoints = user == null ? 0 : user.getPoints();

        lightmarkMembershipProperties.LevelRule nextRule = rules.stream()
                .filter(r -> r.getLevel() > currentLevel)
                .min(Comparator.comparingInt(lightmarkMembershipProperties.LevelRule::getLevel))
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

    private List<lightmarkMembershipProperties.LevelRule> effectiveRules() {
        List<lightmarkMembershipProperties.LevelRule> rules = membershipProperties.getLevels();
        if (rules != null && !rules.isEmpty()) {
            return rules;
        }
        List<lightmarkMembershipProperties.LevelRule> defaults = new ArrayList<>();
        defaults.add(rule((short) 0, 0, List.of()));
        defaults.add(rule((short) 1, 100, List.of("VIP 折扣", "积分加成")));
        defaults.add(rule((short) 2, 500, List.of("更高折扣", "专属客服")));
        defaults.add(rule((short) 3, 1000, List.of("最高折扣", "专属客服", "生日礼包")));
        return defaults;
    }

    private lightmarkMembershipProperties.LevelRule rule(short level, int pointsThreshold, List<String> benefits) {
        lightmarkMembershipProperties.LevelRule rule = new lightmarkMembershipProperties.LevelRule();
        rule.setLevel(level);
        rule.setPointsThreshold(pointsThreshold);
        rule.setBenefits(benefits);
        return rule;
    }
}

