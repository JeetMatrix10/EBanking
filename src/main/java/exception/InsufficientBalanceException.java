package exception;

// Why this extends Exception (checked), not RuntimeException (unchecked):
// insufficient balance is an expected, recoverable business scenario —
// not a programming bug. Checked exceptions force whoever calls this method
// to explicitly handle it (via try/catch or "throws"), which is appropriate
// here since silently ignoring a failed withdrawal would be a serious bug.
public class InsufficientBalanceException extends Exception {
	private static final long serialVersionUID = 1L;

	public InsufficientBalanceException(String message) {
		super(message);
	}
}