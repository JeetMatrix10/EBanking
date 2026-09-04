package model;

import java.sql.Date;
import java.math.BigDecimal;

public class Account {
    private String accno;
    private String cid;
    private Date opendate;

    // Why BigDecimal instead of double for money: double uses binary
    // floating-point, which can't represent decimal fractions like 0.1
    // exactly — leading to tiny rounding errors that compound over many
    // transactions. BigDecimal represents decimal values exactly, which is
    // why real financial systems always use it instead of double/float.
    private BigDecimal balance;
    private String accounttype;

    public Account() {
    }

    public Account(String cid, Date opendate, BigDecimal balance, String accounttype) {
        this.cid = cid;
        this.opendate = opendate;
        this.balance = balance;
        this.accounttype = accounttype;
    }

    public String getAccno() {
        return accno;
    }

    public void setAccno(String accno) {
        this.accno = accno;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public Date getOpendate() {
        return opendate;
    }

    public void setOpendate(Date opendate) {
        this.opendate = opendate;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getAccounttype() {
        return accounttype;
    }

    public void setAccounttype(String accounttype) {
        this.accounttype = accounttype;
    }
}