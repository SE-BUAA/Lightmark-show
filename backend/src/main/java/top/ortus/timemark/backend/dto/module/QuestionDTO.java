package top.ortus.timemark.backend.dto.module;

import java.time.LocalDateTime;

public class QuestionDTO {
    private String id;
    private String user_id;
    private String user_nickname;
    private String title;
    private String content;
    private String answer;
    private String answer_user_id;
    private String answer_user_nickname;
    private int status;
    private LocalDateTime create_time;
    private LocalDateTime answer_time;

    public QuestionDTO() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_nickname() {
        return user_nickname;
    }

    public void setUser_nickname(String user_nickname) {
        this.user_nickname = user_nickname;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getAnswer_user_id() {
        return answer_user_id;
    }

    public void setAnswer_user_id(String answer_user_id) {
        this.answer_user_id = answer_user_id;
    }

    public String getAnswer_user_nickname() {
        return answer_user_nickname;
    }

    public void setAnswer_user_nickname(String answer_user_nickname) {
        this.answer_user_nickname = answer_user_nickname;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public LocalDateTime getCreate_time() {
        return create_time;
    }

    public void setCreate_time(LocalDateTime create_time) {
        this.create_time = create_time;
    }

    public LocalDateTime getAnswer_time() {
        return answer_time;
    }

    public void setAnswer_time(LocalDateTime answer_time) {
        this.answer_time = answer_time;
    }
}
