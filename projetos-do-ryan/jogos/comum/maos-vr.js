// =====================================================================
// Mãos de verdade pela câmera de trás do celular (MediaPipe, do Google).
//
// RastreioMaos: reconhece as DUAS mãos (esquerda e direita), deixa o movimento
//   liso (filtro que acompanha rápido quando a mão corre e segura firme quando
//   ela está parada) e ainda desliza a mão a cada quadro entre uma leitura da
//   câmera e outra — por isso a mão do jogo não "pula".
//   Usado pelo Ryan VR e pelo MaosVR (Blocos VR).
//
// MaosVR: liga a câmera escondida (a imagem não aparece: você continua "em
//   outro lugar") e transforma a mão de verdade em mão dentro do jogo.
// =====================================================================
(function () {
  const MP_BASE = 'https://cdn.jsdelivr.net/npm/@mediapipe/tasks-vision@0.10.14/';
  const MP_MODELO = 'https://storage.googleapis.com/mediapipe-models/hand_landmarker/hand_landmarker/float16/1/hand_landmarker.task';
  const lerAj = (k, d) => { try { const v = localStorage.getItem(k); return v === null ? d : JSON.parse(v); } catch (e) { return d; } };
  const SOME_MS = 350; // sem ver a mão por esse tempo = mão sumiu

  function novaMao(lado) {
    return { lado, lm: null, alvo: null, vistoEm: 0, vel: 0, tam: 0,
      pinca: false, fechada: false, segura: false, gatilho: false, curva: [0, 0, 0, 0],
      // eventos: valem só no quadro em que aconteceram
      pincou: false, agarrou: false, soltou: false, atirou: false };
  }

  class RastreioMaos {
    constructor() {
      this.maos = [novaMao('esq'), novaMao('dir')];
      this.principal = null;   // a mão que aponta nos menus
      this.pincou = false;     // fica ligado até alguém usar (menus)
      this.ultimo = 0; this.intervalo = 33; this.custo = 0; this.tQuadro = 0;
    }
    visivel(m) { return !!(m && m.lm && performance.now() - m.vistoEm < SOME_MS); }
    visiveis() { return this.maos.filter(m => this.visivel(m)); }
    limpaEventos() { this.maos.forEach(m => { m.pincou = m.agarrou = m.soltou = m.atirou = false; }); }

    // chame uma vez por quadro
    detecta(hl, video) {
      this.limpaEventos();
      const agora = performance.now();
      const dt = this.tQuadro ? Math.min(0.1, (agora - this.tQuadro) / 1000) : 0.016; this.tQuadro = agora;
      if (hl && video && video.readyState >= 2 && agora - this.ultimo >= this.intervalo) {
        this.ultimo = agora;
        const t0 = performance.now();
        let r = null; try { r = hl.detectForVideo(video, agora); } catch (e) { r = null; }
        // celular lento: lê a câmera menos vezes (30 → 20 por segundo) para o jogo não travar
        this.custo = this.custo * 0.9 + (performance.now() - t0) * 0.1;
        this.intervalo = this.custo > 24 ? 50 : 33;
        if (r) this.processa(r, agora);
      } else this.confereSumico(agora);
      this.suaviza(dt);
    }

    processa(r, agora) {
      const L = (r && r.landmarks || []).slice(0, 2);
      const atr = [null, null];
      // a câmera de trás não espelha: a mão mais à esquerda da imagem é a sua mão esquerda
      if (L.length === 2) { const a = L[0][0].x <= L[1][0].x ? 0 : 1; atr[0] = L[a]; atr[1] = L[1 - a]; }
      else if (L.length === 1) {
        const p = L[0][9], vis = this.maos.map(m => this.visivel(m));
        const perto = m => Math.hypot(m.alvo[9].x - p.x, m.alvo[9].y - p.y);
        let i;
        if (vis[0] && vis[1]) i = perto(this.maos[0]) < perto(this.maos[1]) ? 0 : 1;
        else if (vis[0] || vis[1]) { const j = vis[0] ? 0 : 1; i = perto(this.maos[j]) < 0.22 ? j : (p.x < 0.5 ? 0 : 1); }
        else i = p.x < 0.5 ? 0 : 1;
        atr[i] = L[0];
      }
      atr.forEach((lm, i) => { if (lm) this.atualizaMao(this.maos[i], lm, agora); });
      this.confereSumico(agora);
    }

    confereSumico(agora) {
      this.maos.forEach(m => {
        if (m.lm && agora - m.vistoEm > SOME_MS) {
          m.lm = null; m.alvo = null; m.pinca = m.fechada = m.gatilho = false;
          if (m.segura) { m.segura = false; m.soltou = true; }
        }
      });
      // a mão principal: a que está segurando algo; senão a direita; senão a esquerda
      const p = this.principal;
      if (!(p && (this.visivel(p) || p.soltou) && p.segura)) {
        const pega = this.maos.find(m => m.agarrou && this.visivel(m));
        this.principal = pega || (this.visivel(this.maos[1]) ? this.maos[1] : this.visivel(this.maos[0]) ? this.maos[0] : (p && p.soltou ? p : null));
      }
    }

    atualizaMao(m, lm, agora) {
      if (!m.alvo) { m.alvo = lm.map(p => ({ x: p.x, y: p.y })); m.lm = lm.map(p => ({ x: p.x, y: p.y })); m.vel = 0; }
      else {
        // filtro adaptativo: parada = bem lisa (sem tremer); rápida = acompanha sem atraso
        const dtd = Math.max(0.016, (agora - m.vistoEm) / 1000);
        const mov = Math.hypot(lm[9].x - m.alvo[9].x, lm[9].y - m.alvo[9].y) / dtd;
        m.vel = m.vel * 0.5 + mov * 0.5;
        const a = Math.min(1, 0.3 + m.vel * 1.4);
        lm.forEach((p, i) => { m.alvo[i].x += (p.x - m.alvo[i].x) * a; m.alvo[i].y += (p.y - m.alvo[i].y) * a; });
      }
      m.vistoEm = agora;
      const d = (a, b) => Math.hypot(lm[a].x - lm[b].x, lm[a].y - lm[b].y);
      const tam = Math.max(1e-3, d(0, 9));
      m.tam = m.tam ? m.tam * 0.75 + tam * 0.25 : tam;
      // pinça: dedão encostado no indicador
      const pz = d(4, 8) / tam, antesP = m.pinca;
      if (!m.pinca && pz < 0.32) m.pinca = true; else if (m.pinca && pz > 0.5) m.pinca = false;
      if (m.pinca && !antesP) { m.pincou = true; this.pincou = true; }
      // mão fechada: pelo menos 3 pontas de dedo perto da palma
      let dobrados = 0; [[8, 5], [12, 9], [16, 13], [20, 17]].forEach(([ponta, base]) => { if (d(ponta, 0) < d(base, 0) * 1.15) dobrados++; });
      m.fechada = dobrados >= 3;
      m.curva = [[8, 5], [12, 9], [16, 13], [20, 17]].map(([p, b]) => Math.max(0, Math.min(1, 1.6 - d(p, 0) / Math.max(1e-3, d(b, 0)))));
      const segura = m.pinca || m.fechada;
      if (segura && !m.segura) m.agarrou = true;
      if (!segura && m.segura) m.soltou = true;
      m.segura = segura;
      // gatilho (mão de "arminha"): pinça, mão fechada ou dedão abaixado encostando no indicador
      const pol = d(4, 5) / tam, antesG = m.gatilho;
      const baixo = m.gatilho ? pol < 0.45 : pol < 0.3;
      m.gatilho = m.pinca || m.fechada || baixo;
      if (m.gatilho && !antesG) m.atirou = true;
    }

    // a mão desliza entre uma leitura da câmera e outra (fica lisa a 60 quadros por segundo)
    suaviza(dt) {
      const k = 1 - Math.exp(-dt * 26);
      this.maos.forEach(m => { if (!m.lm || !m.alvo) return; for (let i = 0; i < 21; i++) { m.lm[i].x += (m.alvo[i].x - m.lm[i].x) * k; m.lm[i].y += (m.alvo[i].y - m.lm[i].y) * k; } });
    }

    // distância da mão até o olho, pelo tamanho dela na imagem (mão mais perto da câmera = maior)
    profundidade(m, base = 0.45) { return Math.max(0.3, Math.min(0.68, base * Math.sqrt(0.17 / Math.max(0.05, m.tam || 0.17)))); }
  }
  window.RastreioMaos = RastreioMaos;

  // ---------------------------------------------------------------------
  // MaosVR: câmera escondida + rastreio, para jogos que não são o Ryan VR
  // ---------------------------------------------------------------------
  const R = new RastreioMaos();
  const M = {
    rastreio: R, hl: null, video: null, stream: null, carregando: false, erro: '',
    get maos() { return R.maos; },
    get mao() { return R.principal; },
    get lm() { return R.principal && R.principal.lm; },
    get pinca() { return !!(R.principal && R.principal.pinca); },
    get fechada() { return !!(R.principal && R.principal.fechada); },
    get segura() { return !!(R.principal && R.principal.segura); },
    get agarrou() { return !!(R.principal && R.principal.agarrou); },
    get soltou() { return !!(R.principal && R.principal.soltou); },
    get curva() { return R.principal ? R.principal.curva : [0, 0, 0, 0]; },

    async ligar(aviso) {
      aviso = aviso || (() => {});
      if (this.hl && this.video) return true;
      if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) { aviso('Este navegador não deixa usar a câmera'); return false; }
      try {
        aviso('Permita a câmera na tela do celular');
        // imagem menor = reconhecimento mais rápido (a imagem não aparece mesmo)
        this.stream = await navigator.mediaDevices.getUserMedia({ audio: false, video: { facingMode: { ideal: 'environment' }, width: { ideal: 640 }, height: { ideal: 480 } } });
        const v = document.createElement('video'); v.playsInline = true; v.muted = true; v.setAttribute('playsinline', ''); v.srcObject = this.stream;
        await v.play(); this.video = v;
      } catch (e) { aviso(e && e.name === 'NotAllowedError' ? 'A câmera não foi permitida' : 'Não deu para abrir a câmera'); this.erro = String(e && e.message || e); return false; }
      if (this.hl) return true;
      this.carregando = true; aviso('Carregando o reconhecimento das mãos…');
      try {
        const mp = await import(MP_BASE + 'vision_bundle.mjs');
        const fs = await mp.FilesetResolver.forVisionTasks(MP_BASE + 'wasm');
        const cria = delegate => mp.HandLandmarker.createFromOptions(fs, { baseOptions: { modelAssetPath: MP_MODELO, delegate }, runningMode: 'VIDEO', numHands: 2, minHandDetectionConfidence: 0.55, minHandPresenceConfidence: 0.5, minTrackingConfidence: 0.5 });
        try { this.hl = await cria('GPU'); } catch (e) { this.hl = await cria('CPU'); }
        aviso('Pronto! Mostre as mãos para a câmera');
      } catch (e) { this.erro = String(e && e.message || e); aviso('Não deu para ligar o reconhecimento da mão'); }
      this.carregando = false;
      return !!this.hl;
    },
    desligar() { if (this.stream) this.stream.getTracks().forEach(t => t.stop()); this.stream = null; this.video = null; R.maos.forEach(m => { m.lm = null; m.alvo = null; }); },
    ligada() { return !!(this.hl && this.video); },
    visivel(m) { return R.visivel(m || R.principal); },

    // chame uma vez por quadro; atualiza as mãos e os gestos (agarrou/soltou valem só neste quadro)
    atualizar() { R.detecta(this.hl, this.video); },

    // ponto da imagem da câmera → posição no mundo, numa distância a partir do olho.
    // A imagem é "esticada" para cobrir a vista do olho (o mesmo ajuste do Ryan VR).
    _escala(camera) {
      const D = 8, v = this.video, asp = v && v.videoWidth ? v.videoWidth / v.videoHeight : 16 / 9;
      const fov = lerAj('ryanvr-camfov2', 0);
      let w;
      if (fov) w = 2 * D * Math.tan(fov * Math.PI / 360);
      else { const vf = (camera ? camera.fov : 80) * Math.PI / 180, ea = camera ? camera.aspect : 1, hv = 2 * D * Math.tan(vf / 2) * 1.04; w = Math.max(hv * ea, hv * asp); }
      return { D, w, h: w / asp };
    },
    pontoNoMundo(camObj, camera, x, y, dist) {
      const e = this._escala(camera);
      const P = new THREE.Vector3((x - 0.5) * e.w, (0.5 - y) * e.h, -e.D).normalize().multiplyScalar(dist);
      return camObj.localToWorld(P);
    },
    // raio do olho até a palma da mão (ponto 9) — para mirar no que a mão está "em cima"
    raio(camObj, camera, ponto = 9, mao) {
      const m = mao || R.principal;
      if (!R.visivel(m)) return null;
      const o = camObj.getWorldPosition(new THREE.Vector3());
      const p = this.pontoNoMundo(camObj, camera, m.lm[ponto].x, m.lm[ponto].y, 1);
      return new THREE.Ray(o, p.sub(o).normalize());
    }
  };
  window.MaosVR = M;
})();
