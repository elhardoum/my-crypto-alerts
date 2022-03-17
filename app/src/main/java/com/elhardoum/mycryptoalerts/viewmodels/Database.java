package com.elhardoum.mycryptoalerts.viewmodels;

import java.util.Date;
import java.util.function.Consumer;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class Database {
    public static Realm getThread()
    {
        return Realm.getDefaultInstance();
    }

    public static Thread getSetting( String id, Consumer<String> then )
    {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                getThread().executeTransactionAsync(transactionRealm -> {
                    Setting opt = transactionRealm.where(Setting.class).equalTo("id", id).findFirst();
                    then.accept(opt == null ? "" : opt.getValue());
                });
            }
        });

        thread.start();

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
                });
            }
        });

        thread.start();

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
                });
            }
        });

        thread.start();

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
                });
            }
        });

        thread.start();

        return thread;
    }

    public static Thread getSymbols( String id, Consumer<RealmResults<Symbol>> then )
    {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                getThread().executeTransactionAsync(transactionRealm -> {
                    RealmResults<Symbol> symbols = transactionRealm.where(Symbol.class).equalTo("id", id).findAll();
                    then.accept(symbols);
                });
            }
        });

        thread.start();

        return thread;
    }

    public static Thread setSymbol( String id, String name, Double movement, Integer notifications, Consumer<Boolean> then )
    {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                getThread().executeTransactionAsync(transactionRealm -> {
                    Symbol symbol = transactionRealm.where(Symbol.class).equalTo("id", id).findFirst();

                    if ( null == symbol ) { // create new
                        symbol = transactionRealm.createObject(Symbol.class, id);
                    }

                    symbol.setName(name);
                    symbol.setMovement(movement);
                    symbol.setNotifications(notifications);
                    then.accept(true);
                });
            }
        });

        thread.start();

        return thread;
    }
}