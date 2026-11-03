package br.ryan.vr;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.webkit.PermissionRequest;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

/** Ryan VR: abre o saguão de realidade virtual (HTML + A-Frame) em tela cheia, deitado, para o Cardboard. */
public class MainActivity extends Activity {
    private static final int ESCOLHER_VIDEO = 42, PEDIR_CAMERA = 7;
    // as páginas abrem num endereço seguro interno (https://ryan.vr/...) entregue de dentro do app:
    // assim a câmera e o reconhecimento da mão funcionam, sem precisar de internet
    // endereço reservado pelo Android para páginas de dentro do app (o mesmo do WebViewAssetLoader oficial)
    private static final String CASA = "appassets.androidplatform.net";
    private boolean usouReserva = false;
    private PermissionRequest pedidoCamera;
    private WebView web;
    private FrameLayout raiz;
    private View telaCheia;
    private WebChromeClient.CustomViewCallback avisoSaida;
    private ValueCallback<Uri[]> escolha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        WebView.setWebContentsDebuggingEnabled(true); // permite ver erros pelo chrome://inspect no computador
        raiz = new FrameLayout(this);
        raiz.setBackgroundColor(Color.rgb(26, 22, 48));
        web = new WebView(this);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowFileAccessFromFileURLs(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        web.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView v, WebResourceRequest req) {
                Uri u = req.getUrl();
                if (!"https".equals(u.getScheme()) || !CASA.equals(u.getHost())) return null;
                String caminho = u.getPath(); if (caminho == null || caminho.equals("/")) caminho = "/ryan-vr/index.html";
                if (caminho.endsWith("/")) caminho += "index.html";
                try {
                    InputStream in = getAssets().open(caminho.substring(1));
                    Map<String, String> cab = new HashMap<String, String>();
                    cab.put("Access-Control-Allow-Origin", "*");
                    cab.put("Cache-Control", "no-cache");
                    return new WebResourceResponse(tipo(caminho), "UTF-8", 200, "OK", cab, in);
                } catch (Exception e) {
                    return new WebResourceResponse("text/plain", "UTF-8", 404, "Not Found", new HashMap<String, String>(), null);
                }
            }
            // se a página principal não abrir pelo endereço seguro, abre a cópia de reserva direto dos arquivos
            @Override
            public void onReceivedError(WebView v, WebResourceRequest req, WebResourceError err) {
                if (req.isForMainFrame() && !usouReserva) {
                    usouReserva = true;
                    String c = req.getUrl().getPath();
                    v.loadUrl("file:///android_asset" + (c == null || c.equals("/") ? "/ryan-vr/index.html" : c));
                }
            }
        });
        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                telaCheia = view; avisoSaida = callback;
                raiz.addView(view, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                esconderBarras();
            }
            @Override
            public void onHideCustomView() {
                if (telaCheia != null) { raiz.removeView(telaCheia); telaCheia = null; }
                esconderBarras();
            }
            // Modo Casa: a página pede a câmera; o app pede a permissão do Android se ainda não tiver
            @Override
            public void onPermissionRequest(final PermissionRequest r) {
                runOnUiThread(new Runnable() { public void run() {
                    boolean querCamera = false;
                    for (String x : r.getResources()) if (PermissionRequest.RESOURCE_VIDEO_CAPTURE.equals(x)) querCamera = true;
                    if (!querCamera) { r.deny(); return; }
                    if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        pedidoCamera = r; requestPermissions(new String[]{ Manifest.permission.CAMERA }, PEDIR_CAMERA);
                    } else r.grant(new String[]{ PermissionRequest.RESOURCE_VIDEO_CAPTURE });
                }});
            }
            // "Meus vídeos": abre a galeria para escolher um vídeo
            @Override
            public boolean onShowFileChooser(WebView w, ValueCallback<Uri[]> cb, FileChooserParams p) {
                if (escolha != null) escolha.onReceiveValue(null);
                escolha = cb;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("video/*");
                try { startActivityForResult(Intent.createChooser(i, "Escolha um vídeo"), ESCOLHER_VIDEO); }
                catch (Exception e) { escolha = null; return false; }
                return true;
            }
        });
        raiz.addView(web, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(raiz);
        esconderBarras();
        web.loadUrl("https://" + CASA + "/ryan-vr/index.html");
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req != ESCOLHER_VIDEO || escolha == null) return;
        Uri[] r = null;
        if (res == RESULT_OK && data != null && data.getData() != null) r = new Uri[]{ data.getData() };
        escolha.onReceiveValue(r);
        escolha = null;
        esconderBarras();
    }

    @Override
    public void onRequestPermissionsResult(int req, String[] perms, int[] res) {
        if (req != PEDIR_CAMERA || pedidoCamera == null) return;
        if (res.length > 0 && res[0] == PackageManager.PERMISSION_GRANTED) pedidoCamera.grant(new String[]{ PermissionRequest.RESOURCE_VIDEO_CAPTURE });
        else pedidoCamera.deny();
        pedidoCamera = null;
        esconderBarras();
    }

    private static String tipo(String c) {
        c = c.toLowerCase();
        if (c.endsWith(".html")) return "text/html";
        if (c.endsWith(".js") || c.endsWith(".mjs")) return "application/javascript";
        if (c.endsWith(".wasm")) return "application/wasm";
        if (c.endsWith(".json")) return "application/json";
        if (c.endsWith(".png")) return "image/png";
        if (c.endsWith(".css")) return "text/css";
        return "application/octet-stream";
    }

    private void esconderBarras() {
        View d = getWindow().getDecorView();
        d.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) { super.onWindowFocusChanged(hasFocus); if (hasFocus) esconderBarras(); }
    @Override
    protected void onPause() { super.onPause(); if (web != null) web.onPause(); }
    @Override
    protected void onResume() { super.onResume(); if (web != null) web.onResume(); esconderBarras(); }

    @Override
    public void onBackPressed() {
        if (telaCheia != null) { raiz.removeView(telaCheia); telaCheia = null; if (avisoSaida != null) avisoSaida.onCustomViewHidden(); return; }
        // dentro de um jogo do Ryan VR, o voltar abre a pausa (e, de novo, sai do jogo)
        web.evaluateJavascript("(window.__voltar && window.__voltar()) ? 1 : 0", new android.webkit.ValueCallback<String>() {
            @Override public void onReceiveValue(String v) {
                if ("1".equals(v)) return;
                if (web.canGoBack()) { web.goBack(); return; } // volta do jogo para o Ryan VR
                finish();
            }
        });
    }
}
