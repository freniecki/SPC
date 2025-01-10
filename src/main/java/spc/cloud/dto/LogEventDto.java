package spc.cloud.dto;

public class LogEventDto {
    private final String message;
    private final Long timestamp;

    public LogEventDto(String message, Long timestamp) {
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public Long getTimestamp() {
        return timestamp;
    }
}
