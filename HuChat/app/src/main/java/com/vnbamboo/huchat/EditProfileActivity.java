package com.vnbamboo.huchat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import java.io.FileNotFoundException;
import java.io.InputStream;

import static com.vnbamboo.huchat.ServiceConnection.mSocket;
import static com.vnbamboo.huchat.ServiceConnection.thisUser;
import static com.vnbamboo.huchat.Utility.CLIENT_SEND_IMAGE;
import static com.vnbamboo.huchat.Utility.REQUEST_CHOOSE_PHOTO;
import static com.vnbamboo.huchat.Utility.REQUEST_TAKE_PHOTO;
import static com.vnbamboo.huchat.Utility.getByteArrayFromBitmap;
import static com.vnbamboo.huchat.Utility.resize;

public class EditProfileActivity extends AppCompatActivity {
    Button btnBack;
    ImageView imgAvatar;
    Bitmap img = null;
    LayoutInflater inflater;
    ImageView imgAvatemp;
    @Override
    protected void onCreate( @Nullable Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        getSupportActionBar().hide();
        getWindow().setStatusBarColor(getColor(R.color.lightestGreenColor));
        inflater = this.getLayoutInflater();
        addControl();
        addEvent();

    }
    private void addControl(){
        btnBack = (Button) findViewById(R.id.btnBack);
        imgAvatar = (ImageView) findViewById(R.id.imgViewAvatar);
        imgAvatar.setImageBitmap(thisUser.getAvatar());
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
                AlertDialog.Builder dialogBuilder =	new AlertDialog.Builder(v.getContext());
                @SuppressLint("ResourceType") View dialogView	= inflater.inflate(R.layout.dialog_edit_profile_image_layout, (ViewGroup)findViewById(R.layout.activity_edit_profile));

                imgAvatemp = dialogView.findViewById(R.id.imgViewAvatar);
                imgAvatemp.setImageBitmap(thisUser.getAvatar());

                Button btnChooseImage = dialogView.findViewById(R.id.btnChooseImage);
                Button btnTakeImage = dialogView.findViewById(R.id.btnTakeImage);
                Button btnSave = dialogView.findViewById(R.id.btnSave);

                btnChooseImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick( View v ) {
                        choosePicture();
                    }
                });
                btnTakeImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick( View v ) {
                        takePicture();
                    }
                });

                btnSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick( View v ) {
                        if(img != null) {
                            byte[] bytes = getByteArrayFromBitmap(img);
                            mSocket.emit(CLIENT_SEND_IMAGE, bytes);
                            thisUser.setAvatar(img);
                            imgAvatar.setImageBitmap(img);
                        }
                    }
                });
                dialogBuilder.setView(dialogView);
                AlertDialog b = dialogBuilder.create();
                b.show();
//                choosePicture();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Boolean success = false;
        if(requestCode == REQUEST_CHOOSE_PHOTO && resultCode == RESULT_OK){
            try {
                Uri imageURI = data.getData();
                InputStream is = getContentResolver().openInputStream(imageURI);
                Bitmap bm = BitmapFactory.decodeStream(is);
                bm = resize(bm, 300, 300);
                img = bm;
                success = true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }else if(requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK){
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            bitmap = resize(bitmap, 300, 300);
            img = bitmap;
            success = true;
        }
        if (success)
            imgAvatemp.setImageBitmap(img);
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
