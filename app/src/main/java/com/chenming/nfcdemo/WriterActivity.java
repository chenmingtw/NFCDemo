package com.chenming.nfcdemo;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.Locale;

public class WriterActivity extends Activity {

    private NFCManager nfcManager;
    private NdefMessage message = null;
    private ProgressDialog dialog;
    private Spinner spinner;
    private EditText etContent;
    private Button btnWriting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writer);

        nfcManager = new NFCManager(this);

        spinner = findViewById(R.id.spTagType);
        etContent = findViewById(R.id.etContent);
        btnWriting = findViewById(R.id.btnWrite);

        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(this,
                R.array.writer_type, android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                etContent.setText("");

                switch (i) {
                    case 0:
                        etContent.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
                        break;
                    case 1:
                        etContent.setInputType(InputType.TYPE_CLASS_PHONE);
                        break;
                    case 2:
                        etContent.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btnWriting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = spinner.getSelectedItemPosition();
                String content = etContent.getText().toString();

                switch (pos) {
                    case 0:
                        message =  nfcManager.createUriMessage(content, NFCManager.WRITE_TYPE_URI);
                        break;
                    case 1:
                        message =  nfcManager.createUriMessage(content, NFCManager.WRITE_TYPE_TEL);
                        break;
                    case 2:
                        message =  nfcManager.createTextMessage(content);
                        break;
                }

                if (message != null) {
                    dialog = new ProgressDialog(WriterActivity.this);
                    dialog.setMessage(getString(R.string.writer_dialog_msg));
                    dialog.show();;
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            nfcManager.verifyNfcStatus();
            Intent nfcIntent = new Intent(this, getClass());
            nfcIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, nfcIntent, 0);
            IntentFilter[] intentFiltersArray = new IntentFilter[] {};
            String[][] techList = new String[][] {
                    { android.nfc.tech.Ndef.class.getName() },
                    { android.nfc.tech.NdefFormatable.class.getName() }
            };
            NfcAdapter nfcAdpt = NfcAdapter.getDefaultAdapter(this);
            nfcAdpt.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techList);
        }
        catch(NFCManager.NfcNotSupported nfcNSup) {
            Toast.makeText(this, R.string.msg_nfc_nonavailable, Toast.LENGTH_LONG).show();
        }
        catch(NFCManager.NfcNotEnabled nfcNEn) {
            Toast.makeText(this, R.string.msg_nfc_disable, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        nfcManager.disableDispatch();
    }

    @Override
    public void onNewIntent(Intent intent) {
        // It is the time to write the tag
        Tag currentTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (message != null) {
            nfcManager.writeTag(currentTag, message);
            dialog.dismiss();
            Toast.makeText(this, R.string.writer_toast_written, Toast.LENGTH_LONG).show();
        }
        else {
            // Handle intent

        }
    }

    public class NFCManager {
        public static final String WRITE_TYPE_URI = "http://";
        public static final String WRITE_TYPE_TEL = "tel:";

        private Activity activity;
        private NfcAdapter nfcAdapter;

        public NFCManager(Activity activity) {
            this.activity = activity;
        }

        public void verifyNfcStatus() throws NfcNotSupported, NfcNotEnabled {

            nfcAdapter = NfcAdapter.getDefaultAdapter(activity);

            if (nfcAdapter == null)
                throw new NfcNotSupported();

            if (!nfcAdapter.isEnabled())
                throw new NfcNotEnabled();
        }

        public void enableDispatch() {
            Intent nfcIntent = new Intent(activity, getClass());
            nfcIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, nfcIntent, 0);
            IntentFilter[] intentFiltersArray = new IntentFilter[] {};
            String[][] techList = new String[][] {
                    { android.nfc.tech.Ndef.class.getName() },
                    { android.nfc.tech.NdefFormatable.class.getName() }
            };

            nfcAdapter.enableForegroundDispatch(activity, pendingIntent, intentFiltersArray, techList);
        }

        public void disableDispatch() {
            nfcAdapter.disableForegroundDispatch(activity);
        }

        public class NfcNotSupported extends Exception {

            public NfcNotSupported() {
                super();
            }
        }

        public class NfcNotEnabled extends Exception {

            public NfcNotEnabled() {
                super();
            }
        }

        public void writeTag(Tag tag, NdefMessage message)  {
            if (tag != null) {
                try {
                    Ndef ndefTag = Ndef.get(tag);

                    if (ndefTag == null) {
                        // Let's try to format the Tag in NDEF
                        NdefFormatable nForm = NdefFormatable.get(tag);
                        if (nForm != null) {
                            nForm.connect();
                            nForm.format(message);
                            nForm.close();
                        }
                    }
                    else {
                        ndefTag.connect();
                        ndefTag.writeNdefMessage(message);
                        ndefTag.close();
                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public NdefMessage createUriMessage(String content, String type) {
            NdefRecord record = NdefRecord.createUri(type + content);
            return new NdefMessage(new NdefRecord[]{record});
        }

        public NdefMessage createTextMessage(String content) {
            try {
                // Get UTF-8 byte
                byte[] lang = Locale.getDefault().getLanguage().getBytes("UTF-8");
                byte[] text = content.getBytes("UTF-8"); // Content in UTF-8

                int langSize = lang.length;
                int textLength = text.length;

                ByteArrayOutputStream payload = new ByteArrayOutputStream(1 + langSize + textLength);
                payload.write((byte) (langSize & 0x1F));
                payload.write(lang, 0, langSize);
                payload.write(text, 0, textLength);
                NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload.toByteArray());
                return new NdefMessage(new NdefRecord[]{record});
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        public NdefMessage createExternalMessage(String domain, String type, byte[] data) {
            NdefRecord externalRecord = NdefRecord.createExternal(domain, type, data);
            return new NdefMessage(new NdefRecord[] { externalRecord });
        }
    }
}
