package br.ryan.vr;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import java.util.ArrayList;

/**
 * Navegador dentro do VR (Cardboard): o site aparece nas DUAS telas, como uma tela grande parada na
 * sua frente. A mira anda com a cabeça ou com a sua MÃO (a página do Ryan VR lê a mão pela câmera e
 * manda para cá). Clique: botão do óculos (toque na tela) ou pinça. Pinça + arrastar = rolar a página.
 * Embaixo: Voltar, Subir, Descer, Teclado e Ryan VR (olhe um instante, aperte ou faça a pinça).
 */
public class NavegadorVR extends FrameLayout implements SensorEventListener {
    public interface AoFechar { void fechou(); }

    private static final String UA_PC = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36";
    private final WebView nav;
    private final SensorManager sm;
    private final AoFechar aoFechar;
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float yaw0 = Float.NaN, pitch0, tx, ty, alvoX, alvoY;
    private int sw = 1, sh = 1, navH = 1, barH = 1, progresso = 0;
    private String mira = null; private long miraDesde = 0, ultimoPasso = 0; private boolean disparou = false;
    private boolean teclado = false;
    // mão (vinda da página): posição na imagem da câmera (0..1) e pinça
    private float maoX = -1, maoY = -1; private boolean pinca = false, pincaAntes = false; private long maoEm = 0; private float arrastoY = Float.NaN; private boolean arrastou = false;

    private static final String[] IDS = { "voltar", "sobe", "desce", "teclado", "sair" };
    private static final String[] ROTULOS = { "◀ Voltar", "▲", "▼", "⌨️ Teclado", "🏠 Ryan VR" };
    private final RectF[] rb = { new RectF(), new RectF(), new RectF(), new RectF(), new RectF() };
    private static class Tecla { String id, rot; RectF r = new RectF(); Tecla(String i, String t) { id = i; rot = t; } }
    private final ArrayList<Tecla> teclas = new ArrayList<Tecla>();
    private final float[] R = new float[9];

    public NavegadorVR(Activity a, String url, boolean modoPC, AoFechar f) {
        super(a);
        aoFechar = f;
        setBackgroundColor(Color.rgb(11, 9, 24));
        setWillNotDraw(false);
        nav = new WebView(a);
        WebSettings s = nav.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setBuiltInZoomControls(false);
        s.setTextZoom(modoPC ? 100 : 120);
        if (modoPC) s.setUserAgentString(UA_PC); // sites que só abrem no computador (ex.: WhatsApp Web)
        // os logins ficam guardados (cookies) entre uma vez e outra
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(nav, true);
        // desenhado em imagem (software) para poder aparecer nos dois olhos ao mesmo tempo
        nav.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        nav.setWebViewClient(new WebViewClient());
        nav.setWebChromeClient(new WebChromeClient() {
            @Override public void onProgressChanged(WebView v, int n) { progresso = n; invalidate(); }
            // música protegida (ex.: Spotify) pede licença de mídia; câmera e microfone ficam com o Ryan VR
            @Override public void onPermissionRequest(PermissionRequest r) {
                for (String x : r.getResources()) if (PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID.equals(x)) { r.grant(new String[]{ x }); return; }
                r.deny();
            }
        });
        addView(nav, new LayoutParams(1, 1));
        nav.loadUrl(url);
        sm = (SensorManager) a.getSystemService(Context.SENSOR_SERVICE);
    }

    public boolean voltar() { if (teclado) { teclado = false; return true; } if (nav.canGoBack()) { nav.goBack(); return true; } return false; }
    public void fechar() {
        if (sm != null) sm.unregisterListener(this);
        CookieManager.getInstance().flush();
        nav.stopLoading(); nav.destroy();
        if (aoFechar != null) aoFechar.fechou();
    }

    /** A página do Ryan VR manda a mão aqui (x, y de 0 a 1 na imagem da câmera; x < 0 = sem mão). */
    public void mao(float x, float y, boolean aperta) {
        maoX = x; maoY = y; maoEm = SystemClock.uptimeMillis();
        boolean comeco = aperta && !pinca;
        pinca = aperta;
        if (x < 0) { arrastoY = Float.NaN; return; }
        float my = miraY();
        if (comeco) { arrastoY = my; arrastou = false; }
        else if (pinca && !Float.isNaN(arrastoY) && "pagina".equals(sobre())) { // pinça + arrastar = rolar
            float dy = arrastoY - my;
            if (Math.abs(dy) > 6) { nav.scrollBy(0, Math.round(dy * 1.6f)); arrastoY = my; arrastou = true; }
        }
        if (!pinca && pincaAntes && !arrastou) clique(); // soltou a pinça sem arrastar = clique
        if (!pinca) arrastoY = Float.NaN;
        pincaAntes = pinca;
        invalidate();
    }
    private boolean maoAtiva() { return maoX >= 0 && SystemClock.uptimeMillis() - maoEm < 400; }

    @Override protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
        int olho = w / 2;
        sw = Math.round(olho * 1.15f); sh = Math.round(h * 1.2f);
        barH = Math.round(h * 0.16f); navH = sh - barH - 16;
        final LayoutParams lp = new LayoutParams(sw, navH);
        post(new Runnable() { public void run() { nav.setLayoutParams(lp); } });
        float bw = (sw - 4 * 12f) / 5f;
        for (int i = 0; i < 5; i++) rb[i].set(i * (bw + 12), navH + 16, i * (bw + 12) + bw, sh);
        montaTeclas();
    }
    // teclado: ocupa a parte de baixo da página quando está aberto
    private void montaTeclas() {
        teclas.clear();
        String[] L = { "1234567890", "qwertyuiop", "asdfghjklç", "zxcvbnm,.?" };
        float top = navH * 0.5f, alt = navH - top - 10, kh = (alt - 4 * 8) / 5f, kw = (sw - 20 - 9 * 6) / 10f;
        for (int r = 0; r < 4; r++) for (int i = 0; i < 10; i++) {
            Tecla t = new Tecla("c" + L[r].charAt(i), String.valueOf(L[r].charAt(i)));
            float x = 10 + i * (kw + 6), y = top + r * (kh + 8); t.r.set(x, y, x + kw, y + kh); teclas.add(t);
        }
        float y = top + 4 * (kh + 8);
        String[][] esp = { { "apaga", "⌫" }, { "espaco", "espaço" }, { "arroba", "@" }, { "enter", "↵ Enviar" }, { "fechar", "✕" } };
        float[] lar = { 1.5f, 4.5f, 1f, 2.5f, 1f }; float x = 10;
        for (int k = 0; k < 5; k++) { Tecla t = new Tecla(esp[k][0], esp[k][1]); float w = lar[k] * kw + (lar[k] - 1) * 6; t.r.set(x, y, x + w, y + kh); teclas.add(t); x += w + 6; }
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (sm == null) return;
        Sensor s = sm.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        if (s == null) s = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if (s != null) sm.registerListener(this, s, SensorManager.SENSOR_DELAY_GAME);
    }
    @Override protected void onDetachedFromWindow() { super.onDetachedFromWindow(); if (sm != null) sm.unregisterListener(this); }
    @Override public void onAccuracyChanged(Sensor s, int a) {}

    // para onde a parte de trás do celular aponta = para onde você olha
    @Override public void onSensorChanged(SensorEvent e) {
        SensorManager.getRotationMatrixFromVector(R, e.values);
        float vx = -R[2], vy = -R[5], vz = -R[8];
        float yaw = (float) Math.atan2(vx, vy), pitch = (float) Math.asin(Math.max(-1f, Math.min(1f, vz)));
        if (Float.isNaN(yaw0)) { yaw0 = yaw; pitch0 = pitch; }
        float meio = getWidth() / 2f, k = meio / 1.3f;
        float dy = yaw - yaw0; while (dy > Math.PI) dy -= 2 * Math.PI; while (dy < -Math.PI) dy += 2 * Math.PI;
        float x = -dy * k, y = (pitch - pitch0) * k;
        float lx = sw / 2f + 30, ly = sh / 2f + 30;
        if (x > lx) { x = lx; yaw0 = yaw + x / k; } if (x < -lx) { x = -lx; yaw0 = yaw + x / k; }
        if (y > ly) { y = ly; pitch0 = pitch - y / k; } if (y < -ly) { y = -ly; pitch0 = pitch - y / k; }
        alvoX = x; alvoY = y;
        invalidate();
    }

    // mira em coordenadas da tela virtual: com a mão, vai onde o dedo está; sem a mão, fica no meio da vista
    private float cursorX() { float meio = getWidth() / 2f; return maoAtiva() ? maoX * meio : meio / 2f; }
    private float cursorY() { return maoAtiva() ? maoY * getHeight() : getHeight() / 2f; }
    private float miraX() { float meio = getWidth() / 2f; return cursorX() - ((meio - sw) / 2f + tx); }
    private float miraY() { return cursorY() - ((getHeight() - sh) / 2f + ty); }
    private String sobre() {
        float mx = miraX(), my = miraY();
        if (teclado) for (Tecla t : teclas) if (t.r.contains(mx, my)) return "k:" + t.id;
        for (int i = 0; i < 5; i++) if (rb[i].contains(mx, my)) return IDS[i];
        if (mx >= 0 && mx <= sw && my >= 0 && my <= navH) return "pagina";
        return null;
    }

    private void acao(String id) {
        if (id.startsWith("k:")) { tecla(id.substring(2)); return; }
        if ("voltar".equals(id)) { if (!voltar()) fechar(); }
        else if ("sobe".equals(id)) nav.scrollBy(0, -Math.round(navH * 0.3f));
        else if ("desce".equals(id)) nav.scrollBy(0, Math.round(navH * 0.3f));
        else if ("teclado".equals(id)) { teclado = !teclado; }
        else if ("sair".equals(id)) fechar();
    }
    // digita no campo do site que está selecionado (toque antes na caixa de texto)
    private void tecla(String id) {
        if ("fechar".equals(id)) { teclado = false; return; }
        if ("apaga".equals(id)) { tecla(KeyEvent.KEYCODE_DEL); return; }
        if ("enter".equals(id)) { tecla(KeyEvent.KEYCODE_ENTER); return; }
        String c = "espaco".equals(id) ? " " : "arroba".equals(id) ? "@" : id.substring(1);
        KeyEvent[] ev = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD).getEvents(c.toCharArray());
        if (ev != null) { for (KeyEvent k : ev) nav.dispatchKeyEvent(k); }
        else nav.evaluateJavascript("document.execCommand('insertText', false, " + org.json.JSONObject.quote(c) + ")", null); // ç e outras letras especiais
    }
    private void tecla(int code) {
        long t = SystemClock.uptimeMillis();
        nav.dispatchKeyEvent(new KeyEvent(t, t, KeyEvent.ACTION_DOWN, code, 0));
        nav.dispatchKeyEvent(new KeyEvent(t, t, KeyEvent.ACTION_UP, code, 0));
    }
    private void clique() {
        String s = sobre();
        if (s == null) return;
        if (!"pagina".equals(s)) { acao(s); miraDesde = SystemClock.uptimeMillis(); disparou = true; return; }
        long t = SystemClock.uptimeMillis(); float x = miraX(), y = miraY();
        MotionEvent d = MotionEvent.obtain(t, t, MotionEvent.ACTION_DOWN, x, y, 0);
        MotionEvent u = MotionEvent.obtain(t, t + 60, MotionEvent.ACTION_UP, x, y, 0);
        nav.dispatchTouchEvent(d); nav.dispatchTouchEvent(u); d.recycle(); u.recycle();
    }

    // botão do Cardboard (toque em qualquer lugar): clica onde a mira está
    @Override public boolean onInterceptTouchEvent(MotionEvent e) { return true; }
    @Override public boolean onTouchEvent(MotionEvent e) { if (e.getActionMasked() == MotionEvent.ACTION_UP) clique(); return true; }

    @Override protected void dispatchDraw(Canvas c) {
        int w = getWidth(), h = getHeight(), meio = w / 2;
        tx += (alvoX - tx) * 0.5f; ty += (alvoY - ty) * 0.5f;
        // olhar parado num botão = aperta (Subir/Descer continuam enquanto você olha; tecla aperta uma vez)
        String s = sobre(); long agora = SystemClock.uptimeMillis();
        if (s == null || !s.equals(mira)) { mira = s; miraDesde = agora; disparou = false; }
        float prog = 0;
        if (mira != null && !"pagina".equals(mira) && !disparou) {
            boolean rola = "sobe".equals(mira) || "desce".equals(mira), tec = mira.startsWith("k:");
            long espera = rola ? 500 : tec ? 900 : 1300;
            prog = Math.min(1f, (agora - miraDesde) / (float) espera);
            if (agora - miraDesde >= espera) {
                if (rola) { if (agora - ultimoPasso > 90) { nav.scrollBy(0, Math.round(("sobe".equals(mira) ? -1 : 1) * navH * 0.04f)); ultimoPasso = agora; } }
                else { String m = mira; disparou = true; acao(m); if ("sair".equals(m)) return; }
            }
        }
        float ox = (meio - sw) / 2f + tx, oy = (h - sh) / 2f + ty;
        for (int olho = 0; olho < 2; olho++) {
            c.save();
            c.translate(olho * meio, 0); c.clipRect(0, 0, meio, h);
            c.drawColor(Color.rgb(11, 9, 24));
            c.save(); c.translate(ox, oy);
            p.setStyle(Paint.Style.FILL); p.setColor(Color.rgb(42, 36, 80));
            c.drawRoundRect(-14, -14, sw + 14, navH + 14, 26, 26, p);
            drawChild(c, nav, getDrawingTime());
            if (progresso < 100) { p.setColor(Color.rgb(66, 133, 244)); c.drawRect(0, 0, sw * progresso / 100f, 8, p); }
            p.setTextAlign(Paint.Align.CENTER);
            if (teclado) {
                p.setColor(Color.argb(235, 24, 22, 40)); c.drawRoundRect(0, navH * 0.5f - 12, sw, navH, 22, 22, p);
                for (Tecla t : teclas) {
                    boolean hv = ("k:" + t.id).equals(mira);
                    p.setColor(hv ? Color.WHITE : "enter".equals(t.id) ? Color.rgb(37, 211, 102) : Color.argb(60, 255, 255, 255));
                    c.drawRoundRect(t.r, 14, 14, p);
                    p.setColor(hv ? Color.rgb(20, 18, 32) : Color.WHITE); p.setTextSize(t.r.height() * (t.rot.length() > 2 ? 0.38f : 0.5f));
                    c.drawText(t.rot, t.r.centerX(), t.r.centerY() + p.getTextSize() * 0.36f, p);
                }
            }
            p.setTextSize(barH * 0.32f);
            for (int i = 0; i < 5; i++) {
                boolean hv = IDS[i].equals(mira);
                p.setColor(hv ? Color.rgb(143, 123, 255) : (i == 4 ? Color.rgb(229, 72, 95) : i == 3 && teclado ? Color.rgb(143, 123, 255) : Color.rgb(42, 36, 80)));
                c.drawRoundRect(rb[i], 22, 22, p);
                p.setColor(Color.WHITE);
                c.drawText(ROTULOS[i], rb[i].centerX(), rb[i].centerY() + p.getTextSize() * 0.36f, p);
            }
            c.restore();
            // mira: no dedo (mão) ou no meio de cada olho (cabeça)
            float cx = cursorX(), cy = cursorY();
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(5); p.setColor(maoAtiva() ? (pinca ? Color.WHITE : Color.rgb(90, 209, 255)) : Color.argb(230, 255, 255, 255));
            c.drawCircle(cx, cy, maoAtiva() ? 18 : 14, p);
            if (prog > 0) { p.setColor(Color.rgb(255, 179, 107)); p.setStrokeWidth(7); c.drawArc(cx - 24, cy - 24, cx + 24, cy + 24, -90, 360 * prog, false, p); }
            p.setStyle(Paint.Style.FILL);
            c.restore();
        }
        p.setColor(Color.rgb(60, 60, 70)); c.drawRect(meio - 1, 0, meio + 1, h, p);
        postInvalidateOnAnimation();
    }
}
