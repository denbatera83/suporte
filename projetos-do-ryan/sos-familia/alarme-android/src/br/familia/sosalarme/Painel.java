package br.familia.sosalarme;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** Tela do SOS Alarme: mostra o que falta permitir, liga/desliga o alarme e deixa testar. */
public class Painel extends Activity {
    private TextView stSms, stSobre; private CheckBox ligado;
    private final int VERMELHO = Color.rgb(208, 20, 44), TINTA = Color.rgb(20, 24, 29), SUAVE = Color.rgb(90, 99, 110), OK = Color.rgb(19, 112, 63);

    private int dp(int v) { return (int) (v * getResources().getDisplayMetrics().density + 0.5f); }
    private TextView texto(String t, int tam, int cor, boolean negrito) { TextView v = new TextView(this); v.setText(t); v.setTextSize(tam); v.setTextColor(cor); if (negrito) v.setTypeface(Typeface.DEFAULT_BOLD); v.setPadding(0, dp(6), 0, dp(6)); return v; }
    private Button botao(String t, int fundo) { Button b = new Button(this); b.setText(t); b.setTextColor(Color.WHITE); b.setBackgroundColor(fundo); b.setTextSize(17); b.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(56)); lp.topMargin = dp(10); b.setLayoutParams(lp); return b; }
    private boolean temSms() { return Build.VERSION.SDK_INT < 23 || checkSelfPermission(Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED; }
    private boolean podeSobrepor() { return Build.VERSION.SDK_INT < 23 || Settings.canDrawOverlays(this); }

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        final SharedPreferences p = getSharedPreferences("sos", 0);
        LinearLayout raiz = new LinearLayout(this); raiz.setOrientation(LinearLayout.VERTICAL); raiz.setPadding(dp(20), dp(28), dp(20), dp(28)); raiz.setBackgroundColor(Color.rgb(236, 239, 242));
        TextView t = texto("SOS Alarme", 32, VERMELHO, true); raiz.addView(t);
        raiz.addView(texto("Quando chegar um SMS com SOS EMERGÊNCIA (mandado pelo app SOS Família), este celular toca um alarme alto e vibra sem parar até alguém apertar Parar.", 17, TINTA, false));
        ligado = new CheckBox(this); ligado.setText("Alarme ligado neste celular"); ligado.setTextSize(18); ligado.setTextColor(TINTA); ligado.setChecked(p.getBoolean("receber", true)); ligado.setPadding(0, dp(12), 0, dp(12));
        ligado.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { public void onCheckedChanged(CompoundButton c, boolean on) { p.edit().putBoolean("receber", on).apply(); } });
        raiz.addView(ligado);
        stSms = texto("", 16, TINTA, true); raiz.addView(stSms);
        Button bSms = botao("Permitir ler SMS", TINTA); raiz.addView(bSms);
        stSobre = texto("", 16, TINTA, true); raiz.addView(stSobre);
        Button bSobre = botao("Permitir aparecer sobre outros apps", TINTA); raiz.addView(bSobre);
        Button bTeste = botao("Testar o alarme", VERMELHO); raiz.addView(bTeste);
        raiz.addView(texto("Dica: em celulares Samsung e Xiaomi, tire o SOS Alarme da economia de bateria (Configurações, Aplicativos, SOS Alarme, Bateria, Sem restrições). Abra o app uma vez depois de instalar.", 14, SUAVE, false));
        bSms.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { if (Build.VERSION.SDK_INT >= 23) requestPermissions(new String[] { Manifest.permission.RECEIVE_SMS }, 1); } });
        bSobre.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { try { startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()))); } catch (Exception e) { } } });
        bTeste.setOnClickListener(new View.OnClickListener() { public void onClick(View v) {
            Intent i = new Intent(Painel.this, AlarmeActivity.class); i.putExtra("texto", "(TESTE) SOS EMERGÊNCIA! Assim fica o alarme quando chegar um pedido de ajuda. Toque em PARAR ALARME."); startActivity(i); } });
        ScrollView sv = new ScrollView(this); sv.setFillViewport(true); sv.setBackgroundColor(Color.rgb(236, 239, 242)); sv.addView(raiz); setContentView(sv);
        if (!temSms() && Build.VERSION.SDK_INT >= 23) requestPermissions(new String[] { Manifest.permission.RECEIVE_SMS }, 1);
    }

    private void atualizar() {
        boolean s = temSms(), o = podeSobrepor();
        stSms.setText(s ? "✓ Ler SMS: permitido" : "✗ Ler SMS: falta permitir"); stSms.setTextColor(s ? OK : VERMELHO);
        stSobre.setText(o ? "✓ Aparecer sobre outros apps: permitido" : "✗ Aparecer sobre outros apps: falta permitir (sem isso, o celular só vibra)"); stSobre.setTextColor(o ? OK : VERMELHO);
    }
    @Override protected void onResume() { super.onResume(); atualizar(); }
    @Override public void onRequestPermissionsResult(int c, String[] p, int[] r) { atualizar(); }
}
