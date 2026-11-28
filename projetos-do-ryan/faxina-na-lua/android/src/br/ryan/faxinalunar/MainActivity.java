package br.ryan.faxinalunar;

import android.app.Activity;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/** Faxina na Lua: o jogo roda de dentro do app, sem precisar de internet. */
public class MainActivity extends Activity {
    private static final String CASA = "appassets.androidplatform.net";
    private WebView web;

    private static String tipo(String c) {
        if (c.endsWith(".html")) return "text/html";
        if (c.endsWith(".js")) return "application/javascript";
        if (c.endsWith(".png")) return "image/png";
        return "application/octet-stream";
    }

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        web = new WebView(this);
        web.setBackgroundColor(Color.rgb(5, 7, 13));
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true); s.setMediaPlaybackRequiresUserGesture(false);
        web.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView v, String url) { return url == null || !url.startsWith("https://" + CASA); }
            @Override public WebResourceResponse shouldInterceptRequest(WebView v, WebResourceRequest req) {
                Uri u = req.getUrl();
                if (!"https".equals(u.getScheme()) || !CASA.equals(u.getHost())) return null;
                String c = u.getPath(); if (c == null || c.equals("/")) c = "/index.html";
                try {
                    InputStream in = getAssets().open(c.substring(1));
                    Map<String, String> cab = new HashMap<String, String>(); cab.put("Access-Control-Allow-Origin", "*");
                    return new WebResourceResponse(tipo(c), "UTF-8", 200, "OK", cab, in);
                } catch (Exception e) { return new WebResourceResponse("text/plain", "UTF-8", 404, "Not Found", new HashMap<String, String>(), null); }
            }
        });
        setContentView(web);
        esconderBarras();
        web.loadUrl("https://" + CASA + "/index.html");
    }

    private void esconderBarras() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }
    @Override public void onWindowFocusChanged(boolean f) { super.onWindowFocusChanged(f); if (f) esconderBarras(); }
    @Override protected void onPause() { super.onPause(); if (web != null) web.evaluateJavascript("window.__lua && window.__lua.salvar()", null); }
    @Override public void onBackPressed() { finish(); }
}
