package com.example.ticketing;

public class TroubleTicket extends Ticket {

    public TroubleTicket(String subject, String body, String from_address, String status, String graph_id){
        super(subject, body, from_address, status, graph_id);
    }

    @Override
    public String toString(){
        return("\nI'm a Trouble Ticket. \nThis is the subject: " + subject + "\nFrom Address: " + from_address + "\nStatus: " + status);
    }

}
