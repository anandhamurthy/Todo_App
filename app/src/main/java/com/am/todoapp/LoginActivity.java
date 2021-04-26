package com.am.todoapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.bson.Document;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;
import io.realm.mongodb.mongo.result.InsertOneResult;

import static android.content.ContentValues.TAG;

public class LoginActivity extends AppCompatActivity {

    private EditText Email_Id, Password;
    private TextView Forgot_Password;

    private ProgressDialog mProgressDialog;
    private FloatingActionButton Login;
    private TextView Register;

    private String app_id = "application-todo-app-ttyvj";

    private App app;
    MongoDatabase mongoDatabase;
    MongoClient mongoClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Login = findViewById(R.id.login);
        Register = findViewById(R.id.register);
        Email_Id = findViewById(R.id.email_id);
        Password = findViewById(R.id.password);
        Forgot_Password = findViewById(R.id.forgot_password);

        mProgressDialog = new ProgressDialog(this);

        Realm.init(this);

        app = new App(new AppConfiguration.Builder(app_id).build());

        Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                finish();
            }
        });

        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email_id = Email_Id.getText().toString().trim();
                final String password = Password.getText().toString().trim();

                if (isEmpty(email_id, password)) {

                    mProgressDialog.setTitle("Logging In");
                    mProgressDialog.setMessage("Authenticating User..");
                    mProgressDialog.show();
                    mProgressDialog.setCanceledOnTouchOutside(false);


                    Credentials credentials = Credentials.emailPassword(email_id, password);
                    app.loginAsync(credentials, new App.Callback<User>() {
                        @Override
                        public void onResult(App.Result<User> result) {
                            if(result.isSuccess()) {

                                mProgressDialog.dismiss();
                                Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(mainIntent);
                                finish();

                            } else {
                                Toast.makeText(LoginActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                Log.v("User","Failed to Login");
                            }
                        }
                    });

                } else {

                    Toast.makeText(LoginActivity.this, "Enter all the details.", Toast.LENGTH_LONG).show();

                }


            }
        });
        Forgot_Password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));

            }
        });

    }

    private boolean isEmpty(String email_id, String password) {
        if (email_id.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Complete All the Details", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (app.currentUser() != null) {

            Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(mainIntent);
            finish();

        }
    }
}