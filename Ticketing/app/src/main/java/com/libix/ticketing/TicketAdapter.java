package com.libix.ticketing;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {

    ArrayList <TroubleTicket> tickets = new ArrayList<TroubleTicket>();
    Context context;
    private OnTicketCloseClick onTicketCloseClick;
    private OnTicketCloseClick onTicketStatusClick;
    private OnTicketCloseClick onNotTicketClick;

    public TicketAdapter(Context context, ArrayList<TroubleTicket> tickets, OnTicketCloseClick onTicketCloseClick){
        this.context = context;
        this.tickets = tickets;
        this.onTicketCloseClick = onTicketCloseClick;
        this.onTicketStatusClick = onTicketCloseClick;
        this.onNotTicketClick = onTicketCloseClick;
    }
    @NonNull
    @Override
    public TicketAdapter.TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.ticket, parent, false);

        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketAdapter.TicketViewHolder holder, int position) {

        // Reset the buttons visibility and text color back to default
        holder.statusButton.setVisibility(View.VISIBLE);
        holder.statusButton.setClickable(true);
        holder.statusView.setTextColor(Color.parseColor("#ffcc0000")); //TODO: Make a "textColor" var

        holder.subjectView.setText(tickets.get(position).subject);
        holder.statusView.setText(tickets.get(position).status);
        holder.descriptionView.setText(tickets.get(position).body);
        holder.expandedDescription.setText(tickets.get(position).body);


        if (tickets.get(position).status.equalsIgnoreCase("ongoing")) {
            holder.statusButton.setVisibility(View.INVISIBLE);
            holder.statusButton.setClickable(false);
            holder.statusView.setTextColor(Color.parseColor("#3bb3db"));
        }

    }

    @Override
    public int getItemCount() {
        try {
            return tickets.size();
        } catch (Exception e){
            return 0;
        }
    }

    public class TicketViewHolder extends RecyclerView.ViewHolder {

        TextView subjectView;
        TextView statusView;
        TextView descriptionView;
        Button closeButton;
        Button statusButton;
        Button notTicketButton;
        TextView expandedDescription;

        LinearLayout ticketControls;
        LinearLayout solutionView;
        Button solutionButton;
        EditText solutionEditText;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);

            subjectView = itemView.findViewById(R.id.ticket_subject);
            statusView = itemView.findViewById(R.id.ticket_status);
            descriptionView = itemView.findViewById(R.id.ticket_description);
            closeButton = itemView.findViewById(R.id.close_button);
            statusButton = itemView.findViewById(R.id.update_status_button);
            notTicketButton = itemView.findViewById(R.id.not_ticket_button);
            expandedDescription = itemView.findViewById(R.id.expanded_text);

            ticketControls = itemView.findViewById(R.id.ticket_controls_view);
            solutionView = itemView.findViewById(R.id.solution_view);
            solutionButton = itemView.findViewById(R.id.solution_button);
            solutionEditText = itemView.findViewById(R.id.solution_edit_text_view);



            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*
                    int position = getAdapterPosition();
                    String clickedSubject = tickets.get(position).subject;
                    String clickedGraphId = tickets.get(position).graph_id;
                     */

                    // Hide the close, status and not-ticket buttons and make them unclickable. Then display the solution button and edit textview
                    ticketControls.setVisibility(View.INVISIBLE);
                    closeButton.setClickable(false);
                    statusButton.setClickable(false);
                    notTicketButton.setClickable(false);

                    solutionView.setVisibility(View.VISIBLE);
                    solutionButton.setClickable(true);
                }
            });

            statusButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    String clickedSubject = tickets.get(position).subject;

                    onTicketStatusClick.onTicketStatusClick(clickedSubject);
                    statusButton.setVisibility(View.INVISIBLE);
                    statusButton.setClickable(false);
                    statusView.setText("Ongoing");
                    statusView.setTextColor(Color.parseColor("#3bb3db"));
                }
            });

            solutionButton.setOnClickListener((new View.OnClickListener() {
                @Override
                public void onClick(View v){
                    int position = getAdapterPosition();
                    String clickedSubject = tickets.get(position).subject;
                    String clickedGraphId = tickets.get(position).graph_id;

                    String solutionText = solutionEditText.getText().toString().trim();
                    solutionEditText.getText().clear();
                    Log.d("SOLUTION VIEW", "Solution view was clicked\n Here's the solution text: " + solutionText);

                    Log.d("Close", clickedSubject);
                    onTicketCloseClick.onTicketCloseClick(clickedSubject, clickedGraphId, solutionText);
                    Log.d("Close", "Subject sent");

                    //Resetting the item in this list in case sync is called after deleting/closing a ticket
                    ticketControls.setVisibility(View.VISIBLE);
                    closeButton.setClickable(true);
                    statusButton.setClickable(true);
                    notTicketButton.setClickable(true);

                    solutionView.setVisibility(View.INVISIBLE);
                    solutionButton.setClickable(false);
                    removeItem(position);
                }
            }));

            notTicketButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    String clickedSubject = tickets.get(position).subject;
                    String clickedGraphId = tickets.get(position).graph_id;

                    onNotTicketClick.onNotTicketClick(clickedSubject, clickedGraphId);
                    removeItem(position);
                }
            });

            descriptionView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    showExpandedDescription(position);
                }
            });

            expandedDescription.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    showExpandedDescription(position);
                }
            });
        }

        private void showExpandedDescription(int position){
            String ticketDescription = tickets.get(position).body;

            if (expandedDescription.getVisibility() == View.GONE) {
                expandedDescription.setVisibility(View.VISIBLE);
                descriptionView.setText("");
            } else {
                expandedDescription.setVisibility(View.GONE);
                descriptionView.setText(ticketDescription);
            }
        }
    }



    private void removeItem(int position) {
        tickets.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, tickets.size());
    }
}
