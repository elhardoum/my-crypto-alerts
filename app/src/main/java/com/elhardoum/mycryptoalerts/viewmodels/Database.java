package com.elhardoum.mycryptoalerts.viewmodels;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class Database {
    private static Realm thread;

    public static Realm getThread()
    {
        if ( null == thread ) {
            String realmName = "my-crypto-alerts.v1";
            RealmConfiguration config = new RealmConfiguration.Builder().name(realmName).build();
            thread = Realm.getInstance(config);
        }

        return thread;
    }
}