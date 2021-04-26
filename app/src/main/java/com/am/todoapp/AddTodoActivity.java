package com.am.todoapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.bson.Document;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;
import io.realm.mongodb.mongo.result.InsertOneResult;

public class AddTodoActivity extends AppCompatActivity {

    private String app_id = "application-todo-app-ttyvj";

    private App app;

    MongoDatabase mongoDatabase;
    MongoClient mongoClient;

    private EditText Title, Message;
    private AutoCompleteTextView Priority;
    private FloatingActionButton Done;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_todo);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        app = new App(new AppConfiguration.Builder(app_id).build());

        Title = findViewById(R.id.title);
        Message = findViewById(R.id.message);
        Priority = findViewById(R.id.priority);
        Done = findViewById(R.id.done);

        setPriority();

        mProgressDialog = new ProgressDialog(this);

        Done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = Message.getText().toString();
                String title = Title.getText().toString();
                String priority = Priority.getText().toString();

                if (!message.isEmpty() && !title.isEmpty() && !priority.isEmpty()){

                    mProgressDialog.setTitle("Todo");
                    mProgressDialog.setMessage("Creating todo..");
                    mProgressDialog.show();
                    mProgressDialog.setCanceledOnTouchOutside(false);

                    User user = app.currentUser();
                    mongoClient = user.getMongoClient("mongodb-atlas");
                    mongoDatabase = mongoClient.getDatabase("Todo-App");

                    MongoCollection<Document> mongoCollection = mongoDatabase.getCollection("Todo");

                    String currentDate = new SimpleDateFormat("EEE, dd, MMM yyyy, HH:mm:ss", Locale.getDefault()).format(new Date());
                    Document document = new Document();
                    document.append("user_id", user.getId());
                    document.append("title", title);
                    document.append("message", message);
                    document.append("priority", priority);
                    document.append("status", "pending");
                    document.append("timestamp", currentDate);

                    mongoCollection.insertOne(document).getAsync(new App.Callback<InsertOneResult>() {
                        @Override
                        public void onResult(App.Result<InsertOneResult> result) {
                            if (result.isSuccess()) {
                                mProgressDialog.dismiss();
                                Intent intent = new Intent(AddTodoActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Log.v("Data", "Error:" + result.getError().toString());
                            }
                        }
                    });

                }else{
                    Toast.makeText(AddTodoActivity.this, "Add a Todo!", Toast.LENGTH_SHORT).show();
                }
            }
        });



    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(AddTodoActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    void setPriority() {
        String items[];
        items = getResources().getStringArray(R.array.Priority);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                AddTodoActivity.this, R.layout.single_layout_source, items);

        Priority.setAdapter(adapter);
        Priority.setThreshold(1);

    }
}