package com.chenming.nfcdemo;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.TextView;

public abstract class FilterActivity extends Activity {
    private final String TAG = "FilterActivity";

    private TextView tType;
    private TextView tPayload;
    private TextView tTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        tType = findViewById(R.id.textViewType);
        tPayload = findViewById(R.id.textViewPayload);
        tTag = findViewById(R.id.textViewTag);

        setType(getLocalClassName());
        getDataFromIntent(getIntent());
    }

    public void setType(String type) {
        tType.setText(type);
    }

    public void setPayload(String payload) {
        String mPayload = getString(R.string.msg_prefix_payload) + payload;
        tPayload.setText(mPayload);
    }

    public void setTag(String tag) {
        tTag.setText(tag);
    }

    private void getDataFromIntent(Intent intent) {
        if (intent != null && NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Parcelable[] rawMessages =
                    intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMessages != null) {
                NdefMessage[] messages = new NdefMessage[rawMessages.length];
                for (int i = 0; i < rawMessages.length; i++) {
                    messages[i] = (NdefMessage) rawMessages[i];
                }
                // Process the messages array.
                String payload = new String(messages[0].getRecords()[0].getPayload());
                setPayload(payload);
            }

            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if(tag != null) {
                setTag(tag.toString());
            }
        }
    }
}
