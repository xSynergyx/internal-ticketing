package com.example.ticketing;

public class TroubleTicket extends Ticket {

    public TroubleTicket(String subject, String body, String from_address, String status){
        super(subject, body, from_address, status);
    }

    @Override
    public String toString(){
        return("I'm a Trouble Ticket. \nThis is the subject: " + subject + "\nFrom Address: " + from_address + "\nStatus: " + status);
    }

}
