package com.example.fakesmshelper;

import android.app.Activity;
import android.app.role.RoleManager;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int REQUEST_SMS_ROLE = 401;
    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showUi();
        handleIntent(getIntent());
    }

    private void showUi() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(36, 36, 36, 36);

        status = new TextView(this);
        status.setText("Fake SMS Helper\n\nSet this app as the default SMS app to insert test messages.");
        status.setTextSize(18);
        layout.addView(status);

        Button roleButton = new Button(this);
        roleButton.setText("Set as default SMS app");
        roleButton.setOnClickListener(v -> requestSmsRole());
        layout.addView(roleButton);

        Button insertButton = new Button(this);
        insertButton.setText("Insert sample test SMS");
        insertButton.setOnClickListener(v -> insertSms(
                "5550101234", "TEST ONLY: Sample SMS inserted by Fake SMS Helper",
                System.currentTimeMillis(), false));
        layout.addView(insertButton);

        setContentView(layout);
    }

    private void requestSmsRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            RoleManager roleManager = getSystemService(RoleManager.class);
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_SMS)) {
                if (roleManager.isRoleHeld(RoleManager.ROLE_SMS)) {
                    status.setText("This app already holds the default SMS role.");
                } else {
                    startActivityForResult(
                            roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS),
                            REQUEST_SMS_ROLE);
                }
            } else {
                status.setText("SMS role is not available on this device.");
            }
        } else {
            status.setText("This sample targets Android 10+ role management.");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SMS_ROLE) {
            RoleManager roleManager = getSystemService(RoleManager.class);
            boolean held = roleManager != null && roleManager.isRoleHeld(RoleManager.ROLE_SMS);
            status.setText(held ? "Default SMS role granted." : "Default SMS role not granted.");
            Toast.makeText(this, status.getText(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent == null || !"com.example.fakesmshelper.INSERT_TEST_SMS".equals(intent.getAction())) {
            return;
        }
        String address = intent.getStringExtra("address");
        String body = intent.getStringExtra("body");
        long date = intent.getLongExtra("date", System.currentTimeMillis());
        boolean read = intent.getBooleanExtra("read", false);

        if (address == null || address.trim().isEmpty() || body == null) {
            Toast.makeText(this, "Provide address and body extras.", Toast.LENGTH_LONG).show();
            return;
        }
        insertSms(address, body, date, read);
    }

    private void insertSms(String address, String body, long date, boolean read) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            RoleManager roleManager = getSystemService(RoleManager.class);
            if (roleManager == null || !roleManager.isRoleHeld(RoleManager.ROLE_SMS)) {
                Toast.makeText(this, "Grant default SMS role first.", Toast.LENGTH_LONG).show();
                status.setText("Default SMS role required before inserting.");
                return;
            }
        }

        ContentValues values = new ContentValues();
        values.put(Telephony.Sms.ADDRESS, address);
        values.put(Telephony.Sms.BODY, body);
        values.put(Telephony.Sms.DATE, date);
        values.put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_INBOX);
        values.put(Telephony.Sms.READ, read ? 1 : 0);
        values.put(Telephony.Sms.SEEN, read ? 1 : 0);

        try {
            Uri result = getContentResolver().insert(Telephony.Sms.CONTENT_URI, values);
            if (result != null) {
                status.setText("Inserted test SMS. URI: " + result);
                Toast.makeText(this, "Test SMS inserted.", Toast.LENGTH_LONG).show();
            } else {
                status.setText("Provider returned no URI; insertion may have failed.");
                Toast.makeText(this, "Insert failed: no result URI.", Toast.LENGTH_LONG).show();
            }
        } catch (SecurityException e) {
            status.setText("Permission denied: " + e.getMessage());
            Toast.makeText(this, "Permission denied by SMS provider.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            status.setText("Insert error: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            Toast.makeText(this, status.getText(), Toast.LENGTH_LONG).show();
        }
    }
}
