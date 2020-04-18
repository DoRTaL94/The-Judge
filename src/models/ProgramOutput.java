package models;

public class ProgramOutput {
    private final String splitRegex = "--cut-here--";
    private String output = "";
    private String errors = "";
    private String testsFailed = "";
    private float durationInSeconds = -1;
    private boolean isTerminatedBeforeTime = false;

    public boolean isTerminatedBeforeTime() {
        return isTerminatedBeforeTime;
    }

    public void setTerminatedBeforeTime(boolean terminatedBeforeTime) {
        isTerminatedBeforeTime = terminatedBeforeTime;
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

    public float getDurationInSeconds() {
        return durationInSeconds;
    }

    public void setDurationInSeconds(float durationInNanos) {
        this.durationInSeconds = durationInNanos;
    }

    // Function that set the program output.
    // If the output include the string '--cut-here--',
    // that means that the line after there is an array of failed tests separated by space.
    // In that case we're going to split the string into two parts:
    // 1. Program output.
    // 2. Failed tests.
    // Each string will assigned to its proper field.
    public void setOutput(String output) {
        StringBuilder sb = new StringBuilder(this.output);

        if(!this.output.equals("")) {
            sb.append(System.lineSeparator());
        }

        String[] split = output.split(splitRegex);

        sb.append(split[0]);
        this.output = sb.toString();

        if(split.length > 1 && split[1].trim().length() > 0) {
            setDurationInSeconds(Float.parseFloat(split[1]));
        }
        if(split.length > 2 && split[2].trim().length() > 0) {
            setTestsFailed(split[2]);
        }

    }

    public String getErrors() {
        return errors;
    }

    public void setErrors(String errors) {
        StringBuilder sb = new StringBuilder(this.errors);

        if(!this.errors.equals("")) {
            sb.append(System.lineSeparator());
        }

        sb.append(errors);
        this.errors = sb.toString();
    }
}
