package top.ortus.timemark.backend.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.ortus.timemark.backend.dao.User;
import top.ortus.timemark.backend.dao.UserRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Component
public class UpdateEmailTool {

    private final UserRepository userRepository;
    private static final Pattern EMAIL_RE = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    @Autowired
    public UpdateEmailTool(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Tool(name = "update_email", description = "Update the user's email by user id.")
    public Map<String, Object> updateEmail(
            @ToolParam(description = "User id") String userId,
            @ToolParam(description = "New email") String email) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (userId == null || userId.isBlank() || email == null || email.isBlank()) {
                result.put("success", false);
                result.put("message", "missing_parameters");
                return result;
            }

            String newEmail = email.trim();
            if (!EMAIL_RE.matcher(newEmail).matches()) {
                result.put("success", false);
                result.put("message", "invalid_email_format");
                return result;
            }

            User user = userRepository.findById(userId);
            if (user == null) {
                result.put("success", false);
                result.put("message", "user_not_found");
                return result;
            }

            user.setEmail(newEmail);
            int updated = userRepository.update(user);
            if (updated > 0) {
                result.put("success", true);
                result.put("message", "ok");
                result.put("userId", userId);
                result.put("email", newEmail);
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
