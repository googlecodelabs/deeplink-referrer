package com.google.samples.search.recipe_app.client;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.appindexing.AndroidAppUri;
import com.google.samples.search.recipe_app.R;

/**
 * This class manages communication with Google Analytics.
 */
public class AnalyticsApplication extends Application {

    private static final String REFERRER_NAME = "android.intent.extra.REFERRER_NAME";
    private static final String QUICK_SEARCH_BOX = "com.google.android.googlequicksearchbox";
    private static final String APP_CRAWLER = "com.google.appcrawler";

    private static final String REFERRER = "utm_source";
    private static final String MEDIUM = "utm_medium";

    private static final String DIRECT = "direct";
    private static final String ORGANIC = "organic";
    private static final String REFERRAL = "referral";

    private Tracker mTracker;

    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.global_tracker);
        }
        return mTracker;
    }

    public synchronized void trackScreenView(Intent intent, String screenName) {
        Tracker tracker = getDefaultTracker();
        tracker.setScreenName(screenName);

        String appReferrerExtra = intent.getStringExtra(REFERRER_NAME);

        // App was opened directly by the user
        if (appReferrerExtra == null) {
            tracker.send(new HitBuilders.ScreenViewBuilder()
                    .set(MEDIUM, DIRECT)
                    .build());
            // App was referred via a deep link
        } else {
            // App was referred from the browser
            if (appReferrerExtra.startsWith("http://") || appReferrerExtra.startsWith("https://")) {
                String host = Uri.parse(appReferrerExtra).getHost();
                if (host.equals("www.google.com")) {
                    tracker.send(new HitBuilders.ScreenViewBuilder()
                            .set(MEDIUM, ORGANIC)
                            .set(REFERRER, "google.com")
                            .build());
                } else {
                    tracker.send(new HitBuilders.ScreenViewBuilder()
                            .set(MEDIUM, REFERRAL)
                            .set(REFERRER, appReferrerExtra)
                            .build());
                }
            }
            // App was referred from another app
            else if (appReferrerExtra.startsWith("android-app://")) {
                AndroidAppUri appUri = AndroidAppUri.newAndroidAppUri(Uri.parse(appReferrerExtra));
                String referrerPackage = appUri.getPackageName();
                // App was opened from the quick search box in Android
                if (QUICK_SEARCH_BOX.equals(referrerPackage)) {
                    tracker.send(new HitBuilders.ScreenViewBuilder()
                            .set(MEDIUM, ORGANIC)
                            .set(REFERRER, "google_app")
                            .build());
                    // App was deep linked into from another app (excl. Google crawler)
                }  else if (!APP_CRAWLER.equals(referrerPackage)) {
                    tracker.send(new HitBuilders.ScreenViewBuilder()
                            .set(MEDIUM, REFERRAL)
                            .set(REFERRER, referrerPackage)
                            .build());
                }
            }
        }

    }
}
