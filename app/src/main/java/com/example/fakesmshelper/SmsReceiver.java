package com.example.fakesmshelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Required SMS_DELIVER role component placeholder for SMS role eligibility.
        // This helper is for locally inserted test records, not receiving real SMS.
    }
}
