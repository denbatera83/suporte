package br.familia.sos;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.json.JSONArray;

/** SOS Família: o botão vermelho manda SMS com a localização sozinho e depois liga. */
public class MainActivity extends Activity {
    private static final String CASA = "appassets.androidplatform.net";
    static MainActivity atual;
    private WebView web;
    private final Handler h = new Handler(Looper.getMainLooper());
    private int lote = 0;

    static final String[] PERMISSOES = { Manifest.permission.SEND_SMS, Manifest.permission.CALL_PHONE,
        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION };

    private boolean tem(String p) { return Build.VERSION.SDK_INT < 23 || checkSelfPermission(p) == PackageManager.PERMISSION_GRANTED; }

    /** Roda um pedaço de JavaScript na página (chamado pelos avisos do SMS e da localização). */
    static void js(final String codigo) {
        final MainActivity a = atual; if (a == null || a.web == null) return;
        a.web.post(new Runnable() { public void run() { a.web.evaluateJavascript(codigo, null); } });
    }

    public class Ponte {
        @JavascriptInterface public boolean temPermissoes() { for (String p : PERMISSOES) if (!tem(p)) return false; return true; }
        @JavascriptInterface public void pedirPermissoes() { h.post(new Runnable() { public void run() { if (Build.VERSION.SDK_INT >= 23) requestPermissions(PERMISSOES, 1); } }); }
        /** Se o SMS automático não sair, abre o app de mensagens com tudo pronto (só falta tocar em Enviar). */
        @JavascriptInterface public void abrirSMS(final String numerosJson, final String texto) { h.post(new Runnable() { public void run() { abrirMensagens(numerosJson, texto); } }); }
        @JavascriptInterface public void vibrar(long ms) { try { Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE); if (ms <= 0) v.cancel(); else v.vibrate(ms); } catch (Exception e) { } }
        @JavascriptInterface public void ligar(final String numero) { h.post(new Runnable() { public void run() { fazerLigacao(numero); } }); }
        @JavascriptInterface public void pegarLocal() { h.post(new Runnable() { public void run() { buscarLocal(); } }); }
        @JavascriptInterface public void enviarSMS(final String numerosJson, final String texto) { h.post(new Runnable() { public void run() { mandarSMS(numerosJson, texto); } }); }
    }

    void abrirMensagens(String numerosJson, String texto) {
        try {
            JSONArray a = new JSONArray(numerosJson); StringBuilder nums = new StringBuilder();
            for (int i = 0; i < a.length(); i++) { if (i > 0) nums.append(';'); nums.append(a.getString(i)); }
            Intent it = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + nums)); it.putExtra("sms_body", texto); startActivity(it);
        } catch (Exception e) { }
    }

    void fazerLigacao(String numero) {
        Uri u = Uri.parse("tel:" + numero);
        try { startActivity(new Intent(tem(Manifest.permission.CALL_PHONE) ? Intent.ACTION_CALL : Intent.ACTION_DIAL, u)); }
        catch (Exception e) { try { startActivity(new Intent(Intent.ACTION_DIAL, u)); } catch (Exception e2) { } }
    }

    /** Manda o mesmo SMS para cada número; cada parte que sai (ou falha) avisa a página. */
    void mandarSMS(String numerosJson, String texto) {
        lote++;
        try {
            JSONArray a = new JSONArray(numerosJson); SmsManager sm = SmsManager.getDefault(); ArrayList<String> partes = sm.divideMessage(texto);
            for (int i = 0; i < a.length(); i++) {
                ArrayList<PendingIntent> saidas = new ArrayList<PendingIntent>();
                for (int k = 0; k < partes.size(); k++) {
                    Intent it = new Intent(this, SmsEnviado.class); it.putExtra("i", i); it.putExtra("k", k); it.putExtra("n", partes.size());
                    saidas.add(PendingIntent.getBroadcast(this, (lote % 1000) * 1000 + i * 50 + k, it, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));
                }
                try {
                    if (!tem(Manifest.permission.SEND_SMS)) throw new SecurityException("sem permissão");
                    sm.sendMultipartTextMessage(a.getString(i), null, partes, saidas, null);
                } catch (Exception e) { js("window.__smsEnviado && __smsEnviado(" + i + ",0,1,false)"); }
            }
        } catch (Exception e) { js("window.__smsEnviado && __smsEnviado(0,0,1,false)"); }
    }

    // ---------- localização: a melhor que aparecer em até 9 segundos (ou a última conhecida)
    private Location melhor; private boolean respondeu;
    void buscarLocal() {
        respondeu = false; melhor = null;
        if (!tem(Manifest.permission.ACCESS_FINE_LOCATION) && !tem(Manifest.permission.ACCESS_COARSE_LOCATION)) { responderLocal(); return; }
        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        String[] fontes = { LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER };
        for (String f : fontes) { try { Location l = lm.getLastKnownLocation(f); if (l != null && System.currentTimeMillis() - l.getTime() < 10 * 60 * 1000) considerar(l); } catch (Exception e) { } }
        final LocationListener ouvinte = new LocationListener() {
            public void onLocationChanged(Location l) { considerar(l); if (l.hasAccuracy() && l.getAccuracy() <= 25 && !respondeu) { try { lm.removeUpdates(this); } catch (Exception e) { } responderLocal(); } }
            public void onStatusChanged(String p, int s, Bundle b) { }
            public void onProviderEnabled(String p) { }
            public void onProviderDisabled(String p) { }
        };
        for (int i = 0; i < 2; i++) { try { lm.requestLocationUpdates(fontes[i], 1000, 0, ouvinte, Looper.getMainLooper()); } catch (Exception e) { } }
        h.postDelayed(new Runnable() { public void run() { try { lm.removeUpdates(ouvinte); } catch (Exception e) { }
            if (!respondeu) { if (melhor == null) { for (String f : new String[] { LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER }) { try { Location l = lm.getLastKnownLocation(f); if (l != null) considerar(l); } catch (Exception e) { } } } responderLocal(); } } }, 9000);
    }
    void considerar(Location l) { if (l == null) return; if (melhor == null || (l.hasAccuracy() && (!melhor.hasAccuracy() || l.getAccuracy() < melhor.getAccuracy()))) melhor = l; }
    void responderLocal() {
        if (respondeu) return; respondeu = true;
        if (melhor == null) js("window.__local && __local(null)");
        else js(String.format(Locale.US, "window.__local && __local(%.6f,%.6f,%.0f)", melhor.getLatitude(), melhor.getLongitude(), melhor.hasAccuracy() ? melhor.getAccuracy() : 50f));
    }

    private static String tipo(String c) { if (c.endsWith(".html")) return "text/html"; if (c.endsWith(".js")) return "application/javascript"; if (c.endsWith(".png")) return "image/png"; return "application/octet-stream"; }

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        atual = this;
        web = new WebView(this);
        web.setBackgroundColor(Color.rgb(236, 239, 242));
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true); s.setMediaPlaybackRequiresUserGesture(false);
        web.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView v, String url) {
                if (url != null && url.startsWith("https://" + CASA)) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); } catch (Exception e) { }
                return true;
            }
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
        web.addJavascriptInterface(new Ponte(), "Android");
        setContentView(web);
        web.loadUrl("https://" + CASA + "/index.html");
        if (Build.VERSION.SDK_INT >= 23) { boolean falta = false; for (String p : PERMISSOES) if (!tem(p)) falta = true; if (falta) requestPermissions(PERMISSOES, 1); }
    }

    @Override public void onRequestPermissionsResult(int cod, String[] p, int[] r) { js("window.__permissoes && __permissoes()"); }
    @Override protected void onResume() { super.onResume(); atual = this; js("window.__voltou && __voltou()"); }
    @Override protected void onDestroy() { if (atual == this) atual = null; super.onDestroy(); }
    @Override public void onBackPressed() { if (web != null && web.canGoBack()) web.goBack(); else super.onBackPressed(); }
}
