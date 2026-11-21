package br.ryan.ultimoabrigo;

import android.app.Activity;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.webkit.JavascriptInterface;
import java.io.InputStream;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;

/** Último Abrigo: o jogo roda de dentro do app, sem precisar de internet. */
public class MainActivity extends Activity {
    private static final String CASA = "appassets.androidplatform.net";
    private WebView web;
    private PermissionRequest pedido;
    private TextToSpeech voz;
    private boolean vozPronta = false;

    /** Voz da ÍRIS: o WebView do Android não tem voz própria, então usamos a voz do sistema. */
    public class Voz {
        @JavascriptInterface public boolean pronta() { return vozPronta; }
        @JavascriptInterface public void falar(String t) { if (voz != null && vozPronta && t != null) voz.speak(t, TextToSpeech.QUEUE_FLUSH, null, "iris"); }
        @JavascriptInterface public void parar() { if (voz != null) voz.stop(); }
    }
    private void avisarJs(final boolean f) { if (web != null) web.post(new Runnable() { public void run() { web.evaluateJavascript("window.__irisFalando && window.__irisFalando(" + f + ")", null); } }); }

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
        web.setBackgroundColor(Color.rgb(8, 10, 8));
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
        web.setWebChromeClient(new WebChromeClient() {
            @Override public void onPermissionRequest(final PermissionRequest r) {
                runOnUiThread(new Runnable() { public void run() {
                    if (Build.VERSION.SDK_INT < 23 || checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) r.grant(r.getResources());
                    else { pedido = r; requestPermissions(new String[]{ Manifest.permission.RECORD_AUDIO }, 7); }
                } });
            }
        });
        voz = new TextToSpeech(this, new TextToSpeech.OnInitListener() { public void onInit(int st) {
            if (st != TextToSpeech.SUCCESS) return;
            int r = voz.setLanguage(new Locale("pt", "BR")); if (r == TextToSpeech.LANG_MISSING_DATA || r == TextToSpeech.LANG_NOT_SUPPORTED) voz.setLanguage(new Locale("pt"));
            voz.setPitch(1.2f); voz.setSpeechRate(1.05f); vozPronta = true;
            voz.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override public void onStart(String id) { avisarJs(true); }
                @Override public void onDone(String id) { avisarJs(false); }
                @Override public void onError(String id) { avisarJs(false); }
            });
        } });
        web.addJavascriptInterface(new Voz(), "IrisVoz");
        setContentView(web);
        esconderBarras();
        web.loadUrl("https://" + CASA + "/index.html");
    }

    private void esconderBarras() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }
    @Override public void onWindowFocusChanged(boolean f) { super.onWindowFocusChanged(f); if (f) esconderBarras(); }
    @Override protected void onPause() { super.onPause(); if (web != null) web.evaluateJavascript("window.__abrigo && window.__abrigo.salvar()", null); }
    @Override public void onRequestPermissionsResult(int cod, String[] p, int[] res) {
        if (cod != 7 || pedido == null) return;
        if (res.length > 0 && res[0] == PackageManager.PERMISSION_GRANTED) pedido.grant(pedido.getResources()); else pedido.deny();
        pedido = null;
    }
    @Override protected void onDestroy() { if (voz != null) voz.shutdown(); super.onDestroy(); }
    @Override public void onBackPressed() { finish(); }
}
