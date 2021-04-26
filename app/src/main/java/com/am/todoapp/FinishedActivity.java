package com.am.todoapp;

import androidx.appcompat.app.AppCompatActivity;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.RealmResultTask;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;
import io.realm.mongodb.mongo.iterable.MongoCursor;

public class FinishedActivity extends AppCompatActivity {

    private String app_id = "application-todo-app-ttyvj";

    private static App app;

    private static MongoDatabase mongoDatabase;
    private static MongoClient mongoClient;

    private static RecyclerView Todo_List;

    private static RelativeLayout No_Layout;

    private static TodoAdapter todoAdapter;
    public static List<Todo> todoList;
    LinearLayoutManager layoutManager;

    private static ProgressDialog mProgressDialog;

    private static MongoCollection<Document> TodoCollections;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finished);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Realm.init(FinishedActivity.this);

        app = new App(new AppConfiguration.Builder(app_id).build());

        Todo_List = findViewById(R.id.todo_list);

        No_Layout = findViewById(R.id.no_layout);
        LottieAnimationView anim = findViewById(R.id.animationView);
        anim.setAnimation(R.raw.empty);

        mProgressDialog = new ProgressDialog(this);

        layoutManager = new LinearLayoutManager(FinishedActivity.this);
        Todo_List.setLayoutManager(layoutManager);
        Todo_List.setNestedScrollingEnabled(false);
        todoList = new ArrayList<>();
        todoAdapter = new TodoAdapter(FinishedActivity.this, FinishedActivity.this, todoList);
        Todo_List.setAdapter(todoAdapter);
        todoAdapter.notifyDataSetChanged();

        if (app.currentUser()!=null) {

            User user = app.currentUser();
            mongoClient = user.getMongoClient("mongodb-atlas");
            mongoDatabase = mongoClient.getDatabase("Todo-App");
            TodoCollections = mongoDatabase.getCollection("Todo");

            readTodo();
        }
    }


    public static void readTodo() {

        mProgressDialog.setTitle("Loading");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        todoList.clear();

        Document queryFilter  = new Document("user_id", app.currentUser().getId());
        queryFilter.append("status", "completed");
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
}