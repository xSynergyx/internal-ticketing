package com.example.ticketing;

        import android.app.ProgressDialog;
        import android.content.Context;
        import android.os.AsyncTask;
        import android.widget.Toast;

public class ReceiveMail extends AsyncTask<Void,Void,Void> {

    private Context mContext;

    private ProgressDialog mProgressDialog;

    //Constructor
    public ReceiveMail(Context mContext){
        this.mContext = mContext;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //Show progress dialog while sending email
        mProgressDialog = ProgressDialog.show(mContext,"Sending message", "Please wait...",false,false);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        //Dismiss progress dialog when message successfully send
        mProgressDialog.dismiss();

        //Show success toast
        Toast.makeText(mContext,"Message Sent",Toast.LENGTH_SHORT).show();
    }

    protected Void doInBackground(Void... params){


        return null;
    }
}
