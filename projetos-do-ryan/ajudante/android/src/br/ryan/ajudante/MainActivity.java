package br.ryan.ajudante;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Ajudante do Ryan: um app que conversa por voz (ouve e fala).
 * Deixa mensagens do WhatsApp prontas (você aperta Enviar), abre apps pelo nome,
 * abre a tela de ligar e dá desafios de Minecraft. Nada é feito escondido:
 * o Android e o WhatsApp não deixam um app apertar botões nos outros apps.
 */
public class MainActivity extends Activity {
    private static final int OUVIR = 51;
    private WebView web;
    private TextToSpeech voz;
    private boolean vozPronta = false;

    public class Ponte {
        @android.webkit.JavascriptInterface public void ouvir() {
            runOnUiThread(new Runnable() { public void run() {
                Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR");
                i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Fale com o Ajudante");
                try { startActivityForResult(i, OUVIR); } catch (Exception e) { devolve(null); }
            } });
        }
        @android.webkit.JavascriptInterface public boolean falar(final String texto) {
            if (!vozPronta || voz == null) return false;
            voz.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "fala");
            return true;
        }
        @android.webkit.JavascriptInterface public void calar() { if (voz != null) voz.stop(); }
        /** Só links do WhatsApp com a mensagem pronta: quem aperta Enviar é você. */
        @android.webkit.JavascriptInterface public boolean abrirLink(String url) {
            String u = url == null ? "" : url.trim();
            if (!(u.startsWith("https://wa.me/") || u.startsWith("https://api.whatsapp.com/"))) return false;
            return comeca(new Intent(Intent.ACTION_VIEW, Uri.parse(u)));
        }
        /** Abre a tela de ligar com o número digitado (você aperta o botão verde). */
        @android.webkit.JavascriptInterface public boolean ligar(String numero) {
            String n = numero == null ? "" : numero.replaceAll("[^0-9+]", "");
            if (n.length() < 3) return false;
            return comeca(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + n)));
        }
        @android.webkit.JavascriptInterface public String listarApps() {
            JSONArray l = new JSONArray();
            try {
                PackageManager pm = getPackageManager();
                Intent i = new Intent(Intent.ACTION_MAIN); i.addCategory(Intent.CATEGORY_LAUNCHER);
                List<ResolveInfo> r = pm.queryIntentActivities(i, 0);
                for (ResolveInfo ri : r) {
                    if (ri.activityInfo == null || getPackageName().equals(ri.activityInfo.packageName)) continue;
                    JSONObject o = new JSONObject(); o.put("nome", String.valueOf(ri.loadLabel(pm))); o.put("pacote", ri.activityInfo.packageName); l.put(o);
                }
            } catch (Exception e) { }
            return l.toString();
        }
        @android.webkit.JavascriptInterface public boolean abrirApp(String pacote) {
            Intent i = getPackageManager().getLaunchIntentForPackage(pacote);
            return i != null && comeca(i);
        }
        @android.webkit.JavascriptInterface public boolean abrirAssistente() {
            if (comeca(new Intent(Intent.ACTION_VOICE_COMMAND))) return true;
            return comeca(new Intent("android.intent.action.ASSIST"));
        }
    }

    private boolean comeca(Intent i) {
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try { startActivity(i); return true; } catch (Exception e) { return false; }
    }

    private void devolve(String txt) {
        if (web != null) web.evaluateJavascript("window.__voz && window.__voz(" + (txt == null ? "null" : JSONObject.quote(txt)) + ")", null);
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        web = new WebView(this);
        web.setBackgroundColor(Color.rgb(26, 22, 48));
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true); s.setAllowFileAccess(true); s.setMediaPlaybackRequiresUserGesture(false);
        web.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView v, String url) { return url == null || !url.startsWith("file:"); }
        });
        web.setWebChromeClient(new WebChromeClient());
        web.addJavascriptInterface(new Ponte(), "Ajudante");
        setContentView(web);
        web.loadUrl("file:///android_asset/index.html");
        voz = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override public void onInit(int st) {
                if (st != TextToSpeech.SUCCESS) return;
                int r = voz.setLanguage(new Locale("pt", "BR"));
                vozPronta = r != TextToSpeech.LANG_MISSING_DATA && r != TextToSpeech.LANG_NOT_SUPPORTED;
                voz.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override public void onStart(String id) { avisa("true"); }
                    @Override public void onDone(String id) { avisa("false"); }
                    @Override public void onError(String id) { avisa("false"); }
                });
            }
        });
    }

    private void avisa(final String falando) {
        runOnUiThread(new Runnable() { public void run() { if (web != null) web.evaluateJavascript("window.__falando && window.__falando(" + falando + ")", null); } });
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req != OUVIR) return;
        String t = null;
        if (res == RESULT_OK && data != null) { ArrayList<String> l = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS); if (l != null && !l.isEmpty()) t = l.get(0); }
        devolve(t);
    }

    @Override
    protected void onResume() { super.onResume(); if (web != null) web.evaluateJavascript("window.__voltou && window.__voltou()", null); }

    @Override
    public void onBackPressed() {
        web.evaluateJavascript("(window.__voltar && window.__voltar()) ? 1 : 0", new android.webkit.ValueCallback<String>() {
            @Override public void onReceiveValue(String v) { if (!"1".equals(v)) finish(); }
        });
    }

    @Override
    protected void onDestroy() { if (voz != null) voz.shutdown(); super.onDestroy(); }
}
