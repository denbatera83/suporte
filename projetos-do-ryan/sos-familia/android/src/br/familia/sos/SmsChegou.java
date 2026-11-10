package br.familia.sos;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.telephony.SmsMessage;
import java.text.Normalizer;
import java.util.Locale;

/** Chegou um SMS: se for um pedido de ajuda ("SOS EMERGÊNCIA"), o celular vibra e abre o alarme na tela. */
public class SmsChegou extends BroadcastReceiver {
    @Override public void onReceive(Context c, Intent it) {
        if (!c.getSharedPreferences("sos", 0).getBoolean("receber", true)) return;
        Bundle b = it.getExtras(); if (b == null) return;
        Object[] pdus = (Object[]) b.get("pdus"); if (pdus == null) return;
        String formato = b.getString("format"), de = null; StringBuilder sb = new StringBuilder();
        for (Object o : pdus) {
            SmsMessage m = Build.VERSION.SDK_INT >= 23 ? SmsMessage.createFromPdu((byte[]) o, formato) : SmsMessage.createFromPdu((byte[]) o);
            if (m == null) continue; if (de == null) de = m.getOriginatingAddress(); sb.append(m.getMessageBody());
        }
        String texto = sb.toString();
        String plano = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toUpperCase(Locale.ROOT);
        if (!plano.contains("SOS EMERGENCIA")) return;
        try { ((Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(new long[] { 0, 1000, 300, 1000, 300, 1000, 300, 1000, 300, 1000 }, -1); } catch (Exception e) { }
        Intent a = new Intent(c, AlarmeActivity.class);
        a.putExtra("texto", texto); a.putExtra("de", de);
        a.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        try { c.startActivity(a); } catch (Exception e) { }
    }
}
