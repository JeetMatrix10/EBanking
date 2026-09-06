package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class RecurringDeposit {
	private int rdId;
	private String cid;
	private String accno;
	private BigDecimal monthlyAmount;
	private int noOfMonths;
	private BigDecimal interestRate;
	private int installmentsPaid;
	private Timestamp bookDate;
	private Timestamp nextDebitDate;
	private String status;

	public RecurringDeposit() {
	}

	public int getRdId() {
		return rdId;
	}

	public void setRdId(int rdId) {
		this.rdId = rdId;
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

	public BigDecimal getMonthlyAmount() {
		return monthlyAmount;
	}

	public void setMonthlyAmount(BigDecimal monthlyAmount) {
		this.monthlyAmount = monthlyAmount;
	}

	public int getNoOfMonths() {
		return noOfMonths;
	}

	public void setNoOfMonths(int noOfMonths) {
		this.noOfMonths = noOfMonths;
	}

	public BigDecimal getInterestRate() {
		return interestRate;
	}

	public void setInterestRate(BigDecimal interestRate) {
		this.interestRate = interestRate;
	}

	public int getInstallmentsPaid() {
		return installmentsPaid;
	}

	public void setInstallmentsPaid(int installmentsPaid) {
		this.installmentsPaid = installmentsPaid;
	}

	public Timestamp getBookDate() {
		return bookDate;
	}

	public void setBookDate(Timestamp bookDate) {
		this.bookDate = bookDate;
	}

	public Timestamp getNextDebitDate() {
		return nextDebitDate;
	}

	public void setNextDebitDate(Timestamp nextDebitDate) {
		this.nextDebitDate = nextDebitDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	private boolean testMode;

	public boolean isTestMode() {
		return testMode;
	}

	public void setTestMode(boolean testMode) {
		this.testMode = testMode;
	}
}