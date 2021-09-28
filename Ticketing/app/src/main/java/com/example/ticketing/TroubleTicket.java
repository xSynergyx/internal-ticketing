package com.example.ticketing;

public class TroubleTicket extends Ticket {

    public TroubleTicket(String subject, String body, String fromAddress, String status){
        super(subject, body, fromAddress, status);
    }

    @Override
    public String toString(){
        return("I'm a Trouble Ticket. \nThis is the subject: " + subject + "\nFrom Address: " + fromAddress + "\nStatus: " + status);
    }

}
