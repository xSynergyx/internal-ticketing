package com.example.ticketing;

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

public class ClosedTicketAdapter extends RecyclerView.Adapter<ClosedTicketAdapter.ClosedTicketViewHolder> {

    ArrayList<TroubleTicket> tickets = new ArrayList<TroubleTicket>();
    Context context;

    public ClosedTicketAdapter(Context context, ArrayList<TroubleTicket> tickets){
        this.context = context;
        this.tickets = tickets;
    }
    @NonNull
    @Override
    public ClosedTicketAdapter.ClosedTicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.closed_ticket, parent, false);

        return new ClosedTicketAdapter.ClosedTicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClosedTicketAdapter.ClosedTicketViewHolder holder, int position) {
        //TODO: Finish this ClosedTicketAdapter so I can get the recyclerview working in SearchActivity
        holder.subjectView.setText(tickets.get(position).subject);
        holder.statusView.setText(tickets.get(position).status);
        holder.descriptionView.setText(tickets.get(position).body);
        holder.solutionView.setText(tickets.get(position).solution);



        if (tickets.get(position).status.equalsIgnoreCase("closed")) {

            holder.statusView.setTextColor(Color.parseColor("#0ac917"));
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

    public class ClosedTicketViewHolder extends RecyclerView.ViewHolder {

        TextView subjectView;
        TextView statusView;
        TextView descriptionView;
        TextView solutionView;

        public ClosedTicketViewHolder(@NonNull View itemView) {
            super(itemView);

            subjectView = itemView.findViewById(R.id.closed_ticket_subject);
            statusView = itemView.findViewById(R.id.closed_ticket_status);
            descriptionView = itemView.findViewById(R.id.closed_ticket_description);
            solutionView = itemView.findViewById(R.id.closed_ticket_solution);
        }
    }
}
