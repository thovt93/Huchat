package com.vnbamboo.huchat;

import com.vnbamboo.huchat.fragment.FriendFragment;
import com.vnbamboo.huchat.fragment.MessageFragment;
import com.vnbamboo.huchat.fragment.ProfileFragment;
import com.vnbamboo.huchat.helper.BottomNavigationBehavior;
import com.vnbamboo.huchat.object.Room;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import static com.vnbamboo.huchat.ServiceConnection.mSocket;
import static com.vnbamboo.huchat.ServiceConnection.resultFromSever;
import static com.vnbamboo.huchat.ServiceConnection.tempImage;
import static com.vnbamboo.huchat.ServiceConnection.thisUser;
import static com.vnbamboo.huchat.Utility.CLIENT_REQUEST_IMAGE_ROOM;
import static com.vnbamboo.huchat.Utility.CLIENT_REQUEST_IMAGE_USER;
import static com.vnbamboo.huchat.Utility.CLIENT_REQUEST_LIST_ROOM;
import static com.vnbamboo.huchat.Utility.SERVER_SEND_IMAGE_ROOM;


public class MainActivity extends AppCompatActivity {

    @Override
    public void onBackPressed() {

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        mSocket.emit(CLIENT_REQUEST_IMAGE_USER, thisUser.getUserName());
        mSocket.emit(CLIENT_REQUEST_LIST_ROOM, thisUser.getUserName());
        for (final Room room:thisUser.getRoomList()){
            mSocket.emit(CLIENT_REQUEST_IMAGE_ROOM, room.getRoomCode());
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(resultFromSever.event.equals(SERVER_SEND_IMAGE_ROOM)){
                        if(tempImage != null)
                            thisUser.getRoomAt(thisUser.getIndexRoomCode(room.getRoomCode())).setAvatar(tempImage);
                    }
                }
            },300);
        }
        // attaching bottom sheet behaviour - hide / show on scroll
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) navigation.getLayoutParams();
        layoutParams.setBehavior(new BottomNavigationBehavior());

        // load the store fragment by default
        loadFragment(new MessageFragment());
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment;
            switch (item.getItemId()) {
                case R.id.navigation_gifts:
                    fragment = new MessageFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.navigation_cart:
                    fragment = new FriendFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.navigation_profile:
                    fragment = new ProfileFragment();
                    loadFragment(fragment);
                    return true;
            }

            return false;
        }
    };

    private void loadFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    protected void onDestroy() {
        Intent intent = new Intent(MainActivity.this, ServiceConnection.class);
        this.stopService(intent);
        super.onDestroy();
    }
}
