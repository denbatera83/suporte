package br.familia.sosalarme;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Tela vermelha piscando, alarme alto e vibração sem parar até alguém apertar PARAR ALARME. */
public class AlarmeActivity extends Activity {
    private Vibrator vib; private MediaPlayer som; private ToneGenerator tom; private AudioManager am; private int volAntes = -1;
    private final Handler h = new Handler(Looper.getMainLooper());
    private LinearLayout raiz; private TextView titulo, texto; private Button bMapa, bLigar;
    private boolean claro = false, tocando = false; private String mapa, de;

    private final Runnable piscar = new Runnable() { public void run() { claro = !claro; raiz.setBackgroundColor(claro ? Color.rgb(240, 70, 90) : Color.rgb(176, 12, 30)); h.postDelayed(this, 500); } };

    private Button botao(String t, int fundo, int cor) {
        Button b = new Button(this); b.setText(t); b.setTextSize(20); b.setTypeface(Typeface.DEFAULT_BOLD); b.setTextColor(cor); b.setBackgroundColor(fundo); b.setAllCaps(true);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(64)); lp.topMargin = dp(12); b.setLayoutParams(lp); return b;
    }
    private int dp(int v) { return (int) (v * getResources().getDisplayMetrics().density + 0.5f); }

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        raiz = new LinearLayout(this); raiz.setOrientation(LinearLayout.VERTICAL); raiz.setGravity(Gravity.CENTER_HORIZONTAL); raiz.setPadding(dp(24), dp(40), dp(24), dp(32));
        titulo = new TextView(this); titulo.setTextColor(Color.WHITE); titulo.setTextSize(44); titulo.setTypeface(Typeface.DEFAULT_BOLD); titulo.setGravity(Gravity.CENTER); raiz.addView(titulo);
        texto = new TextView(this); texto.setTextColor(Color.WHITE); texto.setTextSize(20); texto.setGravity(Gravity.CENTER); texto.setPadding(0, dp(16), 0, dp(12)); raiz.addView(texto);
        bMapa = botao("Ver no mapa", Color.WHITE, Color.rgb(143, 12, 27)); raiz.addView(bMapa);
        bLigar = botao("Ligar de volta", Color.WHITE, Color.rgb(143, 12, 27)); raiz.addView(bLigar);
        Button b192 = botao("Ligar 192 · SAMU", Color.rgb(20, 24, 29), Color.WHITE); raiz.addView(b192);
        Button parar = botao("Parar alarme", Color.rgb(20, 24, 29), Color.WHITE); raiz.addView(parar);
        bMapa.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { pararAlarme(); abrir(Intent.ACTION_VIEW, mapa); } });
        bLigar.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { pararAlarme(); abrir(Intent.ACTION_DIAL, "tel:" + de); } });
        b192.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { pararAlarme(); abrir(Intent.ACTION_DIAL, "tel:192"); } });
        parar.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { pararAlarme(); finish(); } });
        ScrollView sv = new ScrollView(this); sv.setFillViewport(true); sv.addView(raiz); setContentView(sv);
        mostrar(getIntent()); tocarAlarme();
    }

    @Override protected void onNewIntent(Intent i) { super.onNewIntent(i); setIntent(i); mostrar(i); tocarAlarme(); }

    private void mostrar(Intent i) {
        String t = i.getStringExtra("texto"); if (t == null) t = "SOS EMERGÊNCIA!";
        de = i.getStringExtra("de");
        titulo.setText(t.toUpperCase().contains("TESTE") ? "SOS · TESTE" : "SOS\nEMERGÊNCIA");
        texto.setText((de != null ? "Mensagem de " + de + "\n\n" : "") + t);
        Matcher m = Pattern.compile("https?://maps\\.google\\.com/\\?q=[-0-9.,]+").matcher(t); mapa = m.find() ? m.group() : null;
        bMapa.setVisibility(mapa != null ? View.VISIBLE : View.GONE); bLigar.setVisibility(de != null ? View.VISIBLE : View.GONE);
    }

    private void abrir(String acao, String url) { if (url == null) return; try { startActivity(new Intent(acao, Uri.parse(url))); } catch (Exception e) { } }

    /** Alarme no volume máximo do canal de alarme (que costuma tocar mesmo no silencioso) e vibração repetindo. */
    private void tocarAlarme() {
        if (tocando) return; tocando = true;
        h.post(piscar);
        try { vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE); vib.vibrate(new long[] { 0, 900, 400 }, 0); } catch (Exception e) { }
        try { am = (AudioManager) getSystemService(Context.AUDIO_SERVICE); volAntes = am.getStreamVolume(AudioManager.STREAM_ALARM); am.setStreamVolume(AudioManager.STREAM_ALARM, am.getStreamMaxVolume(AudioManager.STREAM_ALARM), 0); } catch (Exception e) { }
        try {
            Uri u = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM); if (u == null) u = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            som = new MediaPlayer(); som.setAudioStreamType(AudioManager.STREAM_ALARM); som.setDataSource(this, u); som.setLooping(true); som.prepare(); som.start();
        } catch (Exception e) {
            try { if (som != null) som.release(); } catch (Exception e2) { } som = null;
            try { tom = new ToneGenerator(AudioManager.STREAM_ALARM, 100); tom.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK); } catch (Exception e3) { }
        }
    }

    private void pararAlarme() {
        if (!tocando) return; tocando = false;
        h.removeCallbacks(piscar); raiz.setBackgroundColor(Color.rgb(176, 12, 30));
        try { if (vib != null) vib.cancel(); } catch (Exception e) { }
        try { if (som != null) { som.stop(); som.release(); } } catch (Exception e) { } som = null;
        try { if (tom != null) { tom.stopTone(); tom.release(); } } catch (Exception e) { } tom = null;
        try { if (am != null && volAntes >= 0) am.setStreamVolume(AudioManager.STREAM_ALARM, volAntes, 0); } catch (Exception e) { }
    }

    @Override protected void onDestroy() { pararAlarme(); super.onDestroy(); }
}
