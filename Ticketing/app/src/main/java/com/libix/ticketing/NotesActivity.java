package com.libix.ticketing;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class NotesActivity extends MainActivity {

    public EditText newTicketEmail;
    public EditText newTicketSubject;
    public EditText newTicketMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        newTicketEmail = (EditText)findViewById(R.id.new_ticket_email);
        newTicketSubject = (EditText)findViewById(R.id.new_ticket_subject);
        newTicketMessage = (EditText)findViewById(R.id.new_ticket_message);

        FloatingActionButton fab = findViewById(R.id.floating_action_button);
        fab.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                sendMail();
            }
        });
    }

    private void sendMail(){
        String mail = newTicketEmail.getText().toString().trim();
        String subject = newTicketSubject.getText().toString().trim();
        String message = newTicketMessage.getText().toString();

        JavaMail javaMail = new JavaMail(this, mail, subject, message);

        javaMail.execute();
    }
}