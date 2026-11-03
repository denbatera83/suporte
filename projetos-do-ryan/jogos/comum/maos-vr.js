// =====================================================================
// MaosVR — reconhece a mão pela câmera de trás do celular (MediaPipe, do Google)
// e transforma a mão de verdade em mão dentro do jogo. A imagem da câmera não
// aparece: você continua "em outro lugar", só a mão é escaneada.
// Usado pelo Blocos VR (e pode ser usado por outros jogos).
// =====================================================================
(function () {
  const MP_BASE = 'https://cdn.jsdelivr.net/npm/@mediapipe/tasks-vision@0.10.14/';
  const MP_MODELO = 'https://storage.googleapis.com/mediapipe-models/hand_landmarker/hand_landmarker/float16/1/hand_landmarker.task';
  const lerAj = (k, d) => { try { const v = localStorage.getItem(k); return v === null ? d : JSON.parse(v); } catch (e) { return d; } };

  const M = {
    lm: null, vistoEm: 0, ultimo: 0, hl: null, video: null, stream: null, carregando: false, erro: '',
    pinca: false, fechada: false, segura: false, agarrou: false, soltou: false,

    async ligar(aviso) {
      aviso = aviso || (() => {});
      if (this.hl && this.video) return true;
      if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) { aviso('Este navegador não deixa usar a câmera'); return false; }
      try {
        aviso('Permita a câmera na tela do celular');
        this.stream = await navigator.mediaDevices.getUserMedia({ audio: false, video: { facingMode: { ideal: 'environment' }, width: { ideal: 960 }, height: { ideal: 540 } } });
        const v = document.createElement('video'); v.playsInline = true; v.muted = true; v.setAttribute('playsinline', ''); v.srcObject = this.stream;
        await v.play(); this.video = v;
      } catch (e) { aviso(e && e.name === 'NotAllowedError' ? 'A câmera não foi permitida' : 'Não deu para abrir a câmera'); this.erro = String(e && e.message || e); return false; }
      if (this.hl) return true;
      this.carregando = true; aviso('Carregando o reconhecimento da mão…');
      try {
        const mp = await import(MP_BASE + 'vision_bundle.mjs');
        const fs = await mp.FilesetResolver.forVisionTasks(MP_BASE + 'wasm');
        const cria = delegate => mp.HandLandmarker.createFromOptions(fs, { baseOptions: { modelAssetPath: MP_MODELO, delegate }, runningMode: 'VIDEO', numHands: 1, minHandDetectionConfidence: 0.55, minHandPresenceConfidence: 0.5, minTrackingConfidence: 0.5 });
        try { this.hl = await cria('GPU'); } catch (e) { this.hl = await cria('CPU'); }
        aviso('Pronto! Mostre a mão para a câmera');
      } catch (e) { this.erro = String(e && e.message || e); aviso('Não deu para ligar o reconhecimento da mão'); }
      this.carregando = false;
      return !!this.hl;
    },
    desligar() { if (this.stream) this.stream.getTracks().forEach(t => t.stop()); this.stream = null; this.video = null; this.lm = null; },
    ligada() { return !!(this.hl && this.video); },
    visivel() { return !!(this.lm && performance.now() - this.vistoEm < 400); },

    // chame uma vez por quadro; atualiza a mão e os gestos (agarrou/soltou valem só neste quadro)
    atualizar() {
      this.agarrou = false; this.soltou = false;
      const v = this.video; if (!this.hl || !v || v.readyState < 2) return;
      const agora = performance.now(); if (agora - this.ultimo < 50) return; this.ultimo = agora;
      let r; try { r = this.hl.detectForVideo(v, agora); } catch (e) { return; }
      const lm = r && r.landmarks && r.landmarks[0];
      if (!lm) { if (this.lm && agora - this.vistoEm > 400) { this.lm = null; if (this.segura) { this.segura = false; this.soltou = true; } } return; }
      if (!this.lm) this.lm = lm.map(p => ({ x: p.x, y: p.y })); else lm.forEach((p, i) => { this.lm[i].x += (p.x - this.lm[i].x) * 0.6; this.lm[i].y += (p.y - this.lm[i].y) * 0.6; });
      this.vistoEm = agora;
      const d = (a, b) => Math.hypot(lm[a].x - lm[b].x, lm[a].y - lm[b].y);
      const tam = Math.max(1e-3, d(0, 9));
      // pinça: dedão encostado no indicador
      const pz = d(4, 8) / tam;
      if (!this.pinca && pz < 0.32) this.pinca = true; else if (this.pinca && pz > 0.5) this.pinca = false;
      // mão fechada: pelo menos 3 pontas de dedo perto da palma
      let dobrados = 0; [[8, 5], [12, 9], [16, 13], [20, 17]].forEach(([ponta, base]) => { if (d(ponta, 0) < d(base, 0) * 1.15) dobrados++; });
      this.fechada = dobrados >= 3;
      // curvatura de cada dedo (0 = esticado, 1 = dobrado), para a mão do jogo imitar
      this.curva = [[8, 5], [12, 9], [16, 13], [20, 17]].map(([p, b]) => Math.max(0, Math.min(1, 1.6 - d(p, 0) / Math.max(1e-3, d(b, 0)))));
      const segura = this.pinca || this.fechada;
      if (segura && !this.segura) this.agarrou = true;
      if (!segura && this.segura) this.soltou = true;
      this.segura = segura;
    },

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
    raio(camObj, camera, ponto = 9) {
      if (!this.visivel()) return null;
      const o = camObj.getWorldPosition(new THREE.Vector3());
      const p = this.pontoNoMundo(camObj, camera, this.lm[ponto].x, this.lm[ponto].y, 1);
      return new THREE.Ray(o, p.sub(o).normalize());
    }
  };
  window.MaosVR = M;
})();
