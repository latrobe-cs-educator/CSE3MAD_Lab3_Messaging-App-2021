package com.example.lab32021update;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    //UX Variables
    Button selectButton = null;
    Button sendButton = null;
    TextView numView = null;

    //Data variables
    private String contactNumber = null;

    //Required permissions array
    final String[] PERMISSIONS = {Manifest.permission.READ_CONTACTS, Manifest.permission.SEND_SMS};

    //Debug
    String TAG = "TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Bind views
        selectButton = (Button) findViewById(R.id.selectContactBtn);
        sendButton = (Button) findViewById(R.id.sendButton);
        numView = (TextView) findViewById(R.id.SMSTV);

        //make send button inactive
        sendButton.setEnabled(false);

        //ask user for outstanding permissions
        askPermissions();

        //Button listener
        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //First we need to check permissions
                if (!hasPermissions()) {
                    Log.d(TAG, "Permission denied");
                    Toast.makeText(getApplicationContext(), "Insufficent permissions to use app", Toast.LENGTH_SHORT).show();

                } else {
                    //launch contact picker as we have permission
                    Log.d(TAG, "App has permission");
                    mStartForResult.launch(null);
                }
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(MainActivity.this, SendActivity.class);
                myIntent.putExtra("contact_num", contactNumber);
                MainActivity.this.startActivity(myIntent);
            }
        });
    }

    //helper function to check permission status
    private boolean hasPermissions() {
        boolean permissionStatus = true;
        for (String permission : PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permission is granted: " + permission);
            } else {
                Log.d(TAG, "Permission is not granted: " + permission);
                permissionStatus = false;
            }
        }
        return permissionStatus;
    }

    //helper function to ask user permissions
    private void askPermissions() {
        if (!hasPermissions()) {
            Log.d(TAG, "Launching multiple contract permission launcher for ALL required permissions");
            multiplePermissionActivityResultLauncher.launch(PERMISSIONS);
        } else {
            Log.d(TAG, "All permissions are already granted");
        }
    }

    //Result launcher for permissions
    private final ActivityResultLauncher<String[]> multiplePermissionActivityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), isGranted -> {
                Log.d(TAG, "Launcher result: " + isGranted.toString());
                if (isGranted.containsValue(false)) {
                    Log.d(TAG, "At least one of the permissions was not granted, please enable permissions to ensure app functionality");
                }
            });


    //Result launcher for contact picker
    ActivityResultLauncher<Void> mStartForResult = registerForActivityResult(
            new ActivityResultContracts.PickContact(), new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri contactUri) {
                    if (contactUri != null) {
                        Cursor cursor = getContentResolver().query(contactUri,
                                null,
                                null,
                                null,
                                null);
                        if (cursor != null && cursor.moveToFirst()) {
                            //First we get the user details
                            String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                            String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                            //Then we check if the user has a phone number to retrieve
                            String idResults = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                            int idResultValue = Integer.parseInt(idResults);
                            Log.d(TAG, contactId + " " + contactName + " " + idResults);
                            cursor.close();
                            //If the user had a phone number we can then retrieve it
                            if (idResultValue == 1) // if the user has a phone number result is 1
                            {
                                //create a new cursor to run query that checks if selected Contact has a phone number
                                Cursor cursor2 = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                        null,
                                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId,
                                        null,
                                        null
                                );
                                //a contact may have multiple phone numbers currently we are just
                                // selecting the last one but you could add condition per requirement
                                while (cursor2.moveToNext()) {
                                    //get phone number
                                    contactNumber = cursor2.getString(cursor2.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                    Log.d(TAG, contactNumber);
                                    //set numView text to retrieved number
                                    numView.setText(contactNumber);
                                    //enable send button now that a phone number has been retrieved
                                    if (contactNumber != null && contactNumber.length() > 0) {
                                        sendButton.setEnabled(true);
                                    } else {
                                        sendButton.setEnabled(false);
                                    }
                                }
                                cursor2.close();
                            }
                        }
                    }
                }
            });
}