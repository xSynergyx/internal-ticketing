package com.libix.ticketing;
// TODO: Rename this interface
public interface OnTicketCloseClick {
    void onTicketCloseClick (String subject, String graph_id, String solution);

    void onTicketStatusClick (String subject);

    void onNotTicketClick (String subject, String graph_id);
}
