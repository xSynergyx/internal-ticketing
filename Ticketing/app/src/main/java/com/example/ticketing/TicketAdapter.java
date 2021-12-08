package com.example.ticketing;

import android.content.Context;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {

    ArrayList <TroubleTicket> tickets = new ArrayList<TroubleTicket>();
    Context context;
    private OnTicketCloseClick onTicketCloseClick;

    public TicketAdapter(Context context, ArrayList<TroubleTicket> tickets, OnTicketCloseClick onTicketCloseClick){
        this.context = context;
        this.tickets = tickets;
        this.onTicketCloseClick = onTicketCloseClick;
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

        holder.subjectView.setText(tickets.get(position).subject);
        holder.statusView.setText(tickets.get(position).status);
        holder.descriptionView.setText(tickets.get(position).body);
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

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);

            subjectView = itemView.findViewById(R.id.ticket_subject);
            statusView = itemView.findViewById(R.id.ticket_status);
            descriptionView = itemView.findViewById(R.id.ticket_description);
            closeButton = itemView.findViewById(R.id.close_button);

            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    String clickedSubject = tickets.get(position).subject;
                    Log.d("Close", clickedSubject);
                    Log.d("Close", "About to send subject callback");
                    onTicketCloseClick.onTicketCloseClick(clickedSubject);
                    Log.d("Close", "Subject callback sent");
                    removeItem(position);
                }
            });

        }
    }

    private void removeItem(int position) {
        tickets.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, tickets.size());
    }
}
