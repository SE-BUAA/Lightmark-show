package top.ortus.lightmark.backend.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.ortus.lightmark.backend.dao.User;
import top.ortus.lightmark.backend.dao.UserRepository;

import java.util.HashMap;
import java.util.Map;

@Component
public class UpdateNicknameTool {

    private final UserRepository userRepository;

    @Autowired
    public UpdateNicknameTool(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Tool(name = "update_nickname", description = "Update the user's nickname by user id.")
    public Map<String, Object> updateNickname(
            @ToolParam(description = "User id") String userId,
            @ToolParam(description = "New nickname") String nickname) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (userId == null || userId.isBlank() || nickname == null || nickname.isBlank()) {
                result.put("success", false);
                result.put("message", "missing_parameters");
                return result;
            }

            User user = userRepository.findById(userId);
            if (user == null) {
                result.put("success", false);
                result.put("message", "user_not_found");
                return result;
            }

            user.setNickname(nickname);
            int updated = userRepository.update(user);
            if (updated > 0) {
                result.put("success", true);
                result.put("message", "ok");
                result.put("userId", userId);
                result.put("nickname", nickname);
                return result;
            }

            result.put("success", false);
            result.put("message", "update_failed");
            return result;
        } catch (Exception ex) {
            result.put("success", false);
            result.put("message", "exception: " + ex.getMessage());
            return result;
        }
    }
}
