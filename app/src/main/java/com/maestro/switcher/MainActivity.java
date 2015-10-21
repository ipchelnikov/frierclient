package com.maestro.switcher;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;


public class MainActivity extends AppCompatActivity {

    private Socket socket;
    private DataOutputStream ostream;
    private DataInputStream instream;
    private EditText command_string;

    private static Logger log = Logger.getLogger(MainActivity.class.getName());

    public static final int SERVERPORT = 6666;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        command_string = (EditText) findViewById(R.id.command_string);

        command_string.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                log.info("On key: : "+keyCode);

                // Actions filter
                if (event.getAction() != KeyEvent.ACTION_DOWN)
                    return false;

                switch (keyCode)
                {
                    case KeyEvent.KEYCODE_ENTER:
                        log.info("Enter");
                        try {
                            String command = command_string.getText().toString();
                            command += '\0';
                            ostream.writeBytes(command);
                        }
                        catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        log.info("Unknown key. Have no idea what to do..");
                }

                return false;
            }
        });

        new Thread(new ClientThread()).start();
    }

    class ClientThread implements Runnable {
        @Override
        public void run() {

            try {
                InetAddress serverAddr = InetAddress.getByName("192.168.0.100");
                socket = new Socket(serverAddr, SERVERPORT);

                ostream = new DataOutputStream(socket.getOutputStream());
                instream = new DataInputStream(socket.getInputStream());

                while (true)
                {

                    byte [] buf = new byte[1000];
                    int num_bytes = instream.read(buf);
                    final String st_buf = new String(buf, 0, num_bytes, "UTF-8");
                    log.info("Received message: " + st_buf);


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            command_string.append(st_buf);
                        }
                    });

                }

            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }



    }


}


