package com.hellodemo;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Mahnoor on 09/05/2018.
 */

public class testingcamera extends AppCompatActivity {

    //@BindView(R.id.img_camera)
   // CircleImageView mImgCamera;

    private ChoosePhoto choosePhoto=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    //@OnClick(R.id.img_camera)
    public void onViewClicked() {
        choosePhoto = new ChoosePhoto(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ChoosePhoto.CHOOSE_PHOTO_INTENT) {
                if (data != null && data.getData() != null) {
                    choosePhoto.handleGalleryResult(data);
                } else {
                    choosePhoto.handleCameraResult(choosePhoto.getCameraUri());
                }
            }else if (requestCode == ChoosePhoto.SELECTED_IMG_CROP) {
      //          mImgCamera.setImageURI(choosePhoto.getCropImageUrl());
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ChoosePhoto.SELECT_PICTURE_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                choosePhoto.showAlertDialog();
        }
    }
}
