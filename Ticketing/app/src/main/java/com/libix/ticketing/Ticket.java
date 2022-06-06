package com.libix.ticketing;

public class Ticket {

    protected String subject;
    protected String body;
    protected String from_address;
    protected String status;

    public Ticket(String subject, String body, String from_address, String status){
        this.subject = subject;
        this.body = body;
        this.from_address = from_address;
        this.status = status;
    }

}
