package com.example.fakesmshelper;

import android.app.Activity;
import android.app.role.RoleManager;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.Manifest;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int ROLE_REQUEST = 700;
    private static final int PERMISSION_REQUEST = 701;
    private TextView status;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(36, 36, 36, 36);

        status = new TextView(this);
        status.setText("FakeSmsHelper CallLog\n\nGrant default Dialer role, then add a local test call entry.");
        layout.addView(status);

        Button roleButton = new Button(this);
        roleButton.setText("Set as default dialer");
        roleButton.setOnClickListener(v -> requestDialerRole());
        layout.addView(roleButton);

        Button missedButton = new Button(this);
        missedButton.setText("Insert test missed call");
        missedButton.setOnClickListener(v -> insertCall("9876543210", CallLog.Calls.MISSED_TYPE, 0));
        layout.addView(missedButton);

        Button incomingButton = new Button(this);
        incomingButton.setText("Insert test incoming call");
        incomingButton.setOnClickListener(v -> insertCall("9876543210", CallLog.Calls.INCOMING_TYPE, 42));
        layout.addView(incomingButton);

        Button outgoingButton = new Button(this);
        outgoingButton.setText("Insert test outgoing call");
        outgoingButton.setOnClickListener(v -> insertCall("9876543210", CallLog.Calls.OUTGOING_TYPE, 35));
        layout.addView(outgoingButton);

        setContentView(layout);
    }

    private void requestDialerRole() {
        RoleManager rm = (RoleManager) getSystemService(ROLE_SERVICE);
        if (rm != null && rm.isRoleAvailable(RoleManager.ROLE_DIALER)) {
            if (rm.isRoleHeld(RoleManager.ROLE_DIALER)) {
                status.setText("Default Dialer role is already granted.");
                requestCallLogPermissions();
            } else {
                startActivityForResult(rm.createRequestRoleIntent(RoleManager.ROLE_DIALER), ROLE_REQUEST);
            }
        } else {
            status.setText("Default Dialer role is unavailable on this device.");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ROLE_REQUEST) {
            RoleManager rm = (RoleManager) getSystemService(ROLE_SERVICE);
            if (rm != null && rm.isRoleHeld(RoleManager.ROLE_DIALER)) {
                status.setText("Default Dialer role granted.");
                requestCallLogPermissions();
            } else {
                status.setText("Default Dialer role not granted.");
            }
        }
    }

    private void requestCallLogPermissions() {
        if (checkSelfPermission(Manifest.permission.WRITE_CALL_LOG) != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CALL_LOG, Manifest.permission.WRITE_CALL_LOG}, PERMISSION_REQUEST);
        } else {
            status.setText("Call log permissions granted. Ready for local test entries.");
        }
    }

    private void insertCall(String number, int type, int duration) {
        if (checkSelfPermission(Manifest.permission.WRITE_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Grant Dialer role and call-log permission first.", Toast.LENGTH_LONG).show();
            requestCallLogPermissions();
            return;
        }
        ContentValues values = new ContentValues();
        values.put(CallLog.Calls.NUMBER, number);
        values.put(CallLog.Calls.DATE, System.currentTimeMillis());
        values.put(CallLog.Calls.DURATION, duration);
        values.put(CallLog.Calls.TYPE, type);
        values.put(CallLog.Calls.NEW, 1);
        values.put(CallLog.Calls.IS_READ, 0);
        values.put(CallLog.Calls.PHONE_ACCOUNT_ID, (String) null);
        Uri result = getContentResolver().insert(CallLog.Calls.CONTENT_URI, values);
        if (result != null) {
            status.setText("Inserted local test call: " + number + " (type " + type + ")");
            Toast.makeText(this, "Test call log inserted", Toast.LENGTH_SHORT).show();
        } else {
            status.setText("Call log insert returned no record. MIUI may restrict writes.");
        }
    }
}
