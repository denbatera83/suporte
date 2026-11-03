package br.ryan.vr;

import android.app.Activity;
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
    private static final int ESCOLHER_VIDEO = 42;
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
        web.setWebViewClient(new WebViewClient());
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
        web.loadUrl("file:///android_asset/ryan-vr/index.html");
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
        if (web.canGoBack()) { web.goBack(); return; } // volta do jogo para o Ryan VR
        super.onBackPressed();
    }
}
