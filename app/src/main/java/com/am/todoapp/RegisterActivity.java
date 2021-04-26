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

import java.util.HashMap;

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

public class RegisterActivity extends AppCompatActivity {

    private EditText Name, Email_Id, Password, Confirm_Password;
    private TextView Forgot_Password;

    private ProgressDialog mProgressDialog;
    private FloatingActionButton Register;
    private TextView Login;

    private String app_id = "application-todo-app-ttyvj";

    private App app;
    MongoDatabase mongoDatabase;
    MongoClient mongoClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Login = findViewById(R.id.login);
        Register = findViewById(R.id.register);
        Name = findViewById(R.id.name);
        Email_Id = findViewById(R.id.email_id);
        Password = findViewById(R.id.password);
        Confirm_Password = findViewById(R.id.confirm_password);
        Forgot_Password = findViewById(R.id.forgot_password);

        mProgressDialog = new ProgressDialog(this);

        Realm.init(this);

        app = new App(new AppConfiguration.Builder(app_id).build());


        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });

        Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String name = Name.getText().toString();
                final String email_id = Email_Id.getText().toString().trim();
                final String password = Password.getText().toString().trim();
                final String confirm_pass = Confirm_Password.getText().toString();

                if (isEmpty(name, email_id, password, confirm_pass)) {

                    if (password.length() > 5 && confirm_pass.length() > 5) {

                        if (password.equals(confirm_pass)) {

                            mProgressDialog.setTitle("Registering");
                            mProgressDialog.setMessage("Creating User..");
                            mProgressDialog.show();
                            mProgressDialog.setCanceledOnTouchOutside(false);


                            app.getEmailPassword().registerUserAsync(email_id, password, new App.Callback<Void>() {
                                @Override
                                public void onResult(App.Result<Void> result) {

                                    if (result.isSuccess()) {


                                        Credentials credentials = Credentials.emailPassword(email_id, password);
                                        app.loginAsync(credentials, new App.Callback<User>() {
                                            @Override
                                            public void onResult(App.Result<User> result) {
                                                if (result.isSuccess()) {

                                                    User user = app.currentUser();
                                                    mongoClient = user.getMongoClient("mongodb-atlas");
                                                    mongoDatabase = mongoClient.getDatabase("Todo-App");

                                                    MongoCollection<Document> mongoCollection = mongoDatabase.getCollection("User");

                                                    Document document = new Document();
                                                    document.append("_id", user.getId());
                                                    document.append("email_id", email_id);
                                                    document.append("name", name);
                                                    document.append("device_token", user.getDeviceId());

                                                    mongoCollection.insertOne(document).getAsync(new App.Callback<InsertOneResult>() {
                                                        @Override
                                                        public void onResult(App.Result<InsertOneResult> result) {
                                                            if (result.isSuccess()) {
                                                                mProgressDialog.dismiss();
                                                                Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                startActivity(mainIntent);
                                                                finish();
                                                            } else {
                                                                Log.v("Data", "Error:" + result.getError().toString());
                                                            }
                                                        }
                                                    });

                                                }
                                            }
                                        });

                                    } else {
                                        Log.v("User", "Registration Failed");
                                    }

                                }
                            });
                        }

                    } else {

                        Password.setError("Atleast 6 Characters");
                        Confirm_Password.setError("Atleast 6 Characters");
                        Toast.makeText(RegisterActivity.this, "Password must contain atleast 6 characters.", Toast.LENGTH_LONG).show();
                    }



                } else {

                    Toast.makeText(RegisterActivity.this, "Enter all the details.", Toast.LENGTH_LONG).show();

                }


            }
        });

        Forgot_Password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));

            }
        });

    }

    private boolean isEmpty(String name, String email_id, String password, String confirm_pass) {
        if (name.isEmpty() || email_id.isEmpty() || password.isEmpty() || confirm_pass.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "Complete All the Details", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}
