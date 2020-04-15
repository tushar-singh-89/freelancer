package com.divij.freelancerstreet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ChooseLoginRegistrationActivity extends AppCompatActivity {
    private Button mLogin;
    private Button mRegister;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_login_registration);

        mLogin = (Button)findViewById(R.id.login);
        mRegister=(Button)findViewById(R.id.register);

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent2=new Intent(ChooseLoginRegistrationActivity.this,LoginActivity.class);
                startActivity(intent2);


            }
        });
        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1=new Intent(ChooseLoginRegistrationActivity.this,RegistrationActivity.class);
                startActivity(intent1);


            }
        });
    }
}
