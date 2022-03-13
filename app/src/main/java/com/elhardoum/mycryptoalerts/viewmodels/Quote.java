package com.elhardoum.mycryptoalerts.viewmodels;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class Quote extends RealmObject {
    // crypto id as identified by the API provider
    @PrimaryKey private String id;
    // crypto quote
    @Required private Double value;
    // date fetched
    @Required private Date fetched;

    public void setId(String id) { this.id = id; }
    public String getId() { return id; }

    public void setValue(Double value) { this.value = value; }
    public Double getValue() { return value; }

    public void setFetched(Date fetched) { this.fetched = fetched; }
    public Date getFetched() { return fetched; }

    public Quote( String id ) {
        this.id = id;
    }
    
    public Quote( String id, Double value, Date fetched ) {
        this.id = id;
        this.value = value;
        this.fetched = fetched;
    }

    public Quote() {}
}