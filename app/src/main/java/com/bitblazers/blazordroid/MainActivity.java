package com.bitblazers.blazordroid;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.bitblazers.blazordroid.Core;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private AdView BannerAdView;
    private InterstitialAd InterstitialAdView;
    private RewardedAd RewardAdView;

    private android.webkit.WebView WebView;

    private Button Button1;
    private Button Button2;

    private String openUrl = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        Core.AdDisabled = Core.readFileAsString(getApplicationContext(), Core.AdDisableFile).contains((String) "ture");
        Core.ToastEnabled = Core.readFileAsString(getApplicationContext(), Core.ToastEnableFile).contains((String) "ture");
        InitServer();
        InitAds();
        try {
            openUrl = getIntent().getData().toString().replace("blazordroidapp", "https");
        } catch (Exception e) {

        }
        InitWebView(openUrl);
        Core.LastAdTime = 0;
        Core.AdResponse = "0";
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            Log.e("onStart ", getIntent().getData().toString());
        } catch (Exception e) {

        }
    }

    private void InitServer() {
        HttpServer server = null;
        try {
            server = HttpServer.create(new InetSocketAddress(35647), 0);
            server.createContext("/ShowAD", new AdHandler());
            server.createContext("/Cache", new CacheHandler());
            server.createContext("/File", new FileHandler());
            server.createContext("/Share", new ShareHandler());
            server.createContext("/HandShake", new HandShakeHandler());
            server.createContext("/OpenURL", new UrlHandler());
            server.setExecutor(null); // creates a default executor
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void InitAds() {
        // List<String> testDeviceIds = Arrays.asList("EA876987A8B78DACF31819F92843B70A");
        // RequestConfiguration configuration = new RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build();
        // MobileAds.setRequestConfiguration(configuration);
        // RequestConfiguration requestConfiguration = MobileAds.getRequestConfiguration().toBuilder()
        //        .setTagForChildDirectedTreatment(RequestConfiguration. TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE )
        //         //.setTagForUnderAgeOfConsent( TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE )
        //         .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)
        //         .build();

        //  MobileAds.setRequestConfiguration( requestConfiguration );
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        MobileAds.getRewardedVideoAdInstance(this);
        BannerAdView = findViewById(R.id.adView);
        BannerAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // ShowToast(getApplicationContext(), "BannerAd Loaded", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                ShowToast(getApplicationContext(), "BannerAd Loaded Failed. Code : " + String.valueOf(errorCode), Toast.LENGTH_LONG);
            }

            @Override
            public void onAdClosed() {
                BannerAdView.loadAd(Core.GetNewAdRequest());
            }
        });
        BannerAdView.loadAd(Core.GetNewAdRequest());

        InterstitialAdView = new InterstitialAd(this);
        InterstitialAdView.setAdUnitId(Core.InterstitialID);
        InterstitialAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // ShowToast(getApplicationContext(), "InterstitialAd Loaded", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                ShowToast(getApplicationContext(), "InterstitialAd Loaded Failed. Code : " + String.valueOf(errorCode), Toast.LENGTH_LONG);
            }

            @Override
            public void onAdClosed() {
                // Proceed to the next level.
                // ShowToast(getApplicationContext(), "InterstitialAd Closed", Toast.LENGTH_LONG).show();
                LoadInterstitialAd();
            }
        });

        RewardAdView = new RewardedAd(this, Core.RewardID);
    }

    private void InitWebView(String url) {
        WebView = findViewById(R.id.webview1);
        WebView.getSettings().setJavaScriptEnabled(true);
        WebView.getSettings().setAppCacheEnabled(true);
        WebView.getSettings().setDomStorageEnabled(true);
        WebView.getSettings().setAllowFileAccess(true);
        WebView.getSettings().setAllowContentAccess(true);
        WebView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        //WebView.clearCache(true);
        //WebView.clearFormData();
        //WebView.clearHistory();
        // WebView.getSettings().setAllowFileAccessFromFileURLs(true);
        // WebView.getSettings().setAllowFileAccess(true);
        // WebView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        // WebView.getSettings().setAllowContentAccess(true);
        // WebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        // WebView.getSettings().setBlockNetworkLoads(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WebView.getSettings().setSafeBrowsingEnabled(false);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            WebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        WebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(android.webkit.WebView view, String url) {
            }

            @Override
            public void onReceivedError(android.webkit.WebView view, WebResourceRequest request, WebResourceError error) {
            }

            @Override
            public void onReceivedHttpError(android.webkit.WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            }

            @Override
            public void onReceivedSslError(android.webkit.WebView view, SslErrorHandler handler, SslError error) {
            }
        });
        WebView.loadUrl("https://www.towergame.app/");
        if (url != null) WebView.loadUrl(url);
    }

    void LoadInterstitialAd() {
        InterstitialAdView.loadAd(new AdRequest.Builder().build());
    }

    void LoadRewardAd() {
        RewardAdView.loadAd(new AdRequest.Builder().build(), new RewardedAdLoadCallback() {
            @Override
            public void onRewardedAdFailedToLoad(int i) {
                super.onRewardedAdFailedToLoad(i);
                ShowToast(getApplicationContext(), "RewardAd Loaded Failed. Code : " + String.valueOf(i), Toast.LENGTH_LONG);
            }

            @Override
            public void onRewardedAdLoaded() {
                // ShowToast(getApplicationContext(), "RewardAd Loaded", Toast.LENGTH_LONG).show();
            }
        });

    }

    class AdHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            Headers Headers = t.getResponseHeaders();
            Headers.add("Access-Control-Allow-Origin", "*");
            final Map<String, String> res = queryToMap(t.getRequestURI().getQuery());
            //   if (res.containsKey("type")) {
            //     MainActivity.this.runOnUiThread(new Runnable() {
            //       public void run() {
            //            Core.AdResponse = Ads(res.get("type"));
            //        }
            //    });
            // }
            long tmp = Core.LastAdTime + Core.AdDelay * 1000;
            // Log.d("Tmp", String.valueOf(tmp));
            // Log.d("Current", String.valueOf(Calendar.getInstance().getTimeInMillis()));
            if (res.containsKey("type")) {
                String T = res.get("type");
                if (T.equals((String) "Interstitial")) {
                    if (Core.AdDisabled) {
                        Core.AdResponse = "1";
                    } else {
                        MainActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                if (InterstitialAdView.isLoaded()) {
                                    Core.AdResponse = "1";
                                    ShowToast(MainActivity.this, "Showing Interstitial Ad", Toast.LENGTH_LONG);
                                    InterstitialAdView.show();
                                } else {
                                    Core.AdResponse = "0";
                                    LoadInterstitialAd();
                                }
                            }
                        });
                    }
                } else if (T.equals((String) "Reward")) {
                    if (Core.AdDisabled) {
                        Core.AdResponse = "3";
                    } else {
                        if (tmp < Calendar.getInstance().getTimeInMillis()) {
                            Log.d("Showing Ad", "Reward");
                            MainActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    if (RewardAdView.isLoaded()) {
                                        Core.AdResponse = "1";
                                        ShowToast(MainActivity.this, "Showing Reward Ad", Toast.LENGTH_LONG);
                                        RewardAdView.show(MainActivity.this, new RewardedAdCallback() {
                                            @Override
                                            public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                                                Core.AdResponse = "2";
                                                Core.LastAdTime = Calendar.getInstance().getTimeInMillis();
                                                ShowToast(MainActivity.this, "Reward Ad Watched", Toast.LENGTH_LONG);
                                                LoadRewardAd();
                                            }

                                            @Override
                                            public void onRewardedAdClosed() {
                                                super.onRewardedAdClosed();
                                                LoadRewardAd();
                                            }

                                            @Override
                                            public void onRewardedAdFailedToShow(int i) {
                                                super.onRewardedAdFailedToShow(i);
                                                LoadRewardAd();
                                            }
                                        });
                                    } else {
                                        Core.AdResponse = "0";
                                        LoadRewardAd();
                                    }
                                }
                            });
                        } else {
                            if (Core.AdResponse != "2") Core.AdResponse = "3";
                        }
                    }
                } else {
                    //  ShowToast(getApplicationContext(), "Ad Type Not Found", Toast.LENGTH_LONG).show();
                    Core.AdResponse = "-1";
                }
            }
            t.sendResponseHeaders(200, Core.AdResponse.length());
            OutputStream os = t.getResponseBody();
            os.write(Core.AdResponse.getBytes());
            os.close();
        }
    }

    class CacheHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            Headers Headers = t.getResponseHeaders();
            Headers.add("Access-Control-Allow-Origin", "*");
            final Map<String, String> res = queryToMap(t.getRequestURI().getQuery());
            String Msg = "1";
            if (res.containsKey("action")) {
                String T = res.get("action");
                if (T.equals((String) "clear")) {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            WebView.getSettings().setAppCacheEnabled(false);
                            WebView.clearCache(true);
                            WebView.clearFormData();
                            WebView.clearHistory();
                            WebView.getSettings().setAppCacheEnabled(true);
                        }
                    });
                } else if (T.equals((String) "clear-reload")) {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            WebView.getSettings().setAppCacheEnabled(false);
                            WebView.clearCache(true);
                            WebView.clearFormData();
                            WebView.clearHistory();
                            WebView.getSettings().setAppCacheEnabled(true);
                            WebView.reload();
                        }
                    });
                } else Msg = "0";
            } else Msg = "0";
            t.sendResponseHeaders(200, Msg.length());
            OutputStream os = t.getResponseBody();
            os.write(Msg.getBytes());
            os.close();
        }
    }

    class FileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            Headers Headers = t.getResponseHeaders();
            Headers.add("Access-Control-Allow-Origin", "*");
            final Map<String, String> res = queryToMap(t.getRequestURI().getQuery());
            Core.FileResponse = "$NF";
            String Action = null;
            String FileName = null;
            if (res.containsKey("action")) {
                Action = res.get("action");
            }
            if (res.containsKey("file")) {
                FileName = res.get("file");
            }
            if (!Action.isEmpty() && !FileName.isEmpty()) {
                if (Action.equals((String) "get")) {
                    Core.FileResponse = Core.readFileAsString(getApplicationContext(), FileName).replaceAll("null", "");
                    if (Core.FileResponse == null || Core.FileResponse.isEmpty()) {
                        Core.FileResponse = "$NULL";
                    }
                } else if (Action.equals((String) "set")) {
                    BufferedReader httpInput = new BufferedReader(new InputStreamReader(t.getRequestBody(), "UTF-8"));
                    StringBuilder in = new StringBuilder();
                    String input;
                    while ((input = httpInput.readLine()) != null) {
                        in.append(input);
                    }
                    httpInput.close();
                    boolean v = Core.writeStringAsFile(getApplicationContext(), FileName, in.toString());
                    Core.FileResponse = v ? "$OK" : "$ER";
                }
            }
            t.sendResponseHeaders(200, Core.FileResponse.length());
            OutputStream os = t.getResponseBody();
            os.write(Core.FileResponse.getBytes());
            os.close();
        }
    }

    class ShareHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            Headers Headers = t.getResponseHeaders();
            Headers.add("Access-Control-Allow-Origin", "*");
            Core.FileResponse = "$OK";

            BufferedReader httpInput = new BufferedReader(new InputStreamReader(t.getRequestBody(), "UTF-8"));
            final StringBuilder in = new StringBuilder();
            String input;
            while ((input = httpInput.readLine()) != null) {
                in.append(input);
            }
            httpInput.close();
            Log.e("Share", "sharing...............");
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Log.e("Share", "sharing");
                    Core.Share(MainActivity.this, in.toString());
                }
            });
            t.sendResponseHeaders(200, Core.FileResponse.length());
            OutputStream os = t.getResponseBody();
            os.write(Core.FileResponse.getBytes());
            os.close();
        }
    }

    class HandShakeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            Headers Headers = t.getResponseHeaders();
            Headers.add("Access-Control-Allow-Origin", "*");
            final Map<String, String> res = queryToMap(t.getRequestURI().getQuery());
            String Msg = "$NF";
            if (res.containsKey("val")) {
                String T = res.get("val");
                try {
                    int v = Integer.parseInt(T);
                    Msg = String.valueOf((((v * 156) + 3)) - 8);
                } catch (Exception e) {

                }
            }
            t.sendResponseHeaders(200, Msg.length());
            OutputStream os = t.getResponseBody();
            os.write(Msg.getBytes());
            os.close();
        }
    }

    class UrlHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            Headers Headers = t.getResponseHeaders();
            Headers.add("Access-Control-Allow-Origin", "*");
            final Map<String, String> res = queryToMap(t.getRequestURI().getQuery());
            String Msg = "$NF";
            if (res.containsKey("link")) {
                String T = res.get("link");
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(T));
                    startActivity(browserIntent);
                    Msg = "$OK";
                } catch (Exception ex) {

                }
            }
            t.sendResponseHeaders(200, Msg.length());
            OutputStream os = t.getResponseBody();
            os.write(Msg.getBytes());
            os.close();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!InterstitialAdView.isLoaded()) {
            LoadInterstitialAd();
        }
        if (!RewardAdView.isLoaded()) {
            LoadRewardAd();
        }
    }

    @Override
    public void onBackPressed() {
        if (WebView.canGoBack()) WebView.goBack();
        else if (openUrl != null) {
            WebView.loadUrl("https://www.towergame.app/");
            openUrl = null;
        } else super.onBackPressed();
    }

    public Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<>();
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(entry[0], entry[1]);
            } else {
                result.put(entry[0], "");
            }
        }
        return result;
    }

    String Ads(String T) {
        String response1;
        if (T.equals((String) "Interstitial")) {
            response1 = "1";
            ShowToast(MainActivity.this, "Showing Interstitial Ad", Toast.LENGTH_LONG);
            if (InterstitialAdView.isLoaded()) {
                InterstitialAdView.show();
            } else {
                LoadInterstitialAd();
            }
        } else if (T.equals((String) "Reward")) {
            Log.d("Showing Ad", "Reward");
            response1 = "1";
            ShowToast(MainActivity.this, "Showing Reward Ad", Toast.LENGTH_LONG);
            if (RewardAdView.isLoaded()) {
                RewardAdView.show(MainActivity.this, new RewardedAdCallback() {
                    @Override
                    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                        //  response1 = "2";
                    }
                });
            } else {
                LoadRewardAd();
            }
        } else {
            //  ShowToast(getApplicationContext(), "Ad Type Not Found", Toast.LENGTH_LONG).show();
            response1 = "-1";
        }
        return response1;
    }

    public void ShowToast(final Context c, final String msg, final int l) {
        if (Core.ToastEnabled) {
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(c, msg, l).show();
                }
            });
        }
    }
}