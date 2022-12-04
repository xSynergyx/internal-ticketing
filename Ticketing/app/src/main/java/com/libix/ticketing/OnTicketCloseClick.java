package com.libix.ticketing;
public interface OnTicketCloseClick {
    void onTicketCloseClick (String subject, String graph_id, String solution);

    void onTicketStatusClick (String subject);

    void onNotTicketClick (String subject, String graph_id);
}
