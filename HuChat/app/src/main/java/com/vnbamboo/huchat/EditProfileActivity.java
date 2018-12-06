package com.vnbamboo.huchat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.FileNotFoundException;
import java.io.InputStream;

import static com.vnbamboo.huchat.ServiceConnection.mSocket;
import static com.vnbamboo.huchat.Utility.CLIENT_SEND_IMAGE;
import static com.vnbamboo.huchat.Utility.REQUEST_CHOOSE_PHOTO;
import static com.vnbamboo.huchat.Utility.REQUEST_TAKE_PHOTO;
import static com.vnbamboo.huchat.Utility.getByteArrayFromBitmap;
import static com.vnbamboo.huchat.Utility.resize;

public class EditProfileActivity extends AppCompatActivity {
    Button btnBack;
    ImageView imgAvatar;
    @Override
    protected void onCreate( @Nullable Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        getSupportActionBar().hide();
        getWindow().setStatusBarColor(getColor(R.color.lightestGreenColor));

        addControl();
        addEvent();

    }
    private void addControl(){
        btnBack = (Button) findViewById(R.id.btnBack);
        imgAvatar = (ImageView) findViewById(R.id.imgViewAvatar);
    }

    private void addEvent(){
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                onBackPressed();
            }
        });

        imgAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                choosePicture();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CHOOSE_PHOTO && resultCode == RESULT_OK){

            try {
                Uri imageURI = data.getData();
                InputStream is = getContentResolver().openInputStream(imageURI);
                Bitmap bm = BitmapFactory.decodeStream(is);
                bm = resize(bm, 100, 100);
                byte[] bytes = getByteArrayFromBitmap(bm);
                mSocket.emit(CLIENT_SEND_IMAGE, bytes);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }else if(requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK){
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            bitmap = resize(bitmap, 100, 100);
            byte[] bytes = getByteArrayFromBitmap(bitmap);
            mSocket.emit(CLIENT_SEND_IMAGE, bytes);
        }
    }

    private void takePicture(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_TAKE_PHOTO);
    }

    private void choosePicture(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CHOOSE_PHOTO);
    }
}
