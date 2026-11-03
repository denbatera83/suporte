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
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

/**
 * Navegador dentro do VR (Cardboard): a página aparece nas DUAS telas, como uma tela grande parada
 * na sua frente. Vire a cabeça para mover a mira; aperte o botão do óculos (toque na tela) para clicar.
 * Embaixo da página: Voltar, Subir, Descer e Ryan VR — basta olhar um instante para usar.
 */
public class NavegadorVR extends FrameLayout implements SensorEventListener {
    public interface AoFechar { void fechou(); }

    private final WebView nav;
    private final SensorManager sm;
    private final AoFechar aoFechar;
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float yaw0 = Float.NaN, pitch0, tx, ty, alvoX, alvoY;
    private int sw = 1, sh = 1, navH = 1, barH = 1, progresso = 0;
    private String mira = null; private long miraDesde = 0, ultimoPasso = 0;
    private static final String[] IDS = { "voltar", "sobe", "desce", "sair" };
    private static final String[] ROTULOS = { "◀ Voltar", "▲ Subir", "▼ Descer", "🏠 Ryan VR" };
    private final RectF[] rb = { new RectF(), new RectF(), new RectF(), new RectF() };
    private final float[] R = new float[9];

    public NavegadorVR(Activity a, String url, AoFechar f) {
        super(a);
        aoFechar = f;
        setBackgroundColor(Color.rgb(11, 9, 24));
        setWillNotDraw(false);
        nav = new WebView(a);
        WebSettings s = nav.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setTextZoom(120);
        // desenhado em imagem (software) para poder aparecer nos dois olhos ao mesmo tempo
        nav.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        nav.setWebViewClient(new WebViewClient());
        nav.setWebChromeClient(new WebChromeClient() {
            @Override public void onProgressChanged(WebView v, int n) { progresso = n; invalidate(); }
        });
        addView(nav, new LayoutParams(1, 1));
        nav.loadUrl(url);
        sm = (SensorManager) a.getSystemService(Context.SENSOR_SERVICE);
    }

    public boolean voltar() { if (nav.canGoBack()) { nav.goBack(); return true; } return false; }
    public void fechar() {
        if (sm != null) sm.unregisterListener(this);
        nav.stopLoading(); nav.destroy();
        if (aoFechar != null) aoFechar.fechou();
    }

    @Override protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
        int olho = w / 2;
        sw = Math.round(olho * 1.15f); sh = Math.round(h * 1.2f);
        barH = Math.round(h * 0.17f); navH = sh - barH - 16;
        final LayoutParams lp = new LayoutParams(sw, navH);
        post(new Runnable() { public void run() { nav.setLayoutParams(lp); } });
        float bw = (sw - 3 * 12f) / 4f;
        for (int i = 0; i < 4; i++) rb[i].set(i * (bw + 12), navH + 16, i * (bw + 12) + bw, sh);
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
        // a mira não sai da tela: se você virar demais, a tela vem junto
        float lx = sw / 2f + 30, ly = sh / 2f + 30;
        if (x > lx) { x = lx; yaw0 = yaw + x / k; } if (x < -lx) { x = -lx; yaw0 = yaw + x / k; }
        if (y > ly) { y = ly; pitch0 = pitch - y / k; } if (y < -ly) { y = -ly; pitch0 = pitch - y / k; }
        alvoX = x; alvoY = y;
        invalidate();
    }

    // mira em coordenadas da tela virtual
    private float miraX() { float meio = getWidth() / 2f; return meio / 2f - ((meio - sw) / 2f + tx); }
    private float miraY() { return getHeight() / 2f - ((getHeight() - sh) / 2f + ty); }
    private String sobre() {
        float mx = miraX(), my = miraY();
        for (int i = 0; i < 4; i++) if (rb[i].contains(mx, my)) return IDS[i];
        if (mx >= 0 && mx <= sw && my >= 0 && my <= navH) return "pagina";
        return null;
    }

    private void acao(String id) {
        if ("voltar".equals(id)) { if (!voltar()) fechar(); }
        else if ("sobe".equals(id)) nav.scrollBy(0, -Math.round(navH * 0.3f));
        else if ("desce".equals(id)) nav.scrollBy(0, Math.round(navH * 0.3f));
        else if ("sair".equals(id)) fechar();
    }

    // botão do Cardboard (toque em qualquer lugar): clica onde a mira está
    @Override public boolean onInterceptTouchEvent(MotionEvent e) { return true; }
    @Override public boolean onTouchEvent(MotionEvent e) {
        if (e.getActionMasked() != MotionEvent.ACTION_UP) return true;
        String s = sobre();
        if (s == null) return true;
        if (!"pagina".equals(s)) { acao(s); miraDesde = SystemClock.uptimeMillis(); return true; }
        long t = SystemClock.uptimeMillis(); float x = miraX(), y = miraY();
        MotionEvent d = MotionEvent.obtain(t, t, MotionEvent.ACTION_DOWN, x, y, 0);
        MotionEvent u = MotionEvent.obtain(t, t + 60, MotionEvent.ACTION_UP, x, y, 0);
        nav.dispatchTouchEvent(d); nav.dispatchTouchEvent(u); d.recycle(); u.recycle();
        return true;
    }

    @Override protected void dispatchDraw(Canvas c) {
        int w = getWidth(), h = getHeight(), meio = w / 2;
        tx += (alvoX - tx) * 0.5f; ty += (alvoY - ty) * 0.5f;
        // olhar parado num botão = aperta (Subir/Descer continuam enquanto você olha)
        String s = sobre(); long agora = SystemClock.uptimeMillis();
        if (s == null || !s.equals(mira)) { mira = s; miraDesde = agora; }
        float prog = 0;
        if (mira != null && !"pagina".equals(mira)) {
            boolean rola = "sobe".equals(mira) || "desce".equals(mira);
            long espera = rola ? 500 : 1300;
            prog = Math.min(1f, (agora - miraDesde) / (float) espera);
            if (agora - miraDesde >= espera) {
                if (rola) { if (agora - ultimoPasso > 90) { nav.scrollBy(0, Math.round(("sobe".equals(mira) ? -1 : 1) * navH * 0.04f)); ultimoPasso = agora; } }
                else { String m = mira; mira = null; acao(m); return; }
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
            p.setTextAlign(Paint.Align.CENTER); p.setTextSize(barH * 0.34f);
            for (int i = 0; i < 4; i++) {
                boolean hv = IDS[i].equals(mira);
                p.setColor(hv ? Color.rgb(143, 123, 255) : (i == 3 ? Color.rgb(229, 72, 95) : Color.rgb(42, 36, 80)));
                c.drawRoundRect(rb[i], 22, 22, p);
                p.setColor(Color.WHITE);
                c.drawText(ROTULOS[i], rb[i].centerX(), rb[i].centerY() + p.getTextSize() * 0.36f, p);
            }
            c.restore();
            // mira no meio de cada olho
            float cx = meio / 2f, cy = h / 2f;
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(5); p.setColor(Color.argb(230, 255, 255, 255));
            c.drawCircle(cx, cy, 14, p);
            if (prog > 0) { p.setColor(Color.rgb(255, 179, 107)); p.setStrokeWidth(7); c.drawArc(cx - 24, cy - 24, cx + 24, cy + 24, -90, 360 * prog, false, p); }
            p.setStyle(Paint.Style.FILL);
            c.restore();
        }
        // divisória entre os olhos
        p.setColor(Color.rgb(60, 60, 70)); c.drawRect(meio - 1, 0, meio + 1, h, p);
        postInvalidateOnAnimation();
    }
}
