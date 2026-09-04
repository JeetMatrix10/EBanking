package model;

// Why this is a separate small class rather than reusing Transaction: this
// isn't a database entity — it never gets saved anywhere. It exists purely
// to carry two pieces of information (did it work, and is there a warning)
// from the DAO back to the servlet in one clean object.
public class TransferResult {
    private boolean success;
    private String warningMessage; // null if no warning

    public TransferResult(boolean success, String warningMessage) {
        this.success = success;
        this.warningMessage = warningMessage;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getWarningMessage() {
        return warningMessage;
    }
}