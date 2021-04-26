package com.am.todoapp;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Users extends RealmObject {

    @PrimaryKey
    String _id;

    public String user_id, email_id, name, device_token;

    public Users() {
    }

    public Users(String _id, String user_id, String email_id, String name, String device_token) {
        this._id = _id;
        this.user_id = user_id;
        this.email_id = email_id;
        this.name = name;
        this.device_token = device_token;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getEmail_id() {
        return email_id;
    }

    public void setEmail_id(String email_id) {
        this.email_id = email_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDevice_token() {
        return device_token;
    }

    public void setDevice_token(String device_token) {
        this.device_token = device_token;
    }
}
