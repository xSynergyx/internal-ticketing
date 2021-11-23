package com.example.ticketing;

public class Ticket {

    protected String subject;
    protected String body;
    protected String from_address;
    //public date dateOpened;
    protected String status;

    public Ticket(String subject, String body, String from_address, String status){
        this.subject = subject;
        this.body = body;
        this.from_address = from_address;
        this.status = status;
    }

    protected void updateStatus(String newStatus){
        this.status = newStatus;
    }

}
