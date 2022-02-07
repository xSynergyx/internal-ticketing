package com.example.ticketing;

public class Ticket {

    protected String subject;
    protected String body;
    protected String from_address;
    protected String status;
    protected String graph_id;

    public Ticket(String subject, String body, String from_address, String status, String graph_id){
        this.subject = subject;
        this.body = body;
        this.from_address = from_address;
        this.status = status;
        this.graph_id = graph_id;
    }

    protected void updateStatus(String newStatus){
        this.status = newStatus;
    }

}
