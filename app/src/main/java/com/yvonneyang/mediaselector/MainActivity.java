package com.yvonneyang.mediaselector;

import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import yvonne.mediaselector_lib.activity.MediaChooserBucketActivity;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initActionBar();
        findViewById(R.id.choose_local_video_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MediaChooserBucketActivity.class);
                startActivityForResult(intent, 1001);
            }
        });

    }

    private void initActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar == null) {
            return;
        }
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        toolbar.setTitle("");// 标题的文字需在setSupportActionBar之前，不然会无效
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.actionbar_main_activity);
        View topView = findViewById(R.id.top_view);
        if (topView != null) {
            topView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }
        View actionBarView = getSupportActionBar().getCustomView();
        actionBarView.findViewById(R.id.img_Back).setOnClickListener(this);
        ((TextView) actionBarView.findViewById(R.id.title)).setText("");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001) {
            if (data != null && data.getStringExtra("path") != null) {
                ((TextView) findViewById(R.id.title_tv)).setText(data.getStringExtra("path"));
            }
        }
    }

    @Override
    public void onClick(View view) {
        finish();
    }
}
