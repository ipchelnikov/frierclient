package com.maestro.switcher;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Logger;


public class MainActivity extends AppCompatActivity implements ConnectDialogFragment.Connector {

    private Socket socket;
    private DataOutputStream ostream;
    private DataInputStream instream;
    private EditText command_string; // TODO: change EditText to CommandString class
    private String server_ip;
    private ProgressDialog progress;
    private int command_start;

    private static Logger log = Logger.getLogger(MainActivity.class.getName());

    public static final int SERVER_PORT = 6666;
    public static final int CONNECT_TIME_OUT = 10000; // in milliseconds

    @Override
    public void Connect(String ip){

        server_ip = ip;

        if (progress != null && !progress.isShowing())
            progress = ProgressDialog.show(this,
                    getBaseContext().getString(R.string.progress_dialog_title),
                    getBaseContext().getString(R.string.progress_dialog_message), true);

        new Thread(new ClientThread()).start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progress = new ProgressDialog(MainActivity.this);

        (new ConnectDialog()).connect();

        command_string = (EditText) findViewById(R.id.command_string);

        command_string.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                // Actions filter
                if (event.getAction() != KeyEvent.ACTION_DOWN)
                    return false;

                switch (keyCode) {
                    case KeyEvent.KEYCODE_ENTER:
                        log.info("Processing enter command");
                        try {
                            String command = command_string.getText().subSequence(command_start,
                                    command_string.getSelectionEnd()).toString();

                            command += '\0';
                            ostream.writeBytes(command);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        // Ignoring unknown key
                }

                return false;
            }
        });
    }

    class ClientThread implements Runnable {
        @Override
        public void run() {

            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        command_string.append(">");
                        command_start = command_string.getSelectionEnd();
                    }
                });

                InetAddress serverAddr = InetAddress.getByName(server_ip);

                socket = new Socket();
                socket.connect(new InetSocketAddress(serverAddr, SERVER_PORT), CONNECT_TIME_OUT);

                ostream = new DataOutputStream(socket.getOutputStream());
                instream = new DataInputStream(socket.getInputStream());

                if (progress != null && progress.isShowing())
                    progress.dismiss();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        command_string.setEnabled(true); // Enabling console on connect
                    }
                });

                while (true)
                {
                    byte [] buf = new byte[1000];
                    int num_bytes = instream.read(buf);

                    if (buf[0] == 1)
                    {
                        // skip start
                        continue;
                    }if (buf[0] == 2) {
                        // finish
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                command_string.append(">");
                                command_start = command_string.getSelectionEnd();
                            }
                        });
                    } else {

                        final String st_buf = new String(buf, 0, num_bytes, "UTF-8");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                command_string.append(st_buf);
                                command_start = command_string.getSelectionEnd();
                            }
                        });
                    }
                }

            } catch (Exception e1) {
                e1.printStackTrace();
                (new ConnectDialog(server_ip,
                        getBaseContext().getString(R.string.host_unreachable))).connect();
            }
        }
    }

    class ConnectDialog {

        String ip, text;

        ConnectDialogFragment connectDialog;

        public ConnectDialog() {
            connectDialog = new ConnectDialogFragment();
        }

        public ConnectDialog(String ip, String text) {
            this.ip = ip;
            this.text = text;
            connectDialog = new ConnectDialogFragment();
        }

        void connect()
        {
            Bundle args = new Bundle();
            args.putString("ip", ip);
            args.putString("text", text);
            connectDialog.setArguments(args);
            connectDialog.show(getFragmentManager(), "Connect");
        }
    }
}