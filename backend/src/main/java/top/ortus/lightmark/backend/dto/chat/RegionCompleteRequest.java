package top.ortus.lightmark.backend.dto.chat;

public class RegionCompleteRequest {
    private String sessionId;
    private String regionText;
    private String surroundingText;

    public RegionCompleteRequest() {}

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getRegionText() {
        return regionText;
    }

    public void setRegionText(String regionText) {
        this.regionText = regionText;
    }

    public String getSurroundingText() {
        return surroundingText;
    }

    public void setSurroundingText(String surroundingText) {
        this.surroundingText = surroundingText;
    }
}

