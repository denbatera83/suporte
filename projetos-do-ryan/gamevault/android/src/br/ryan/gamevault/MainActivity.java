package br.ryan.gamevault;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/** GameVault: a biblioteca roda de dentro do app. Links de fora (como o da nuvem no claude.ai) abrem no navegador. */
public class MainActivity extends Activity {
    private static final String CASA = "appassets.androidplatform.net";
    private static final int ESCOLHER = 11;
    private WebView web;
    private ValueCallback<Uri[]> escolha;

    private static String tipo(String c) {
        if (c.endsWith(".html")) return "text/html";
        if (c.endsWith(".js")) return "application/javascript";
        if (c.endsWith(".png")) return "image/png";
        return "application/octet-stream";
    }

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        web = new WebView(this);
        web.setBackgroundColor(Color.rgb(11, 18, 32));
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true); s.setDatabaseEnabled(true); s.setMediaPlaybackRequiresUserGesture(false);
        web.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView v, String url) {
                if (url == null || url.startsWith("https://" + CASA)) return false;
                if (url.startsWith("about:")) return false;
                abrirFora(url); return true;
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
        web.setWebChromeClient(new WebChromeClient() {
            // links com target="_blank" também abrem no navegador
            @Override public boolean onCreateWindow(WebView v, boolean dialog, boolean gesto, android.os.Message msg) {
                WebView.HitTestResult r = v.getHitTestResult(); String url = r != null ? r.getExtra() : null;
                if (url != null) abrirFora(url);
                return false;
            }
            // botão "Selecionar arquivo" e "Importar backup"
            @Override public boolean onShowFileChooser(WebView v, ValueCallback<Uri[]> cb, FileChooserParams p) {
                if (escolha != null) escolha.onReceiveValue(null);
                escolha = cb;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT); i.addCategory(Intent.CATEGORY_OPENABLE); i.setType("*/*");
                if (p != null && p.getMode() == FileChooserParams.MODE_OPEN_MULTIPLE) i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                try { startActivityForResult(Intent.createChooser(i, "Escolher arquivo"), ESCOLHER); } catch (Exception e) { escolha = null; return false; }
                return true;
            }
        });
        s.setSupportMultipleWindows(true);
        web.addJavascriptInterface(new Object() {
            /** Salva o backup (.json) na pasta Downloads. */
            @JavascriptInterface public String salvarArquivo(String nome, String texto) {
                try {
                    byte[] b = texto.getBytes("UTF-8");
                    if (Build.VERSION.SDK_INT >= 29) {
                        ContentValues cv = new ContentValues(); cv.put("_display_name", nome); cv.put("mime_type", "application/json"); cv.put("relative_path", "Download/GameVault");
                        Uri u = getContentResolver().insert(Uri.parse("content://media/external/downloads"), cv); OutputStream o = getContentResolver().openOutputStream(u); o.write(b); o.close();
                        return "Downloads/GameVault";
                    }
                    File d = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS); if (d == null) d = getFilesDir(); File f = new File(d, nome); FileOutputStream o = new FileOutputStream(f); o.write(b); o.close();
                    return f.getAbsolutePath();
                } catch (Exception e) { return ""; }
            }
            @JavascriptInterface public void abrir(String url) { abrirFora(url); }
        }, "GameVaultApp");
        setContentView(web);
        web.loadUrl("https://" + CASA + "/index.html");
    }

    private void abrirFora(final String url) {
        runOnUiThread(new Runnable() { public void run() {
            try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); } catch (Exception e) { }
        } });
    }

    @Override protected void onActivityResult(int req, int res, Intent data) {
        if (req != ESCOLHER || escolha == null) { super.onActivityResult(req, res, data); return; }
        Uri[] r = null;
        if (res == RESULT_OK && data != null) {
            if (data.getClipData() != null) { int n = data.getClipData().getItemCount(); r = new Uri[n]; for (int i = 0; i < n; i++) r[i] = data.getClipData().getItemAt(i).getUri(); }
            else if (data.getData() != null) r = new Uri[]{ data.getData() };
        }
        escolha.onReceiveValue(r); escolha = null;
    }

    @Override public void onBackPressed() { if (web != null && web.canGoBack()) web.goBack(); else finish(); }
}
