package com.vnbamboo.huchat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import static com.vnbamboo.huchat.ServiceConnection.mSocket;
import static com.vnbamboo.huchat.ServiceConnection.resultFromSever;
import static com.vnbamboo.huchat.ServiceConnection.statusConnecttion;
import static com.vnbamboo.huchat.Utility.LOGIN;
import static com.vnbamboo.huchat.Utility.toSHA256;

public class LoginActivity extends AppCompatActivity {

    boolean doubleBackToExitPressedOnce = false;
    Button btnLogin, btnRegister;
    TextView txtUserName;
    TextView txtPassword, txtConnectionState;
    CheckBox cbxRememberPass;
    Context thisContext = this;
    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

        getWindow().setStatusBarColor(getColor(R.color.lightGreenColor));

        Intent intent = new Intent(LoginActivity.this, ServiceConnection.class);
        if(!ServiceConnection.isConnected)
            this.stopService(intent);
        this.startService(intent);

        setControl();
        addEvent();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(resultFromSever.success)
                    txtConnectionState.setCompoundDrawablesWithIntrinsicBounds(0,0, R.mipmap.bullet_green, 0);
                else
                    txtConnectionState.setCompoundDrawablesWithIntrinsicBounds(0,0, R.mipmap.bullet_red, 0);
            }
        }, 500);
    }

    private void setControl(){
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        txtUserName = (TextView) findViewById(R.id.txtUserName);
        txtPassword = (TextView) findViewById(R.id.txtPassword);
        txtConnectionState = (TextView) findViewById(R.id.txtConnectionState);
        cbxRememberPass = (CheckBox) findViewById(R.id.cbxRememberPass);
    }

    private void addEvent(){
//        if(resultFromSever.success)
//            txtConnectionState.setCompoundDrawablesWithIntrinsicBounds(0,0, R.mipmap.bullet_green, 0);
//        else
//            txtConnectionState.setCompoundDrawablesWithIntrinsicBounds(0,0, R.mipmap.bullet_red, 0);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                if (statusConnecttion)
                    txtConnectionState.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.bullet_green, 0);
                else
                    txtConnectionState.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.bullet_red, 0);
                if (!statusConnecttion) {
                    Toast.makeText(view.getContext(), "Không thể kết nối đến sever! Hãy kiểm tra lại kết nối mạng!", Toast.LENGTH_SHORT).show();
                    return;
                }
                resultFromSever.event = "";
                mSocket.emit(LOGIN, txtUserName.getText().toString(), toSHA256(txtPassword.getText().toString()));
                final ProgressDialog dialog = new ProgressDialog(thisContext);
                dialog.setTitle("Đang đăng nhập...");
                dialog.setContentView(R.layout.loading_layout);
                dialog.show();
                while (!resultFromSever.event.equals(LOGIN)){
                };
                dialog.cancel();
                if (resultFromSever.event.equals(LOGIN) && resultFromSever.success) {
                    savingPreferences();
                    startMainActivity();
                } else
                    Toast.makeText(thisContext, "Sai tên đăng nhập hoặc mật khẩu!", Toast.LENGTH_SHORT).show();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                startRegisterActivity();
            }
        });

        txtUserName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged( CharSequence s, int start, int count, int after ) {

            }

            @Override
            public void onTextChanged( CharSequence s, int start, int before, int count ) {
                restoringPreferences();
            }

            @Override
            public void afterTextChanged( Editable s ) {

            }
        });

        txtPassword.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch( View v, MotionEvent event ) {
                return false;
            }
        });
    }

    public void restoringPreferences()
    {
        SharedPreferences pre = getSharedPreferences(txtUserName.getText().toString().toLowerCase(), MODE_PRIVATE);
        //lấy giá trị checked ra, nếu không thấy thì giá trị mặc định là false
        boolean bchk = pre.getBoolean("remembered", false);
        String pwd = pre.getString("password", "");
        txtPassword.setText(pwd);
        cbxRememberPass.setChecked(bchk);
    }

    public void savingPreferences()
    {
        //tạo đối tượng getSharedPreferences
        SharedPreferences pre=getSharedPreferences(txtUserName.getText().toString().toLowerCase(), MODE_PRIVATE);
        //tạo đối tượng Editor để lưu thay đổi
        SharedPreferences.Editor editor=pre.edit();
        String user = txtUserName.getText().toString();
        String pwd = txtPassword.getText().toString();
        boolean bchk = cbxRememberPass.isChecked();
        if(!bchk)
        {
            //xóa mọi lưu trữ trước đó
            editor.clear();
        }
        else
        {
            //lưu vào editor
            editor.putString("userName", user);
            editor.putString("password", pwd);
            editor.putBoolean("remembered", bchk);
        }
        //chấp nhận lưu xuống file
        editor.commit();
    }
    public void startMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        this.startActivity(intent);
    }

    public void startRegisterActivity(){
        Intent intent = new Intent(this, RegisterActivity.class);
        this.startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            finishAffinity();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Bấm lần nữa để thoát!", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        restoringPreferences();
    }

    @Override
    protected void onDestroy() {
        Intent intent = new Intent(LoginActivity.this, ServiceConnection.class);
        this.stopService(intent);
        super.onDestroy();
    }
}
