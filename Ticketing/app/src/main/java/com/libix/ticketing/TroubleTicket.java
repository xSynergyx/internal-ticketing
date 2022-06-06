package com.libix.ticketing;

public class TroubleTicket extends Ticket {

    protected String graph_id;
    protected String solution;

    public TroubleTicket(String subject, String body, String from_address, String status, String graph_id, String solution){
        super(subject, body, from_address, status);
        this.graph_id = graph_id;
        this.solution = solution;
    }

    @Override
    public String toString(){
        return("\nI'm a Trouble Ticket. \nThis is the subject: " + subject + "\nFrom Address: " + from_address + "\nStatus: " + status);
    }

}
