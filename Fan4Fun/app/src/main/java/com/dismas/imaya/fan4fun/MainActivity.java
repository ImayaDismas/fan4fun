package com.dismas.imaya.fan4fun;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        addListenerOnButton();
        addListenerOnButton1();
    }
    public void addListenerOnButton() {

        final Context context = this;

        button = (Button) findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                Intent intent = new Intent(context, MainSoundCloud.class);
                startActivity(intent);

            }

        });

    }
    public void addListenerOnButton1() {

        final Context context = this;

        button = (Button) findViewById(R.id.button1);

        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                Intent intent = new Intent(context, VideoItemListActivity.class);
                startActivity(intent);

            }

        });

    }
}
