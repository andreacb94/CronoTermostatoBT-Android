package com.example.andrea.testcronotermostato;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

/* App in fase di test. Tutto da rivedere, commentare il codice e testare. Per ora sembra funzioni bene.
Connessione bt:
https://www.makeritalia.org/tutorial/2015/12/23/connessione-bluetooth-arduino-andoid/
 */

public class MainActivity extends AppCompatActivity {

    CountDownTimer cd;
    //per bluetooth
    public UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    BluetoothAdapter mBluetoothAdapter=null;
    BluetoothSocket mmSocket=null;
    BluetoothDevice mmDevice=null;
    OutputStream outStream;
    String arduino_MAC = "98:D3:31:90:82:A4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText ore =(EditText) findViewById(R.id.ore);
        final EditText minuti =(EditText) findViewById(R.id.minuti);

        Button accendi = (Button) findViewById(R.id.accendi);
        Button spegni = (Button) findViewById(R.id.spegni);

        final TextView countdown = (TextView) findViewById(R.id.countdown);

        accendi.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                int start = 0;

                try {

                    start = Integer.parseInt(ore.getText().toString()) * 60 * 1000;
                    start += Integer.parseInt(minuti.getText().toString()) * 1000;
                }
                catch (Exception e){

                    Toast.makeText(getBaseContext(), "Non hai inserito un tempo!", Toast.LENGTH_SHORT).show();

                }

                try {
                    cd.cancel();
                }
                catch (Exception e){

                }
                cd = new CountDownTimer(start, 1000) { // adjust the milli seconds here

                    public void onTick(long millisUntilFinished) {
                        sendMessageBluetooth("1");
                        countdown.setText(String.valueOf(millisUntilFinished/1000));
                        Toast.makeText(getBaseContext(), "Acceso", Toast.LENGTH_SHORT).show();
                    }

                    public void onFinish() {

                        sendMessageBluetooth("0");
                        countdown.setText("Fine!");
                        Toast.makeText(getBaseContext(), "Fine!", Toast.LENGTH_SHORT).show();
                    }
                };

                cd.start();

            }
        });


        spegni.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                sendMessageBluetooth("0");

                try {
                    cd.cancel();
                }
                catch (Exception e){
                    Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show();
                    Log.e("Countdown_Error", e.toString());
                }

                countdown.setText("Spento");
                Toast.makeText(getBaseContext(), "Spento", Toast.LENGTH_SHORT).show();

            }

        });

    }

    private void startBtConnection(){

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null){//controlla se il devices è supportato{
            // IL BLUETOOTH NON E' SUPPORTATO
            Toast.makeText(MainActivity.this, "BlueTooth non supportato", Toast.LENGTH_LONG).show();
        }
        else{
            if (!mBluetoothAdapter.isEnabled())//controlla che sia abilitato il devices
            {
                //  NON E' ABILITATO IL BLUETOOTH
                Toast.makeText(MainActivity.this, "BlueTooth non abilitato", Toast.LENGTH_LONG).show();
            }
            else{
                //  IL BLUETOOTH E' ABILITATO
                mmDevice=mBluetoothAdapter.getRemoteDevice(arduino_MAC);//MAC address del bluetooth di arduino
                try{
                    mmSocket=mmDevice.createRfcommSocketToServiceRecord(uuid);
                }
                catch (IOException e){
                    Log.e("IOException", e.toString());
                }
                try{
                    // CONNETTE IL DISPOSITIVO TRAMITE IL SOCKET mmSocket
                    mmSocket.connect();
                    outStream = mmSocket.getOutputStream();
                    Toast.makeText(MainActivity.this, "Bluetooth connesso",  Toast.LENGTH_SHORT).show();//bluetooth è connesso
                }
                catch (IOException closeException){
                    try{
                        //TENTA DI CHIUDERE IL SOCKET
                        mmSocket.close();
                    }
                    catch (IOException ceXC){
                    }
                    Toast.makeText(MainActivity.this, "connessione non riuscita",  Toast.LENGTH_SHORT).show();
                }
            }   //CHIUDE l'else di isEnabled
        }
    }
    private void sendMessageBluetooth(String message) {

        startBtConnection();

        if (outStream == null)
        {
            return;
        }
        byte[] msgBuffer = message.getBytes();
        try
        {
            outStream.write(msgBuffer);
            Toast.makeText(MainActivity.this, "Messaggio BT inviato", Toast.LENGTH_SHORT).show();

        }
        catch (IOException e)
        {
            Toast.makeText(MainActivity.this, "Messaggio BT non inviato", Toast.LENGTH_SHORT).show();
        }
    }


}
