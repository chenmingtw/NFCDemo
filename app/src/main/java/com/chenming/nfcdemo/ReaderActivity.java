package com.chenming.nfcdemo;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Set;

public class ReaderActivity extends Activity {
    private final String TAG = "ReaderActivity";
    private SharedPreferences sharedPreferences;
    private NfcAdapter mNfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] intentFiltersArray;
    private String[][] techListsArray;
    private TextView textViewMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

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
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Get NFC tech list from SharedPreferences.
        Set<String> techSet = sharedPreferences.getStringSet(getString(R.string.readersettings_nfc_tech_list_key), null);
        if(techSet != null) {
            ArrayList<String> techListsArrayList = new ArrayList<>(techSet);
            String[][] newTechListsArray = new String[techListsArrayList.size()][];

            for (int i = 0; i < newTechListsArray.length; i++) {
                newTechListsArray[i] = new String[] { getNfcTechClassName(techListsArrayList.get(i)) };
            }
            techListsArray = newTechListsArray;
        } else {
            techListsArray = new String[][] {};
        }

        for (int i = 0; i < techListsArray.length; i++) {
            for (int j = 0; j < techListsArray[i].length; j++) {
                Log.i(TAG, "techListsArray = " + techListsArray[i][j]);
            }
        }

        // Enable Reader
        if(mNfcAdapter != null) {
            mNfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Disable Reader
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.reader, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.settings) {
            Intent intent = new Intent();
            intent.setClass(this, ReaderSettingsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    private void printTimestamp() {
        Calendar calendar = Calendar.getInstance();
        String timeNow = calendar.getTime().toString();
        textViewMsg.append(timeNow + "\n");
    }

    private void printSpacer() {
        textViewMsg.append("\n====================\n");     // 20 '='
    }

    private String getNfcTechClassName(String name) {
        switch (name) {
            case "IsoDep":
                return IsoDep.class.getName();
            case "NfcA":
                return NfcA.class.getName();
            case "NfcB":
                return NfcB.class.getName();
            case "NfcF":
                return NfcF.class.getName();
            case "NfcV":
                return NfcV.class.getName();
            case "Ndef":
                return Ndef.class.getName();
            case "NdefFormatable":
                return NdefFormatable.class.getName();
            case "MifareClassic":
                return MifareClassic.class.getName();
            case "MifareUltralight":
                return MifareUltralight.class.getName();
            default:
                break;
        }

        return null;
    }
}
