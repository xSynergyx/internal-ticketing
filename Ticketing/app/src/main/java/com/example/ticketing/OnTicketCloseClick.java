package com.example.ticketing;

public interface OnTicketCloseClick {
    void onTicketCloseClick (String subject, String graph_id);

    void onTicketStatusClick (String subject);
}
