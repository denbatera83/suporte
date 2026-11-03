package br.ryan.blocosvr;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

/** Abre o jogo Blocos VR (HTML + A-Frame) em tela cheia, deitado, para usar no Cardboard. */
public class MainActivity extends Activity {
    private WebView web;
    private FrameLayout raiz;
    private View telaCheia;
    private WebChromeClient.CustomViewCallback avisoSaida;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        raiz = new FrameLayout(this);
        raiz.setBackgroundColor(Color.rgb(16, 24, 38));
        web = new WebView(this);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        web.setWebViewClient(new WebViewClient());
        web.setWebChromeClient(new WebChromeClient() {
            // o modo Cardboard pede tela cheia de um elemento: mostra por cima do jogo
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
        });
        raiz.addView(web, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(raiz);
        esconderBarras();
        web.loadUrl("file:///android_asset/index.html");
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
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) esconderBarras();
    }

    @Override
    protected void onPause() { super.onPause(); if (web != null) web.onPause(); }

    @Override
    protected void onResume() { super.onResume(); if (web != null) web.onResume(); esconderBarras(); }

    @Override
    public void onBackPressed() {
        if (telaCheia != null) { raiz.removeView(telaCheia); telaCheia = null; if (avisoSaida != null) avisoSaida.onCustomViewHidden(); return; }
        super.onBackPressed();
    }
}
