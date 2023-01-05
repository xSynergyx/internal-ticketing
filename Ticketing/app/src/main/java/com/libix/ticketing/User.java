package com.libix.ticketing;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {

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

    // Read from parcel
    protected User(Parcel in) {
        email = in.readString();
        firstName = in.readString();
        lastName = in.readString();
        tickets_closed = in.readInt();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    // Write to parcel (It's in the method name)
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(email);
        parcel.writeString(firstName);
        parcel.writeString(lastName);
        parcel.writeInt(tickets_closed);
    }
}
