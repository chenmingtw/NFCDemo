package com.chenming.nfcdemo;

import android.app.Activity;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Locale;

public class PushNdefActivity extends Activity implements NfcAdapter.CreateNdefMessageCallback {
    private NfcAdapter mNfcAdapter;
    private NdefRecord ndefRecord;
    private String ndefType;
    private String ndefMsg;

    private TextView textViewMsg;
    private Button btnAbsUri;
    private Button btnMime;
    private Button btnText;
    private Button btnUri;
    private Button btnExt;
    private Button btnAAR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_ndef);

        textViewMsg = findViewById(R.id.textViewMsg);
        btnAbsUri = findViewById(R.id.buttonAbsUri);
        btnMime = findViewById(R.id.buttonMime);
        btnText = findViewById(R.id.buttonText);
        btnUri = findViewById(R.id.buttonUri);
        btnExt = findViewById(R.id.buttonExt);
        btnAAR = findViewById(R.id.buttonAAR);

        btnAbsUri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ndefRecord = createAbsoluteUriRecord();
                ndefType = btnAbsUri.getText().toString();
                updateNdefMsgForType();
            }
        });

        btnMime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ndefRecord = createMimeRecord(false);
                ndefType = btnMime.getText().toString();
                updateNdefMsgForType();
            }
        });

        btnText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ndefRecord = createTextRecord(ndefMsg, Locale.getDefault(), true);
                ndefType = btnText.getText().toString();
                updateNdefMsgForType();
            }
        });

        btnUri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ndefRecord = createUriRecord(false);
                ndefType = btnUri.getText().toString();
                updateNdefMsgForType();
            }
        });

        btnExt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ndefRecord = createExternalRecord(ndefMsg, false);
                ndefType = btnExt.getText().toString();
                updateNdefMsgForType();
            }
        });

        btnAAR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ndefRecord = createAAR();
                ndefType = btnAAR.getText().toString();
                updateNdefMsgForType();
            }
        });

        initData();

        // Check for available NFC Adapter
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            String nfcStatus = getString(R.string.msg_nfc_nonavailable);
            textViewMsg.setText(nfcStatus);
            return;
        } else if (!mNfcAdapter.isEnabled()) {
            String nfcStatus = getString(R.string.msg_nfc_disable);
            textViewMsg.setText(nfcStatus);
            return;
        } else {
            updateNdefMsgForType();
        }

        mNfcAdapter.setNdefPushMessageCallback(this, this);
    }

    private void initData() {
        ndefRecord = createAbsoluteUriRecord();
        ndefType = btnAbsUri.getText().toString();
        ndefMsg = "Hello! NDEF Message!";
    }

    private void updateNdefMsgForType() {
        String nfcStatus = getString(R.string.push_msg_default) + ndefType;
        textViewMsg.setText(nfcStatus);
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent nfcEvent) {
        NdefMessage msg = new NdefMessage(new NdefRecord[] { ndefRecord });
        return msg;
    }

    private void printTimestamp() {
        Calendar calendar = Calendar.getInstance();
        String timeNow = calendar.getTime().toString();
        textViewMsg.append(timeNow + "\n");
    }

    private void printSpacer() {
        textViewMsg.append("\n====================\n");     // 20 '='
    }

    /**
     * TNF_ABSOLUTE_URI
     * @return
     */
    private NdefRecord createAbsoluteUriRecord() {
        NdefRecord uriRecord = new NdefRecord(
                NdefRecord.TNF_ABSOLUTE_URI ,
                "http://developer.android.com/index.html".getBytes(Charset.forName("US-ASCII")),
                new byte[0], new byte[0]);

        return uriRecord;
    }

    /**
     * TNF_MIME_MEDIA
     * @param manually
     * @return
     */
    private NdefRecord createMimeRecord(boolean manually) {
        // Using the createMime() method
        NdefRecord mimeRecord1 = NdefRecord.createMime("application/vnd.com.example.android.beam",
                "Beam me up, Android".getBytes(Charset.forName("US-ASCII")));

        // Creating the NdefRecord manually
        NdefRecord mimeRecord2 = new NdefRecord(
                NdefRecord.TNF_MIME_MEDIA ,
                "application/vnd.com.example.android.beam".getBytes(Charset.forName("US-ASCII")),
                new byte[0], "Beam me up, Android!".getBytes(Charset.forName("US-ASCII")));

        return manually ? mimeRecord2 : mimeRecord1;
    }

    /**
     * TNF_WELL_KNOWN_with_RTD_TEXT
     * @param payload
     * @param locale
     * @param encodeInUtf8
     * @return
     */
    private NdefRecord createTextRecord(String payload, Locale locale, boolean encodeInUtf8) {
        byte[] langBytes = locale.getLanguage().getBytes(Charset.forName("US-ASCII"));
        Charset utfEncoding = encodeInUtf8 ? Charset.forName("UTF-8") : Charset.forName("UTF-16");
        byte[] textBytes = payload.getBytes(utfEncoding);
        int utfBit = encodeInUtf8 ? 0 : (1 << 7);
        char status = (char) (utfBit + langBytes.length);
        byte[] data = new byte[1 + langBytes.length + textBytes.length];
        data[0] = (byte) status;
        System.arraycopy(langBytes, 0, data, 1, langBytes.length);
        System.arraycopy(textBytes, 0, data, 1 + langBytes.length, textBytes.length);
        NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                NdefRecord.RTD_TEXT, new byte[0], data);
        return record;
    }

    /**
     * TNF_WELL_KNOWN_with_RTD_TEXT
     * @param manually
     * @return
     */
    private NdefRecord createUriRecord(boolean manually) {
        // Using the createUri(String) method
        NdefRecord rtdUriRecord1 = NdefRecord.createUri("http://example.com");

        // Creating the NdefRecord manually
        byte[] uriField = "example.com".getBytes(Charset.forName("US-ASCII"));
        byte[] payload = new byte[uriField.length + 1];              //add 1 for the URI Prefix
        payload[0] = 0x01;                                      //prefixes http://www. to the URI
        System.arraycopy(uriField, 0, payload, 1, uriField.length);  //appends URI to payload
        NdefRecord rtdUriRecord2 = new NdefRecord(
                NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_URI, new byte[0], payload);

        return manually ? rtdUriRecord2 : rtdUriRecord1;
    }

    /**
     * TNF_EXTERNAL_TYPE
     * @param payload
     * @param manually
     * @return
     */
    private NdefRecord createExternalRecord(String payload, boolean manually) {
        // Using the createExternal() method
        byte[] payloadBytes = payload.getBytes(); //assign to your data
        String domain = "com.example"; //usually your app's package name
        String type = "externalType";
        NdefRecord extRecord1 = NdefRecord.createExternal(domain, type, payloadBytes);

        // Creating the NdefRecord manually
        NdefRecord extRecord2 = new NdefRecord(
                NdefRecord.TNF_EXTERNAL_TYPE, "com.example:externalType".getBytes(), new byte[0], payloadBytes);

        return manually ? extRecord2 : extRecord1;
    }

    /**
     * Android Application Records
     * @return
     */
    private NdefRecord createAAR() {
        return NdefRecord.createApplicationRecord("com.example.android.beam");
    }
}
