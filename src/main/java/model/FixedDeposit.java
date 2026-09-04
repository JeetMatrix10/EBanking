package model;

import java.math.BigDecimal;
// import java.sql.Date;      // <-- Old code commented out
import java.sql.Timestamp;    // <-- New replacement

public class FixedDeposit {
    private int fdId;
    private String cid;
    private String accno;
    private BigDecimal amount;
    private int noOfYears;
    private BigDecimal interestRate;
    
    // private Date bookDate;      // <-- Old code commented out
    private Timestamp bookDate;    // <-- New replacement
    
    // private Date maturityDate;  // <-- Old code commented out
    private Timestamp maturityDate;// <-- New replacement
    
    private String status;

    public FixedDeposit() {
    }

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

    // public Date getBookDate() {  // <-- Old code commented out
    //     return bookDate;
    // }
    public Timestamp getBookDate() {
        return bookDate;
    }

    // public void setBookDate(Date bookDate) { // <-- Old code commented out
    //     this.bookDate = bookDate;
    // }
    public void setBookDate(Timestamp bookDate) {
        this.bookDate = bookDate;
    }

    // public Date getMaturityDate() { // <-- Old code commented out
    //     return maturityDate;
    // }
    public Timestamp getMaturityDate() {
        return maturityDate;
    }

    // public void setMaturityDate(Date maturityDate) { // <-- Old code commented out
    //     this.maturityDate = maturityDate;
    // }
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