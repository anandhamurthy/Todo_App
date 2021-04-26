package com.am.todoapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import org.bson.Document;

import java.util.List;

import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;

import static androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_IDLE;
import static androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE;
import static androidx.recyclerview.widget.ItemTouchHelper.RIGHT;

class Swipe extends ItemTouchHelper.Callback {

    private Drawable imageDrawable;
    //private Drawable shareRound;
    private RecyclerView.ViewHolder currentItemViewHolder;
    private View mView;
    private float dX = 0f;
    private float replyButtonProgress = 0f;
    private long lastReplyButtonAnimationTime = 0;
    private boolean swipeBack = false;
    private boolean isVibrate = false;
    private boolean startTracking = false;
    private float density;
    private final Context context;
    private final SwipeControllerActions swipeControllerActions;
    List<Todo> List;

    private String app_id = "application-todo-app-ttyvj";

    private App app;

    MongoDatabase mongoDatabase;
    MongoClient mongoClient;


    public Swipe(Context context, List<Todo> chatList, SwipeControllerActions swipeControllerActions) {
        super();
        this.context = context;
        this.swipeControllerActions = swipeControllerActions;
        this.density = 1.0F;
        this.List = chatList;
    }


    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        mView = viewHolder.itemView;
        imageDrawable = context.getDrawable(R.drawable.completed);
//        shareRound = context.getDrawable(R.drawable.graph);
        return ItemTouchHelper.Callback.makeMovementFlags(ACTION_STATE_IDLE, RIGHT);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

    }

    @Override
    public int convertToAbsoluteDirection(int flags, int layoutDirection) {
        if (swipeBack) {
            swipeBack = false;
            return 0;
        }
        return super.convertToAbsoluteDirection(flags, layoutDirection);
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        int position = viewHolder.getAdapterPosition();
        Todo todo = List.get(position);
        if (todo.getStatus().equals("pending")){
            if (actionState == ACTION_STATE_SWIPE) {
                setTouchListener(recyclerView, viewHolder);
            }

            if (mView.getTranslationX() < convertTodp(130) || dX < this.dX) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                this.dX = dX;
                startTracking = true;
            }
            currentItemViewHolder = viewHolder;
            drawReplyButton(c, todo);
        }

    }


    private void setTouchListener(RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder) {
        recyclerView.setOnTouchListener((__, event) -> {
                swipeBack = event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP;
                if (swipeBack) {
                    if (Math.abs(mView.getTranslationX()) >= this.convertTodp(100)) {
                        swipeControllerActions.showReplyUI(viewHolder.getAdapterPosition());
                    }
                }
            return false;
        });
    }


    private void drawReplyButton(Canvas canvas, Todo todo) {
        if (currentItemViewHolder == null) {
            return;
        }
        float translationX = mView.getTranslationX();
        long newTime = System.currentTimeMillis();
        long dt = Math.min(17, newTime - lastReplyButtonAnimationTime);
        lastReplyButtonAnimationTime = newTime;
        boolean showing = translationX >= convertTodp(30);
        if (showing) {
            if (replyButtonProgress < 1.0f) {
                replyButtonProgress += dt / 180.0f;
                if (replyButtonProgress > 1.0f) {
                    replyButtonProgress = 1.0f;
                } else {
                    mView.invalidate();
                }
            }
        } else if (translationX <= 0.0f) {
            replyButtonProgress = 0f;
            startTracking = false;
            isVibrate = false;
        } else {
            if (replyButtonProgress > 0.0f) {
                replyButtonProgress -= dt / 180.0f;
                if (replyButtonProgress < 0.1f) {
                    replyButtonProgress = 0f;
                } else {
                    mView.invalidate();
                }
            }
        }
        int alpha;
        float scale;
        if (showing) {
            scale = this.replyButtonProgress <= 0.8F ? 1.2F * (this.replyButtonProgress / 0.8F) : 1.2F - 0.2F * ((this.replyButtonProgress - 0.8F) / 0.2F);
            alpha = (int) Math.min(255.0F, (float) 255 * (this.replyButtonProgress / 0.8F));
        } else {
            scale = this.replyButtonProgress;
            alpha = (int) Math.min(255.0F, (float) 255 * this.replyButtonProgress);
        }
        //shareRound.setAlpha(alpha);
        imageDrawable.setAlpha(alpha);
        if (startTracking) {
            if (!isVibrate && mView.getTranslationX() >= convertTodp(100)) {


                app = new App(new AppConfiguration.Builder(app_id).build());
                User user = app.currentUser();
                mongoClient = user.getMongoClient("mongodb-atlas");
                mongoDatabase = mongoClient.getDatabase("Todo-App");

                Document queryFilter = new Document("_id", todo.get_id());
                Document updateDocument = new Document();
                updateDocument.append("user_id", todo.getUser_id());
                updateDocument.append("title", todo.getTitle());
                updateDocument.append("message", todo.getMessage());
                updateDocument.append("priority", todo.getPriority());
                updateDocument.append("status", "completed");
                updateDocument.append("timestamp", todo.getTimestamp());
                MongoCollection<Document> TodoCollections = mongoDatabase.getCollection("Todo");
                TodoCollections.updateOne(queryFilter, updateDocument).getAsync(task -> {
                    if (task.isSuccess()) {
                        long count = task.get().getModifiedCount();
                        if (count == 1) {
                            Log.v("EXAMPLE", "successfully updated a document.");
                            Toast.makeText(context, "Completed !", Toast.LENGTH_SHORT).show();
                            MainActivity.readTodo();
                        } else {
                            Log.v("EXAMPLE", "did not update a document.");
                        }
                    } else {
                        Log.e("EXAMPLE", "failed to update document with: ", task.getError());
                    }
                });

                isVibrate = true;
            }
        }

        int x;
        if (mView.getTranslationX() > (float) this.convertTodp(130)) {
            x = this.convertTodp(130) / 2;
        } else {
            x = (int) (mView.getTranslationX() / (float) 2);
        }


        float y;
        y = (float) ((mView.getTop() + mView.getMeasuredHeight() / 2));
        //shareRound.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.colorAccent), PorterDuff.Mode.MULTIPLY));
        //shareRound.setBounds((int) ((float) x - (float) this.convertTodp(18) * scale), (int) (y - (float) this.convertTodp(18) * scale), (int) ((float) x + (float) this.convertTodp(18) * scale), (int) (y + (float) this.convertTodp(18) * scale));


        //shareRound.draw(canvas);
        imageDrawable.setBounds((int) ((float) x - (float) this.convertTodp(12) * scale), (int) (y - (float) this.convertTodp(11) * scale), (int) ((float) x + (float) this.convertTodp(12) * scale), (int) (y + (float) this.convertTodp(10) * scale));

        imageDrawable.draw(canvas);
        //shareRound.setAlpha(255);
        imageDrawable.setAlpha(255);
    }


    private final int convertTodp(int pixel) {
        return this.dp((float) pixel, this.context);
    }


    public int dp(Float value, Context context) {
        if (this.density == 1.0F) {
            this.checkDisplaySize(context);
        }

        return value == 0.0F ? 0 : (int) Math.ceil((double) (this.density * value));
    }

    private final void checkDisplaySize(Context context) {
        try {
            this.density = context.getResources().getDisplayMetrics().density;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public interface SwipeControllerActions {
        void showReplyUI(int var1);
    }

}