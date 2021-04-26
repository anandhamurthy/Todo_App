package com.am.todoapp;

import org.bson.types.ObjectId;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Todo extends RealmObject {

    @PrimaryKey
    private ObjectId _id = new ObjectId();

    private String user_id, title, message, priority, status, timestamp;

    public Todo() {
    }

    public Todo(ObjectId _id, String user_id, String title, String message, String priority, String status, String timestamp) {
        this._id = _id;
        this.user_id = user_id;
        this.title = title;
        this.message = message;
        this.priority = priority;
        this.status = status;
        this.timestamp = timestamp;
    }

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
