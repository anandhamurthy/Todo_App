package com.am.todoapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import org.bson.Document;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.CropHolder> {

    private Context mContext;
    private List<Todo> mTodo;

    private Activity activity;

    private String app_id = "application-todo-app-ttyvj";

    private App app;

    private boolean isMain;

    MongoDatabase mongoDatabase;
    MongoClient mongoClient;


    public TodoAdapter(Context context, MainActivity activity, List<Todo> todos){
        mContext = context;
        mTodo = todos;
        this.activity = activity;
        this.isMain = true;
    }

    public TodoAdapter(FinishedActivity context, FinishedActivity finishedActivity, List<Todo> todos) {
        mContext = context;
        mTodo = todos;
        this.activity = finishedActivity;
        this.isMain = false;
    }

    @Override
    public CropHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.single_todo_layout, parent, false);
        return new CropHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final CropHolder holder, final int position) {

        final Todo todo = mTodo.get(position);

        holder.title.setText(todo.getTitle());
        holder.message.setText(todo.getMessage());
        holder.priority.setText(todo.getPriority());
        holder.timestamp.setText(todo.getTimestamp());

        if (todo.getPriority().equals("High")){
            holder.priority.setBackgroundResource(R.drawable.status_red_bg);
        }else if (todo.getPriority().equals("Medium")){
            holder.priority.setBackgroundResource(R.drawable.status_yellow_bg);
        }else{
            holder.priority.setBackgroundResource(R.drawable.status_green_bg);
        }

        app = new App(new AppConfiguration.Builder(app_id).build());
        User user = app.currentUser();
        mongoClient = user.getMongoClient("mongodb-atlas");
        mongoDatabase = mongoClient.getDatabase("Todo-App");

        setTagforDown(holder.down);

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Document queryFilter = new Document("_id", todo.get_id());
                MongoCollection<Document> TodoCollections = mongoDatabase.getCollection("Todo");
                TodoCollections.deleteOne(queryFilter).getAsync(task -> {
                    if (task.isSuccess()) {
                        long count = task.get().getDeletedCount();
                        if (count == 1) {
                            Log.v("EXAMPLE", "successfully deleted a document.");
                            if (isMain)
                                MainActivity.readTodo();
                            else
                                FinishedActivity.readTodo();
                        } else {
                            Log.v("EXAMPLE", "did not delete a document.");
                        }
                    } else {
                        Log.e("EXAMPLE", "failed to delete document with: ", task.getError());
                    }
                });
            }
        });

        holder.down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (holder.down.getTag().equals("false")){
                    holder.down.setImageResource(R.drawable.up);
                    holder.down.setTag("true");
                    holder.other_layout.setVisibility(View.VISIBLE);
                    holder.line.setVisibility(View.VISIBLE);
                }else{
                    holder.down.setImageResource(R.drawable.down);
                    holder.down.setTag("false");
                    holder.other_layout.setVisibility(View.GONE);
                    holder.line.setVisibility(View.GONE);
                }

            }
        });


    }

    private void setTagforDown(ImageView down) {

        if (down.getTag().equals("false")){
            down.setTag("true");
        }else{
            down.setTag("false");
        }
    }

    @Override
    public int getItemCount() {
        return mTodo.size();
    }


    public class CropHolder extends RecyclerView.ViewHolder {

        private TextView title, message, timestamp, priority;
        private ImageView down, delete, line;
        private RelativeLayout other_layout;

        public CropHolder(View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.title);
            message = itemView.findViewById(R.id.message);
            down = itemView.findViewById(R.id.down);
            down.setTag("false");
            delete = itemView.findViewById(R.id.delete);
            line = itemView.findViewById(R.id.line);
            other_layout = itemView.findViewById(R.id.other_layout);
            timestamp = itemView.findViewById(R.id.timestamp);
            priority = itemView.findViewById(R.id.priority);

        }
    }
}