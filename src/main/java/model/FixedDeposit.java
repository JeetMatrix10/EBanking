package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class FixedDeposit {
	private int fdId;
	private String cid;
	private String accno;
	private BigDecimal amount;
	private int noOfYears;
	private BigDecimal interestRate;
	private Timestamp bookDate;
	private Timestamp maturityDate;
	private String status;

	public FixedDeposit() {
	}

	// Why Timestamp instead of Date for bookDate/maturityDate: this class
	// originally used java.sql.Date (day-only precision), but that couldn't
	// distinguish minutes when test mode was added (an FD "maturing in 3
	// minutes" would round to the same DAY as booking, making it
	// indistinguishable from one maturing in 3 years on the same day).
	// Timestamp preserves time-of-day, which both real (years) and test
	// (minutes) maturity calculations depend on.
	public int getFdId() {
		return fdId;
	}

	public void setFdId(int fdId) {
		this.fdId = fdId;
	}

	public String getCid() {
		return cid;
	}

	public void setCid(String cid) {
		this.cid = cid;
	}

	public String getAccno() {
		return accno;
	}

	public void setAccno(String accno) {
		this.accno = accno;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public int getNoOfYears() {
		return noOfYears;
	}

	public void setNoOfYears(int noOfYears) {
		this.noOfYears = noOfYears;
	}

	public BigDecimal getInterestRate() {
		return interestRate;
	}

	public void setInterestRate(BigDecimal interestRate) {
		this.interestRate = interestRate;
	}

	public Timestamp getBookDate() {
		return bookDate;
	}

	public void setBookDate(Timestamp bookDate) {
		this.bookDate = bookDate;
	}

	public Timestamp getMaturityDate() {
		return maturityDate;
	}

	public void setMaturityDate(Timestamp maturityDate) {
		this.maturityDate = maturityDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}