package com.maestro.switcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.view.View;

import java.util.logging.Logger;


/**
 * Created by maestro on 22.10.2015.
 */
public class ConnectDialogFragment extends DialogFragment {

    private Connector mConnector;

    public interface Connector {
        public void Connect(String ip);
    }


    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mConnector = (Connector) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View connect_dial_view = inflater.inflate(R.layout.connect, null);

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("IP address");

        try {
            String local_ip = getArguments().getString("ip");
            String local_text = getArguments().getString("text");

            builder.setMessage(local_text);

            if (!local_text.isEmpty())
                builder.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getActivity().finish();
                        System.exit(0);
                    }
                });

            ((EditText) connect_dial_view.findViewById(R.id.ip)).setText(local_ip);
        }
        catch (Exception e) {
            e.printStackTrace();
            Logger.getLogger(MainActivity.class.getName()).info("IP: exception!");
        }

        builder.setView(connect_dial_view)
                .setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        EditText ip_string = (EditText) connect_dial_view.findViewById(R.id.ip);
                        mConnector.Connect(ip_string.getText().toString());
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}