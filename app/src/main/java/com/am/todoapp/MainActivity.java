package com.am.todoapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.RealmResultTask;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;
import io.realm.mongodb.mongo.iterable.FindIterable;
import io.realm.mongodb.mongo.iterable.MongoCursor;
import io.realm.mongodb.mongo.result.InsertOneResult;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {

    private String app_id = "application-todo-app-ttyvj";

    private static App app;

    private static MongoDatabase mongoDatabase;
    private static MongoClient mongoClient;

    private TextView User_Name;
    private static RecyclerView Todo_List;
    private FloatingActionButton Add;

    private static RelativeLayout No_Layout;

    private static TodoAdapter todoAdapter;
    public static List<Todo> todoList;
    LinearLayoutManager layoutManager;

    private static ProgressDialog mProgressDialog;

    private static MongoCollection<Document> TodoCollections, UserCollections;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Realm.init(MainActivity.this);

        app = new App(new AppConfiguration.Builder(app_id).build());

        User_Name = findViewById(R.id.user_name);
        Todo_List = findViewById(R.id.todo_list);
        Add = findViewById(R.id.add_todo);

        No_Layout = findViewById(R.id.no_layout);
        LottieAnimationView anim = findViewById(R.id.animationView);
        anim.setAnimation(R.raw.empty);

        mProgressDialog = new ProgressDialog(this);

        layoutManager = new LinearLayoutManager(MainActivity.this);
        Todo_List.setLayoutManager(layoutManager);
        Todo_List.setNestedScrollingEnabled(false);
        todoList = new ArrayList<>();
        todoAdapter = new TodoAdapter(MainActivity.this, MainActivity.this, todoList);
        Todo_List.setAdapter(todoAdapter);
        todoAdapter.notifyDataSetChanged();

        if (app.currentUser()!=null) {

            User user = app.currentUser();
            mongoClient = user.getMongoClient("mongodb-atlas");
            mongoDatabase = mongoClient.getDatabase("Todo-App");

            UserCollections = mongoDatabase.getCollection("User");
            TodoCollections = mongoDatabase.getCollection("Todo");

            Document document = new Document("_id",app.currentUser().getId());
            UserCollections.findOne(document).getAsync(new App.Callback<Document>() {
                @Override
                public void onResult(App.Result<Document> result) {
                    if (result!=null) {
                        Document document = result.get();
                        Users users = new Users();
                        users.setName(document.getString("name"));

                        User_Name.setText("Hi, "+users.getName());
                    }
                }
            });

            Add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddTodoActivity.class);
                startActivity(intent);
                finish();

                }
            });

            readTodo();

            enableswipe();
        }
    }

    private void enableswipe() {

        Swipe swipeReplyController = new Swipe(MainActivity.this, todoList, position -> {
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeReplyController);
        itemTouchHelper.attachToRecyclerView(Todo_List);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (app.currentUser() == null) {

            Intent mainIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(mainIntent);
            finish();

        }
    }

    public static void readTodo() {

        mProgressDialog.setTitle("Loading");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        todoList.clear();

        Document queryFilter  = new Document("user_id", app.currentUser().getId());
        RealmResultTask<MongoCursor<Document>> findTask = TodoCollections.find(queryFilter).iterator();
        findTask.getAsync(task -> {
            if (task.isSuccess()) {
                MongoCursor<Document> results = task.get();
                while (results.hasNext()) {
                    Document document = results.next();
                    Todo todo1 = new Todo();
                    todo1.setTitle(document.getString("title"));
                    todo1.setMessage(document.getString("message"));
                    todo1.set_id(document.getObjectId("_id"));
                    todo1.setPriority(document.getString("priority"));
                    todo1.setStatus(document.getString("status"));
                    todo1.setTimestamp(document.getString("timestamp"));
                    todo1.setUser_id(document.getString("user_id"));

                    todoList.add(todo1);
                    todoAdapter.notifyDataSetChanged();
                }
                if (todoList.isEmpty()) {
                    No_Layout.setVisibility(View.VISIBLE);
                    Todo_List.setVisibility(View.GONE);
                    mProgressDialog.dismiss();
                } else {
                    No_Layout.setVisibility(View.GONE);
                    Todo_List.setVisibility(View.VISIBLE);
                    mProgressDialog.dismiss();
                }
                todoAdapter.notifyDataSetChanged();
            } else {
                Log.e("EXAMPLE", "failed to find documents with: ", task.getError());
            }
        });


    }


    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                app.currentUser().logOutAsync(new App.Callback<User>() {
                    @Override
                    public void onResult(App.Result<User> result) {
                        if (result.isSuccess()) {
                            Log.v("AUTH", "Successfully logged out.");
                            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                            startActivity(loginIntent);
                            finish();
                        } else {
                            Log.e("AUTH", result.getError().toString());
                        }
                    }
                });

                break;
            case R.id.finished:
                Intent setupIntent = new Intent(MainActivity.this, FinishedActivity.class);
                startActivity(setupIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }



}