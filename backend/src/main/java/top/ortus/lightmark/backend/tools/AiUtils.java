package top.ortus.lightmark.backend.tools;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;

public class AiUtils {

    public AiUtils() {
        System.out.println("AiUtils init");
    }

    public SystemMessage SystemMessageGenerator(String systemPrompt) {
        // filter
        systemPrompt = systemPrompt.replaceAll("\\s+", "");
        return new SystemMessage(systemPrompt);
    }

    public UserMessage UserMessageGenerator(String userPrompt, String message) {

        // unconcerned information filter
        message = message.replaceAll("\\s+", "");
        return new UserMessage(userPrompt + message);
    }

    public AssistantMessage AssistantMessageGenerator(String assistantPrompt) {
        // filter
        assistantPrompt = assistantPrompt.replaceAll("\\s+", "");
        return new AssistantMessage(assistantPrompt);
    }

    public Prompt PromptGenerator(String systemPrompt, String userPrompt, String message) {
        return new Prompt(SystemMessageGenerator(systemPrompt), UserMessageGenerator(userPrompt, message));
    }

    public Prompt PromptGenerator(String systemPrompt, String userPrompt, String assistantPrompt, String message) {
        return new Prompt(SystemMessageGenerator(systemPrompt), UserMessageGenerator(userPrompt, message),
                AssistantMessageGenerator(assistantPrompt));
    }

    public Prompt PromptGenerator(SystemMessage systemMessage, UserMessage message) {
        return new Prompt(systemMessage, message);
    }

    public Prompt PromptGenerator(AssistantMessage assistantMessage) {
        return new Prompt(assistantMessage);
    }

    public Prompt PromptGenerator(SystemMessage systemMessage, UserMessage message, AssistantMessage assistantMessage) {
        return new Prompt(systemMessage, message, assistantMessage);
    }

    public Prompt PromptGenerator(List<Message> history) {
        return new Prompt(history);
    }
}
