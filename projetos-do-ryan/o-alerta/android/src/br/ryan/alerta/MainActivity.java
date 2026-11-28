package br.ryan.alerta;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.webkit.JavascriptInterface;
import java.util.Locale;
import android.view.View;
import android.view.WindowManager;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/** O Alerta: o jogo roda de dentro do app, sem internet, e as falas saem pela voz do celular. */
public class MainActivity extends Activity {
    private static final String CASA = "appassets.androidplatform.net";
    private static final int PEDIR_CAMERA = 7;
    private WebView web;
    private PermissionRequest pedido;
    private TextToSpeech tts;
    private boolean ttsOk = false;

    /** Ponte para o jogo pedir que o celular fale (o navegador do Android não tem voz própria). */
    public class Ponte {
        @JavascriptInterface public boolean falar(String texto, float tom, float vel) {
            if (!ttsOk || tts == null) return false;
            tts.setPitch(tom); tts.setSpeechRate(vel);
            tts.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "fala");
            return true;
        }
        @JavascriptInterface public void calar() { if (tts != null) tts.stop(); }
    }

    private void avisarFim() {
        if (web == null) return;
        web.post(new Runnable() { public void run() { web.evaluateJavascript("window.__falaFim && window.__falaFim()", null); } });
    }

    private static String tipo(String c) {
        if (c.endsWith(".html")) return "text/html";
        if (c.endsWith(".js") || c.endsWith(".mjs")) return "application/javascript";
        if (c.endsWith(".wasm")) return "application/wasm";
        if (c.endsWith(".png")) return "image/png";
        return "application/octet-stream";
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        web = new WebView(this);
        web.setBackgroundColor(Color.rgb(26, 23, 18));
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
                    if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        pedido = r; requestPermissions(new String[]{ Manifest.permission.CAMERA }, PEDIR_CAMERA);
                    } else r.grant(r.getResources());
                } });
            }
        });
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override public void onInit(int st) {
                if (st != TextToSpeech.SUCCESS) return;
                int r = tts.setLanguage(new Locale("pt", "BR"));
                ttsOk = r != TextToSpeech.LANG_MISSING_DATA && r != TextToSpeech.LANG_NOT_SUPPORTED;
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override public void onStart(String id) {}
                    @Override public void onDone(String id) { avisarFim(); }
                    @Override public void onError(String id) { avisarFim(); }
                });
            }
        });
        web.addJavascriptInterface(new Ponte(), "Android");
        setContentView(web);
        esconderBarras();
        web.loadUrl("https://" + CASA + "/index.html");
    }

    @Override
    public void onRequestPermissionsResult(int req, String[] p, int[] res) {
        if (req != PEDIR_CAMERA || pedido == null) return;
        if (res.length > 0 && res[0] == PackageManager.PERMISSION_GRANTED) pedido.grant(pedido.getResources()); else pedido.deny();
        pedido = null;
    }

    private void esconderBarras() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }
    @Override public void onWindowFocusChanged(boolean f) { super.onWindowFocusChanged(f); if (f) esconderBarras(); }
    @Override public void onBackPressed() { finish(); }
    @Override protected void onDestroy() { if (tts != null) tts.shutdown(); super.onDestroy(); }
}
