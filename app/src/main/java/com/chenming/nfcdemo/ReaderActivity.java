package com.chenming.nfcdemo;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.TextView;

import java.util.Calendar;

public class ReaderActivity extends Activity {
    private NfcAdapter mNfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] intentFiltersArray;
    private String[][] techListsArray;
    private TextView textViewMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);

        textViewMsg = findViewById(R.id.textViewReaderMsg);

        // Check for available NFC Adapter
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            String nfcStatus = "\n" + getString(R.string.msg_nfc_nonavailable);
            textViewMsg.setText(nfcStatus);
        } else if (!mNfcAdapter.isEnabled()) {
            String nfcStatus = "\n" + getString(R.string.msg_nfc_disable);
            textViewMsg.setText(nfcStatus);
        } else {
            String nfcStatus = "\n" + getString(R.string.reader_msg_default);
            textViewMsg.setText(nfcStatus);
            printSpacer();
        }

        pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndef.addDataType("*/*");    /* Handles all MIME based dispatches.
                                       You should specify only the ones that you need. */
        }
        catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }
        intentFiltersArray = new IntentFilter[] {ndef, };
        techListsArray = new String[][] { new String[] { NfcF.class.getName() },
                new String[] { MifareClassic.class.getName() } };
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(mNfcAdapter != null) {
            mNfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        printTimestamp();

        if (intent != null) {
            if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
                textViewMsg.append("Intent: ACTION_NDEF_DISCOVERED\n");

                Parcelable[] rawMessages =
                        intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
                if (rawMessages != null) {
                    NdefMessage[] messages = new NdefMessage[rawMessages.length];
                    for (int i = 0; i < rawMessages.length; i++) {
                        messages[i] = (NdefMessage) rawMessages[i];
                    }
                    // Process the messages array.
                    String payload = new String(messages[0].getRecords()[0].getPayload());
                    textViewMsg.append(getString(R.string.msg_prefix_payload) + payload + "\n");
                }
            } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
                textViewMsg.append("Intent: ACTION_TECH_DISCOVERED\n");

                Parcelable[] rawMessages =
                        intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
                if (rawMessages != null) {
                    NdefMessage[] messages = new NdefMessage[rawMessages.length];
                    for (int i = 0; i < rawMessages.length; i++) {
                        messages[i] = (NdefMessage) rawMessages[i];
                    }
                    // Process the messages array.
                    String payload = new String(messages[0].getRecords()[0].getPayload());
                    textViewMsg.append(getString(R.string.msg_prefix_payload) + payload + "\n");
                }
            } else if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
                textViewMsg.append("Intent: ACTION_TAG_DISCOVERED\n");
            }

            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            textViewMsg.append(tag.toString());
        }

        printSpacer();
    }

    private void printTimestamp() {
        Calendar calendar = Calendar.getInstance();
        String timeNow = calendar.getTime().toString();
        textViewMsg.append(timeNow + "\n");
    }

    private void printSpacer() {
        textViewMsg.append("\n====================\n");     // 20 '='
    }
}
