package com.example.lab32021update;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SendActivity extends AppCompatActivity {

    //UX variables
    private Button msgBtn = null;
    private EditText msgText = null;
    //Other
    private String contact_number = null;
    private String message = null;
    static final int SMS_PERMISSION_REQ = 123; // PERMISSIONS VALUE

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        //Bind views
        msgBtn = (Button) findViewById(R.id.sendMsgButton);
        msgText = (EditText) findViewById(R.id.smstext);
        //Get data from intent
        Intent intent = getIntent();
        contact_number = intent.getStringExtra("contact_num");

        //Button listener
        msgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
    }

    private void sendMessage() {
        message = msgText.getText().toString();
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(contact_number, null, message, null, null);
        Toast.makeText(SendActivity.this, "Message Sent", Toast.LENGTH_LONG).show();
    }
}