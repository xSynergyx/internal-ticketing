package com.libix.ticketing;

public class User {

    protected String email;
    protected String firstName;
    protected String lastName;
    protected int tickets_closed;
    //protected String profile_image;  //String? File? Something?

    public User(String email, String firstName, String lastName, int tickets_closed){
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.tickets_closed = tickets_closed;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getTicketsClosed() {
        return tickets_closed;
    }

    public void setTicketsClosed(int tickets_closed) {
        this.tickets_closed = tickets_closed;
    }
}
