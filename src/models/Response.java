package models;

public class Response {
    private String message = "";
    private String output = "";
    private String errors = "";
    private float durationInSeconds = -1;
    private String testsFailed = "";

    public Response() {
    }

    public Response(String message) {
        this.message = message;
    }

    public float getDurationInSeconds() {
        return durationInSeconds;
    }

    public void setDurationInSeconds(float durationInNanos) {
        this.durationInSeconds = durationInNanos;
    }

    public String getTestsFailed() {
        return testsFailed;
    }

    public void setTestsFailed(String testsFailed) {
        this.testsFailed = testsFailed;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getErrors() {
        return errors;
    }

    public void setErrors(String errors) {
        this.errors = errors;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
