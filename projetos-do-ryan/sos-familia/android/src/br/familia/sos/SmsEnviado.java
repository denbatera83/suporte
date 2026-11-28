package br.familia.sos;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** O celular avisa aqui quando cada parte do SMS saiu (ou falhou); repassamos para a página. */
public class SmsEnviado extends BroadcastReceiver {
    @Override public void onReceive(Context c, Intent it) {
        boolean ok = getResultCode() == Activity.RESULT_OK;
        MainActivity.js("window.__smsEnviado && __smsEnviado(" + it.getIntExtra("i", 0) + "," + it.getIntExtra("k", 0) + "," + it.getIntExtra("n", 1) + "," + ok + ")");
    }
}
