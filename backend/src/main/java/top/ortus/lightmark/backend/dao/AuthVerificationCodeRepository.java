package top.ortus.lightmark.backend.dao;

public interface AuthVerificationCodeRepository {
    AuthVerificationCode findActiveByTargetAndChannel(String target, String channel);

    int upsert(AuthVerificationCode code);

    int consume(String id);

    int countRecentByTargetAndChannel(String target, String channel, int withinMinutes);
}
