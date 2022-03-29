package com.elhardoum.mycryptoalerts.viewmodels;

import io.realm.RealmObject;
import io.realm.annotations.Required;
import io.realm.annotations.PrimaryKey;

public class Symbol extends RealmObject {
    // entity id
    @PrimaryKey private String id;
    // crypto id as identified by the API provider
    @Required private String coinId;
    // crypto symbol (also used as display name)
    @Required private String symbol;
    // signed double for up/down movement tracking
    @Required private Double movement;
    // how many notifications were sent so far
    @Required private Integer notifications;

    public void setId(String id) { this.id = id; }
    public String getId() { return id; }

    public void setCoinId(String coinId) { this.coinId = coinId; }
    public String getCoinId() { return coinId; }

    public void setSymbol(String symbol) { this.symbol = symbol; }
    public String getSymbol() { return symbol; }

    public void setMovement(Double movement) { this.movement = movement; }
    public Double getMovement() { return movement; }

    public void setNotifications(Integer notifications) { this.notifications = notifications; }
    public Integer getNotifications() { return notifications; }

    public Symbol( String id ) {
        this.id = id;
    }
    
    public Symbol( String id, String coinId, String symbol, Double movement, Integer notifications ) {
        this.id = id;
        this.coinId = coinId;
        this.symbol = symbol;
        this.movement = movement;
        this.notifications = notifications;
    }

    public Symbol() {}
}