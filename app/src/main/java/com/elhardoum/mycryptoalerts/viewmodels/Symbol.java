package com.elhardoum.mycryptoalerts.viewmodels;

import io.realm.RealmObject;
import io.realm.annotations.Required;

public class Symbol extends RealmObject {
    // crypto id as identified by the API provider
    @Required private String id;
    // crypto display name
    @Required private String name;
    // signed double for up/down movement tracking
    @Required private Double movement;
    // how many notifications were sent so far
    @Required private Integer notifications;

    public void setId(String id) { this.id = id; }
    public String getId() { return id; }

    public void setName(String name) { this.name = name; }
    public String getName() { return name; }

    public void setMovement(Double movement) { this.movement = movement; }
    public Double getMovement() { return movement; }

    public void setNotifications(Integer notifications) { this.notifications = notifications; }
    public Integer getNotifications() { return notifications; }

    public Symbol( String id ) {
        this.id = id;
    }
    
    public Symbol( String id, String name, Double movement, Integer notifications ) {
        this.id = id;
        this.name = name;
        this.movement = movement;
        this.notifications = notifications;
    }

    public Symbol() {}
}