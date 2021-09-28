package com.example.ticketing;

public class Ticket {

    protected String subject;
    protected String body;
    protected String fromAddress;
    //public date dateOpened;
    protected String status;

    public Ticket(String subject, String body, String fromAddress, String status){
        this.subject = subject;
        this.body = body;
        this.fromAddress = fromAddress;
        this.status = status;
    }

    protected void updateStatus(String newStatus){
        this.status = newStatus;
    }

}
