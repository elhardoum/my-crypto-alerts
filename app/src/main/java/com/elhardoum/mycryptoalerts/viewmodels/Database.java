package com.elhardoum.mycryptoalerts.viewmodels;

import android.text.TextUtils;

import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;
import java.util.function.Consumer;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.Sort;

public class Database {
    private static RealmConfiguration config;

    public static Realm getThread()
    {
        if ( null == config ) {
            config = new RealmConfiguration.Builder()
                .schemaVersion(2)
                .name("my-crypto-alerts.v02")
                .build();

            Realm.setDefaultConfiguration(config);
        }

        return Realm.getDefaultInstance();
    }

    public static Thread getSetting( String id, Consumer<String> then )
    {
        final String[] value = new String[1];

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                getThread().executeTransactionAsync(transactionRealm -> {
                    Setting opt = transactionRealm.where(Setting.class).equalTo("id", id).findFirst();
                    then.accept(opt == null ? "" : opt.getValue());
                    // transactionRealm.close();
                });
            }
        });

        thread.start();

        try { thread.join(); } catch (Exception e) {}

        return thread;
    }

    public static Thread setSetting( String id, String value, Consumer<Boolean> then )
    {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                getThread().executeTransactionAsync(transactionRealm -> {
                    Setting opt = transactionRealm.where(Setting.class).equalTo("id", id).findFirst();

                    if ( null == opt ) { // create new
                        opt = transactionRealm.createObject(Setting.class, id);
                    }

                    opt.setValue(value);
                    then.accept(true);
                    // transactionRealm.close();
                });
            }
        });

        thread.start();

        try { thread.join(); } catch (Exception e) {}

        return thread;
    }

    public static Thread getQuote( String id, Consumer<Quote> then )
    {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                getThread().executeTransactionAsync(transactionRealm -> {
                    Quote quote = transactionRealm.where(Quote.class).equalTo("id", id).findFirst();
                    then.accept(quote == null ? new Quote() : quote);
                    // transactionRealm.close();
                });
            }
        });

        thread.start();

        try { thread.join(); } catch (Exception e) {}

        return thread;
    }

    public static Thread setQuote( String id, Double value, Date fetched, Consumer<Boolean> then )
    {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                getThread().executeTransactionAsync(transactionRealm -> {
                    Quote quote = transactionRealm.where(Quote.class).equalTo("id", id).findFirst();

                    if ( null == quote ) { // create new
                        quote = transactionRealm.createObject(Quote.class, id);
                    }

                    quote.setValue(value);
                    quote.setFetched(fetched);
                    then.accept(true);
                    // transactionRealm.close();
                });
            }
        });

        thread.start();

        try { thread.join(); } catch (Exception e) {}

        return thread;
    }

    public static Thread getSymbols( Consumer<RealmResults<Symbol>> then )
    {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                getThread().executeTransactionAsync(transactionRealm -> {
                    RealmResults<Symbol> symbols = transactionRealm.where(Symbol.class)
                            .findAll().sort("id", Sort.DESCENDING);
                    then.accept(symbols);
                    // transactionRealm.close();
                });
            }
        });

        thread.start();

        try { thread.join(); } catch (Exception e) {}

        return thread;
    }

    public static Thread setSymbol( String id, String coinId, String symbol, Double movement, Integer notifications, Consumer<Boolean> then )
    {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                getThread().executeTransaction(transactionRealm -> {
                    Symbol sym = null;

                    if ( id.length() > 0 ) {
                        sym = transactionRealm.where(Symbol.class).equalTo("id", id).findFirst();
                    }

                    if ( null == sym ) { // create new
                        sym = transactionRealm.createObject(Symbol.class, new ObjectId().toString());
                    }

                    sym.setCoinId(coinId);
                    sym.setSymbol(symbol);
                    sym.setMovement(movement);
                    sym.setNotifications(notifications);

                    then.accept(true);
                    // transactionRealm.close();
                });
            }
        });

        thread.start();

        try { thread.join(); } catch (Exception e) {}

        return thread;
    }

    public static Thread getSymbol( String id, Consumer<Symbol> then )
    {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                getThread().executeTransactionAsync(transactionRealm -> {
                    Symbol sym = transactionRealm.where(Symbol.class).equalTo("id", id).findFirst();
                    then.accept(sym == null ? new Symbol() : sym);
                    // transactionRealm.close();
                });
            }
        });

        thread.start();

        try { thread.join(); } catch (Exception e) {}

        return thread;
    }

    public static Thread deleteSymbol( String id, Consumer<Boolean> then )
    {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                getThread().executeTransaction(transactionRealm -> {
                    RealmResults<Symbol> symbols = transactionRealm.where(Symbol.class).equalTo("id", id).findAll();
                    symbols.deleteAllFromRealm();
                    then.accept(true);
                    // transactionRealm.close();
                });
            }
        });

        thread.start();

        try { thread.join(); } catch (Exception e) {}

        return thread;
    }

    public static void getSupportedCrypto( Consumer<HashMap<String, HashMap<String, String>>> then )
    {
        getSetting("supported-crypto", data ->
        {
            JSONArray obj;

            if ( TextUtils.isEmpty(data) ) { // fetch from API
                URLConnection connection = null;
                try {
                    connection = new URL("https://api.coingecko.com/api/v3/coins/list").openConnection();
                } catch (IOException e) {
                    android.util.Log.e("API", "Error getting url", e);
                    then.accept(null);
                    return;
                }

                Scanner scanner = null;
                try {
                    scanner = new Scanner(connection.getInputStream());
                } catch (IOException e) {
                    android.util.Log.e("API", "Error getting url", e);
                    then.accept(null);
                    return;
                }

                String response = scanner.useDelimiter("\\A").next();

                try {
                    obj = new JSONArray(response);
                } catch (JSONException e) {
                    android.util.Log.e("API", "Error parsing json", e);
                    then.accept(null);
                    return;
                }

                Database.setSetting("supported-crypto", obj.toString(), k -> {});
            } else {
                try {
                    obj = new JSONArray(data);
                } catch (JSONException e) {
                    android.util.Log.e("API", "Error parsing json", e);
                    then.accept(null);
                    return;
                }
            }

            HashMap<String, HashMap<String, String>> map = new HashMap<>();

            for (int i = 0; i < obj.length(); i++) {
                JSONObject item;
                String id;
                String symbol;
                String name;

                try { item = obj.getJSONObject(i); } catch (Exception e) { continue; }
                try { id = item.getString("id"); } catch (Exception e) { continue; }
                try { symbol = item.getString("symbol"); } catch (Exception e) { continue; }
                try { name = item.getString("name"); } catch (Exception e) { continue; }

                map.put(id, new HashMap<String, String>(){{
                        put("id", id);
                        put("symbol", symbol);
                        put("name", name);
                }});
            }

            then.accept(map);
        });
    }
}