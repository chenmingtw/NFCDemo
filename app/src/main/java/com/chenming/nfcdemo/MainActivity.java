package com.chenming.nfcdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnReader = findViewById(R.id.buttonReader);
        Button btnWriter = findViewById(R.id.buttonWriter);
        Button btnPushNdef = findViewById(R.id.buttonPushNDEF);
        Button btnCardEmu = findViewById(R.id.buttonCardEmu);

        btnReader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, ReaderActivity.class);
                startActivity(intent);
            }
        });

        btnWriter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, WriterActivity.class);
                startActivity(intent);
            }
        });

        btnPushNdef.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, PushNdefActivity.class);
                startActivity(intent);
            }
        });
    }
}
