package com.libix.ticketing;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewTicketFragment extends Fragment {

    public NewTicketFragment() {
        // Required empty public constructor
    }

    public EditText newTicketEmail;
    public EditText newTicketSubject;
    public EditText newTicketMessage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_ticket, container, false);

        newTicketEmail = (EditText) view.findViewById(R.id.new_ticket_email);
        newTicketSubject = (EditText) view.findViewById(R.id.new_ticket_subject);
        newTicketMessage = (EditText) view.findViewById(R.id.new_ticket_message);

        FloatingActionButton fab = view.findViewById(R.id.floating_action_button);
        fab.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                sendMail();
            }
        });

        return view;
    }

    private void sendMail(){
        String email = newTicketEmail.getText().toString().trim();
        String subject = newTicketSubject.getText().toString().trim();
        String message = newTicketMessage.getText().toString();

        JavaMail javaMail = new JavaMail(getContext(), email, subject, message);
        javaMail.execute();
    }
}