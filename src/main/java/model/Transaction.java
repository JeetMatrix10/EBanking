package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Transaction {
    private int transactionId;
    private String saccno;
    private String benaccno;
    private BigDecimal amount;
    private Timestamp transdt;
    private String type;

    public Transaction() {
    }

    public Transaction(String saccno, String benaccno, BigDecimal amount, String type) {
        this.saccno = saccno;
        this.benaccno = benaccno;
        this.amount = amount;
        this.type = type;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public String getSaccno() {
        return saccno;
    }

    public void setSaccno(String saccno) {
        this.saccno = saccno;
    }

    public String getBenaccno() {
        return benaccno;
    }

    public void setBenaccno(String benaccno) {
        this.benaccno = benaccno;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Timestamp getTransdt() {
        return transdt;
    }

    public void setTransdt(Timestamp transdt) {
        this.transdt = transdt;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}