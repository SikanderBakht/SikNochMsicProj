package com.hellodemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;

public class DialogActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(new ColorDrawable(0));
           LayoutInflater li = LayoutInflater.from(getApplicationContext());
                View promptsView = li.inflate(R.layout.bottomsheet_groupsettings, null);

                //promptsView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                BottomSheetDialog dialog = new BottomSheetDialog(this);
                dialog.setContentView(promptsView);
                dialog.show();
    }
}