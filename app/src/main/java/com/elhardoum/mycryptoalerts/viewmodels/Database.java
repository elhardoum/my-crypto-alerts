package com.elhardoum.mycryptoalerts.viewmodels;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

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
    private final static Database INSTANCE = new Database();

    private static RealmConfiguration config;
    private static boolean useThreads = false;

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

    private static void executeTransactionWrapper(Consumer<Realm> callback)
    {
        if ( useThreads ) {
            executeInThread(callback);
        } else {
            executeTransaction(callback);
        }
    }

    private static void executeTransactionWrapper( boolean threads, Consumer<Realm> callback)
    {
        if ( threads ) {
            executeInThread(callback);
        } else {
            executeTransaction(callback);
        }
    }

    private static void executeTransaction(Consumer<Realm> callback)
    {
        Realm realm = getThread();

        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {
                callback.accept(bgRealm);
            }
        }, realm::close, new Realm.Transaction.OnError() {
            @Override
            public void onError(@NonNull Throwable error) {
                Log.e("DATA", "realm transaction exception", error);
                realm.close();
            }
        });
    }

    private static Thread executeInThread(Consumer<Realm> callback)
    {
        Thread thread = new Thread(() ->
        {
            Realm realm = getThread();

            realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm bgRealm) {
                    callback.accept(bgRealm);
                }
            });
        });

        thread.start();

        try { thread.join(); } catch (Exception e) {
            Log.e("DATA", "Thread exception", e);
        }

        return thread;
    }

    public static void getSetting( String id, Consumer<String> then )
    {
        executeTransactionWrapper(transactionRealm -> {
            Setting opt = transactionRealm.where(Setting.class).equalTo("id", id).findFirst();
            then.accept(opt == null ? "" : opt.getValue());
        });
    }

    public static void setSetting( String id, String value, Consumer<Boolean> then )
    {
        executeTransactionWrapper(transactionRealm -> {
            Setting opt = transactionRealm.where(Setting.class).equalTo("id", id).findFirst();

            if ( null == opt ) { // create new
                opt = transactionRealm.createObject(Setting.class, id);
            }

            opt.setValue(value);
            then.accept(true);
        });
    }

    public static void setSetting( String id, String value, boolean threads, Consumer<Boolean> then )
    {
        executeTransactionWrapper(threads, transactionRealm -> {
            Setting opt = transactionRealm.where(Setting.class).equalTo("id", id).findFirst();

            if ( null == opt ) { // create new
                opt = transactionRealm.createObject(Setting.class, id);
            }

            opt.setValue(value);
            then.accept(true);
        });
    }

    public static void getQuote( String id, Consumer<Quote> then, boolean threads )
    {
        executeTransactionWrapper(threads, transactionRealm -> {
            Quote quote = transactionRealm.where(Quote.class).equalTo("id", id).findFirst();
            then.accept(quote == null ? new Quote() : quote);
        });
    }

    public static void setQuote( String id, Double value, Date fetched, Consumer<Boolean> then, boolean threads )
    {
        executeTransactionWrapper(threads, transactionRealm -> {
            Quote quote = transactionRealm.where(Quote.class).equalTo("id", id).findFirst();

            if ( null == quote ) { // create new
                quote = transactionRealm.createObject(Quote.class, id);
            }

            quote.setValue(value);
            quote.setFetched(fetched);
            then.accept(true);
        });
    }

    public static void getSymbols( Consumer<RealmResults<Symbol>> then )
    {
        executeTransactionWrapper(transactionRealm -> {
            RealmResults<Symbol> symbols = transactionRealm.where(Symbol.class)
                    .findAll().sort("id", Sort.DESCENDING);
            then.accept(symbols);
        });
    }

    public static void getSymbols( boolean thread, Consumer<RealmResults<Symbol>> then )
    {
        executeTransactionWrapper(thread, transactionRealm -> {
            RealmResults<Symbol> symbols = transactionRealm.where(Symbol.class)
                    .findAll().sort("id", Sort.DESCENDING);
            then.accept(symbols);
        });
    }

    public static void setSymbol( String id, String coinId, String symbol, Double movement, Integer notifications, Consumer<Boolean> then )
    {
        executeTransactionWrapper(transactionRealm -> {
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
        });
    }

    public static void incrNotification( String id, Consumer<Boolean> then, boolean theads )
    {
        executeTransactionWrapper(theads, transactionRealm -> {
            Symbol sym = transactionRealm.where(Symbol.class).equalTo("id", id).findFirst();

            if ( null == sym ) {
                then.accept(false);
                return;
            }

            sym.setNotifications(sym.getNotifications()+1);
            then.accept(true);
        });
    }

    public static void getSymbol( String id, Consumer<Symbol> then )
    {
        executeTransactionWrapper(transactionRealm -> {
            Symbol sym = transactionRealm.where(Symbol.class).equalTo("id", id).findFirst();
            then.accept(sym == null ? new Symbol() : sym);
        });
    }

    public static void deleteSymbol( String id, Consumer<Boolean> then )
    {
        executeTransactionWrapper(transactionRealm -> {
            RealmResults<Symbol> symbols = transactionRealm.where(Symbol.class).equalTo("id", id).findAll();
            symbols.deleteAllFromRealm();
            then.accept(true);
        });
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
                    Log.e("API", "Error getting url", e);
                    then.accept(null);
                    return;
                }

                Scanner scanner = null;
                try {
                    scanner = new Scanner(connection.getInputStream());
                } catch (IOException e) {
                    Log.e("API", "Error getting url", e);
                    then.accept(null);
                    return;
                }

                String response = scanner.useDelimiter("\\A").next();

                try {
                    obj = new JSONArray(response);
                } catch (JSONException e) {
                    Log.e("API", "Error parsing json", e);
                    then.accept(null);
                    return;
                }

                Database.setSetting("supported-crypto", obj.toString(), true, k -> {});
            } else {
                try {
                    obj = new JSONArray(data);
                } catch (JSONException e) {
                    Log.e("API", "Error parsing json", e);
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