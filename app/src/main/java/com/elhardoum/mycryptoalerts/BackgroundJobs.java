package com.elhardoum.mycryptoalerts;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.PersistableBundle;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.elhardoum.mycryptoalerts.viewmodels.Database;
import com.elhardoum.mycryptoalerts.viewmodels.Quote;
import com.elhardoum.mycryptoalerts.viewmodels.Symbol;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;
import java.util.function.Consumer;

public class BackgroundJobs extends JobService {

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.d("JOB", ">> started");

        PersistableBundle bundle = jobParameters.getExtras();

        UpdateQuotes task = new UpdateQuotes();
        task.execute(bundle.getLong("interval_minutes", 15));

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }

    private class UpdateQuotes extends AsyncTask<Long, Integer, String> {
        private HashMap recentQuotes = new HashMap<String, Double>();

        @Override
        protected String doInBackground(Long[] longs) {
            Database.getSymbols(true, items ->
            {
                ArrayList<Symbol> syms = new ArrayList<>(Database.getThread().copyFromRealm(items));
                flushRecentQuotes();

                for ( Symbol item: syms ) {
                    Database.getQuote(item.getCoinId(), quote ->
                    {
                        Double value = quote.getValue();
                        Date fetched = quote.getFetched();

                        boolean update = fetched == null || ((Instant.now().getEpochSecond() - fetched.toInstant().getEpochSecond()) / 60) >= longs[0];

                        if ( update ) {
                            getQuote(item.getCoinId(), newValue ->
                            {
                                if ( null != value ) {
                                    boolean notify = false;

                                    // %
                                    double mvmt = ((100 * newValue) / (value*1d)) - 100;

                                    // Percent > 0 ? movement >= Percent : movement <= Percent
                                    if ( item.getMovement() > 0 ) {
                                        notify = mvmt >= item.getMovement();
                                    } else {
                                        notify = mvmt <= item.getMovement();
                                    }

                                    if ( notify ) {
                                        double mvmtPretty = Math.round(mvmt * 100.0) / 100.0;
                                        Database.incrNotification(
                                                item.getId(),
                                                k -> {},
                                                true
                                        );

                                        showNotification(
                                                Math.abs(new Random().nextInt()),
                                                BackgroundJobs.this,
                                                String.format("%s is %s %s%%",
                                                        item.getSymbol().toUpperCase(Locale.ROOT),
                                                        mvmtPretty > 0 ? "up" : "down",
                                                        Math.abs(mvmtPretty)),
                                                String.format("New quote: %s (%s%%)", newValue, mvmtPretty),
                                                new Intent()
                                        );
                                    }
                                }

                                Database.setQuote(item.getCoinId(), newValue, new Date(), k -> {}, true);
                            });
                        }
                    }, true);
                }
            });

            return null;
        }

        private void getQuote(String id, Consumer<Double> then)
        {
            if ( recentQuotes.containsKey(id) ) {
                Log.d("API", "quote served from cache");
                then.accept(Double.parseDouble(recentQuotes.get(id).toString()));
                return;
            }

            URLConnection connection = null;
            try {
                connection = new URL(String.format("https://api.coingecko.com/api/v3/simple/price?ids=%s&vs_currencies=usd", id))
                        .openConnection();
            } catch (IOException e) {
                Log.e("API", "Error getting url", e);
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
            JSONObject obj;

            try {
                obj = new JSONObject(response);
            } catch (JSONException e) {
                Log.e("API", "Error parsing json", e);
                then.accept(null);
                return;
            }

            try {
                String usd = obj.getJSONObject(id).get("usd").toString();
                then.accept(Double.parseDouble(usd));
            } catch (JSONException e) {
                Log.e("API", "Error parsing json", e);
                then.accept(null);
                return;
            }
        }

        private void flushRecentQuotes()
        {
            recentQuotes = new HashMap<String, Double>();
        }
    }

    private void showNotification(int notificationId, Context context, String title, String body, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = "my-crypto-alerts";
        String channelName = "My Crypto Alerts";
        int importance = NotificationManager.IMPORTANCE_HIGH;

        NotificationChannel mChannel = new NotificationChannel(
                channelId, channelName, importance);
        notificationManager.createNotificationChannel(mChannel);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.info_icon)
                .setContentTitle(title)
                .setContentText(body);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_MUTABLE
        );
        mBuilder.setContentIntent(resultPendingIntent);

        notificationManager.notify(notificationId, mBuilder.build());
    }
}
