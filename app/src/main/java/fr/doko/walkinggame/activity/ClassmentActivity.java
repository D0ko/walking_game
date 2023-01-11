package fr.doko.walkinggame.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.doko.walkinggame.R;
import fr.doko.walkinggame.util.PedometerService;

public class ClassmentActivity extends AppCompatActivity {
    private TextView textView_classement;
    Button button_return;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classment);

        button_return = findViewById(R.id.button_return);
        button_return.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(ClassmentActivity.this, PedometerActivity.class);
                startActivity(intent);
            }
        });

        textView_classement = (TextView) findViewById(R.id.textView_classement);

        new Thread(new ClientThread_Refresh(textView_classement)).start();

    }

    static class ClientThread_Refresh implements Runnable {

        private final Map<String, String> map = new HashMap<>();
        private List<String> fromServer_new;
        private TextView textView_msg;
        private String[] transi;
        private StringBuilder string_affichage = new StringBuilder();
        ArrayList<String> aff = new ArrayList<String>();
        String[] chaine;

        ClientThread_Refresh(TextView textView_msg) {
            this.textView_msg = textView_msg;
        }

        @Override
        public void run () {
            try {
                Socket socket = new Socket("82.66.70.21", 44444);
                Log.d("server connect", "Connected!");

                String fromServer;
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);


                out.println("1");
                fromServer = in.readLine();
                Log.d("server resp", "received2: " + fromServer);

                fromServer = fromServer.substring(1, fromServer.length() - 1);
                fromServer_new = Arrays.asList(fromServer.split(", "));

                for (int i = 0; i < fromServer_new.size(); i++) {
                    chaine = fromServer_new.get(i).split("=");
                    map.put(chaine[0], chaine[1]);
                    transi = (chaine[1].split("//-"));
                    aff.add(transi[0]);
                    aff.add(transi[1]);
                }

                for (int i = 0; i < aff.size(); i += 2) {
                    string_affichage.append(aff.get(i));
                    string_affichage.append("  :  ");
                    string_affichage.append(aff.get(i + 1));
                    string_affichage.append("\n");
                }

                textView_msg.setText(string_affichage);

                in.close();
                out.close();
                socket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}