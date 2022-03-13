package com.elhardoum.mycryptoalerts.viewmodels;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class Setting extends RealmObject {
    // option id
    @PrimaryKey private String id;
    // option value
    @Required private String value;

    public void setId(String id) { this.id = id; }
    public String getId() { return id; }

    public void setValue(String value) { this.value = value; }
    public String getValue() { return value; }

    public Setting( String id ) {
        this.id = id;
    }
    
    public Setting( String id, String value ) {
        this.id = id;
        this.value = value;
    }

    public Setting() {}
}