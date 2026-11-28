// =====================================================================
// CarroEsportivo.cs — física de carro esportivo para Unity (sem WheelCollider)
//
// O que este script simula:
//   • Suspensão por raio (raycast) com mola e amortecedor em cada roda.
//     A carga de cada pneu vem da compressão da mola, então a TRANSFERÊNCIA
//     DE PESO acontece de verdade: na frenagem o carro mergulha, a frente
//     ganha carga (mais aderência) e a traseira alivia (fica solta).
//   • Pneu com "Magic Formula" simplificada (Pacejka) para força longitudinal
//     (escorregamento da roda) e lateral (ângulo de deriva), combinadas num
//     círculo de atrito: se a roda traseira gasta a aderência acelerando,
//     sobra pouca para segurar de lado → PERDA DE TRAÇÃO TRASEIRA (drift).
//   • Motor com curva de TORQUE x RPM, limitador de giro, freio-motor,
//     embreagem de arrancada, câmbio automático ou manual e diferencial.
//   • Cada roda tem rotação própria (integrada em sub-passos), então ela
//     patina quando o torque passa da aderência e trava sem ABS.
//   • Arrasto aerodinâmico, downforce e resistência de rolagem.
//   • Freio de mão nas rodas traseiras para iniciar drift.
//
// Como montar na Unity:
//   1. Objeto do carro com Rigidbody (massa ~1400, Interpolate ligado,
//      Collision Detection = Continuous Dynamic) e um collider da carroceria.
//   2. Crie 4 objetos vazios nos pontos onde a suspensão prende na carroceria
//      (um pouco acima do centro de cada roda) e arraste em "rodas".
//      Marque dianteira/tracao em cada uma (tração traseira = só as de trás).
//   3. Arraste as malhas das rodas em "visual" (opcional).
//   4. Edit > Project Settings > Time > Fixed Timestep = 0.01 (100 Hz) ou menor.
//   5. Entrada padrão: Vertical (acelerar/frear), Horizontal (direção),
//      Espaço (freio de mão), E/Q (subir/descer marcha no modo manual).
//
// Outros scripts (áudio, HUD) podem ler: currentRPM, marchaAtual,
// velocidadeKmh, anguloDeriva e transferenciaPesoFrente.
// =====================================================================
using UnityEngine;

[RequireComponent(typeof(Rigidbody))]
public class CarroEsportivo : MonoBehaviour
{
    [System.Serializable]
    public class Roda
    {
        public Transform ponto;          // onde a suspensão prende na carroceria
        public Transform visual;         // malha da roda (opcional)
        public bool dianteira;
        public bool tracao;

        [HideInInspector] public float compressao, compressaoAnterior;
        [HideInInspector] public float carga;            // N (força normal no pneu)
        [HideInInspector] public float velAngular;       // rad/s
        [HideInInspector] public float slipRatio;        // escorregamento longitudinal
        [HideInInspector] public float slipAngle;        // rad (deriva lateral)
        [HideInInspector] public float anguloDirecao;    // rad
        [HideInInspector] public float rotacaoVisual;    // graus
        [HideInInspector] public bool noChao;
        [HideInInspector] public RaycastHit hit;
        [HideInInspector] public Vector3 velLocal;       // x = lateral, z = longitudinal
        [HideInInspector] public float torqueMotor, torqueFreio;
    }

    [Header("Rodas")]
    public Roda[] rodas = new Roda[4];
    public float raioRoda = 0.34f;
    public float inerciaRoda = 1.2f;           // kg·m² (roda + pneu + disco)

    [Header("Suspensão")]
    public float cursoSuspensao = 0.18f;       // m
    public float rigidezMola = 55000f;         // N/m
    public float amortecimento = 4500f;        // N·s/m
    public float barraEstabilizadora = 9000f;  // N/m (diferença de compressão no mesmo eixo)
    public LayerMask camadaChao = ~0;          // tire a camada do próprio carro daqui (ex.: crie a camada "Carro")

    [Header("Pneus (Magic Formula simplificada)")]
    public float muPneu = 1.15f;               // coeficiente de atrito pico
    public float B_long = 12f, C_long = 1.65f, E_long = 0.1f;
    public float B_lat = 9f, C_lat = 1.35f, E_lat = -0.6f;
    [Range(0.3f, 1.2f)] public float aderenciaTraseira = 1.0f;   // < 1 deixa a traseira mais solta
    [Range(0.1f, 1f)] public float aderenciaFreioDeMao = 0.45f;  // grip lateral traseiro com freio de mão puxado

    [Header("Motor")]
    public AnimationCurve curvaTorque = new AnimationCurve(
        new Keyframe(800, 260), new Keyframe(2500, 380), new Keyframe(4500, 470),
        new Keyframe(6000, 500), new Keyframe(7200, 450), new Keyframe(7800, 390));
    public float rpmMarchaLenta = 850f;
    public float rpmMaximo = 7800f;            // limitador
    public float inerciaMotor = 0.25f;         // kg·m²
    public float freioMotor = 0.06f;           // fração do torque usada como freio-motor sem acelerar
    public float rpmEmbreagem = 2600f;         // giro de arrancada com a embreagem patinando

    [Header("Câmbio")]
    public float[] relacoes = { 3.30f, 2.25f, 1.72f, 1.38f, 1.12f, 0.92f };
    public float relacaoRe = 3.0f;
    public float diferencial = 3.6f;
    public float eficiencia = 0.88f;
    public bool cambioAutomatico = true;
    public float rpmSubirMarcha = 7200f, rpmDescerMarcha = 3200f;
    public float tempoTroca = 0.15f;

    [Header("Freios e direção")]
    public float torqueFreioMax = 3200f;       // N·m por roda
    [Range(0f, 1f)] public float distribuicaoFreioFrente = 0.65f;
    public float torqueFreioDeMao = 4000f;
    public float anguloMaxDirecao = 32f;       // graus
    public float velocidadeDirecao = 4f;       // quão rápido o volante gira (1/s)
    public float entreEixos = 2.6f, bitola = 1.6f;

    [Header("Aerodinâmica")]
    public float Cd = 0.32f, areaFrontal = 2.1f, Cl = 0.9f; // Cl = downforce
    public float densidadeAr = 1.225f;
    public float resistenciaRolagem = 0.012f;

    [Header("Ajudas (0 = desligado)")]
    [Range(0f, 1f)] public float controleTracao = 0f;
    [Range(0f, 1f)] public float ajudaContraEsterco = 0f; // ajuda a corrigir o drift virando o volante sozinho

    // ---------------- leitura para áudio e HUD ----------------
    [HideInInspector] public float currentRPM;
    [HideInInspector] public int marchaAtual = 1;          // -1 ré, 0 ponto morto, 1..n
    [HideInInspector] public float velocidadeKmh;
    [HideInInspector] public float anguloDeriva;           // graus (ângulo do drift)
    [HideInInspector] public float transferenciaPesoFrente; // N a mais na frente (negativo = foi para trás)
    [HideInInspector] public float acelerador, freio;

    Rigidbody rb;
    float direcao, freioMao, timerTroca;
    float aceleracaoLongitudinal;
    Vector3 velAnterior;
    const int SUBPASSOS_RODA = 10;

    void Awake()
    {
        rb = GetComponent<Rigidbody>();
        rb.centerOfMass = new Vector3(0f, -0.25f, 0.05f); // centro de massa baixo (carro esportivo)
        rb.maxAngularVelocity = 20f;
        currentRPM = rpmMarchaLenta;
    }

    void Update()
    {
        // entrada (no Update para não perder teclas; usada no FixedUpdate)
        float v = Input.GetAxis("Vertical");
        float h = Input.GetAxis("Horizontal");
        acelerador = Mathf.Clamp01(v);
        freio = Mathf.Clamp01(-v);
        freioMao = Input.GetKey(KeyCode.Space) ? 1f : 0f;

        // volante com velocidade limitada e menos ângulo em alta velocidade
        float limiteVel = Mathf.Lerp(1f, 0.35f, Mathf.InverseLerp(10f, 60f, rb.velocity.magnitude));
        float alvo = h * anguloMaxDirecao * limiteVel;
        if (ajudaContraEsterco > 0f) alvo += anguloDeriva * ajudaContraEsterco * 0.6f;
        direcao = Mathf.MoveTowards(direcao, Mathf.Clamp(alvo, -anguloMaxDirecao, anguloMaxDirecao),
                                    anguloMaxDirecao * velocidadeDirecao * Time.deltaTime);

        if (!cambioAutomatico && timerTroca <= 0f)
        {
            if (Input.GetKeyDown(KeyCode.E) && marchaAtual < relacoes.Length) TrocarMarcha(marchaAtual + 1);
            if (Input.GetKeyDown(KeyCode.Q) && marchaAtual > -1) TrocarMarcha(marchaAtual - 1);
        }
        AtualizarVisual();
    }

    void FixedUpdate()
    {
        float dt = Time.fixedDeltaTime;
        Vector3 velLocalCarro = transform.InverseTransformDirection(rb.velocity);
        velocidadeKmh = velLocalCarro.z * 3.6f;
        aceleracaoLongitudinal = Vector3.Dot((rb.velocity - velAnterior) / dt, transform.forward);
        velAnterior = rb.velocity;

        // ré automática: freio segurado com o carro quase parado
        if (cambioAutomatico)
        {
            if (freio > 0.5f && velLocalCarro.z < 0.5f && marchaAtual > 0 && acelerador < 0.05f) TrocarMarcha(-1);
            else if (acelerador > 0.05f && marchaAtual == -1 && velLocalCarro.z > -0.5f) TrocarMarcha(1);
        }
        float aceleradorEfetivo = acelerador, freioEfetivo = freio;
        if (marchaAtual == -1) { aceleradorEfetivo = freio; freioEfetivo = acelerador; }

        Suspensao(dt);
        Direcao();
        Motor(aceleradorEfetivo, dt);
        Freios(freioEfetivo);
        Pneus(dt);
        Aerodinamica(velLocalCarro);
        if (cambioAutomatico) CambioAutomatico();
        timerTroca -= dt;

        // dados para HUD/áudio
        anguloDeriva = velLocalCarro.magnitude > 3f
            ? Mathf.Atan2(velLocalCarro.x, Mathf.Abs(velLocalCarro.z)) * Mathf.Rad2Deg : 0f;
        float cargaFrente = 0f, cargaTotal = 0f;
        foreach (var r in rodas) { cargaTotal += r.carga; if (r.dianteira) cargaFrente += r.carga; }
        // transferência analítica (m·a·h/L) para comparação com a da suspensão
        float alturaCG = Mathf.Max(0.3f, transform.InverseTransformPoint(rb.worldCenterOfMass).y + raioRoda);
        transferenciaPesoFrente = -rb.mass * aceleracaoLongitudinal * alturaCG / entreEixos;
    }

    // ---------------------------------------------------------------
    // SUSPENSÃO: mola + amortecedor + barra estabilizadora → carga em cada pneu
    // ---------------------------------------------------------------
    void Suspensao(float dt)
    {
        float alcance = cursoSuspensao + raioRoda;
        foreach (var r in rodas)
        {
            r.compressaoAnterior = r.compressao;
            r.noChao = Physics.Raycast(r.ponto.position, -r.ponto.up, out r.hit, alcance, camadaChao, QueryTriggerInteraction.Ignore);
            r.compressao = r.noChao ? Mathf.Clamp01((alcance - r.hit.distance) / cursoSuspensao) : 0f;
        }
        for (int i = 0; i < rodas.Length; i++)
        {
            var r = rodas[i];
            if (!r.noChao) { r.carga = 0f; continue; }
            float mola = r.compressao * cursoSuspensao * rigidezMola;
            float velComp = (r.compressao - r.compressaoAnterior) * cursoSuspensao / dt;
            float amort = velComp * amortecimento;
            // barra estabilizadora: compara com a roda do outro lado do mesmo eixo
            float barra = 0f;
            for (int j = 0; j < rodas.Length; j++)
                if (j != i && rodas[j].dianteira == r.dianteira)
                    barra = (r.compressao - rodas[j].compressao) * cursoSuspensao * barraEstabilizadora;
            r.carga = Mathf.Max(0f, mola + amort + barra);
            rb.AddForceAtPosition(r.hit.normal * r.carga, r.ponto.position);
        }
    }

    // direção com geometria de Ackermann (roda de dentro vira mais)
    void Direcao()
    {
        float a = direcao * Mathf.Deg2Rad;
        foreach (var r in rodas)
        {
            if (!r.dianteira) { r.anguloDirecao = 0f; continue; }
            if (Mathf.Abs(a) < 1e-4f) { r.anguloDirecao = 0f; continue; }
            float raio = entreEixos / Mathf.Tan(Mathf.Abs(a));
            bool esquerda = transform.InverseTransformPoint(r.ponto.position).x < 0f;
            bool dentro = (a < 0f) == esquerda;
            float raioRodaCurva = raio + (dentro ? -bitola / 2f : bitola / 2f);
            r.anguloDirecao = Mathf.Sign(a) * Mathf.Atan(entreEixos / raioRodaCurva);
        }
    }

    // ---------------------------------------------------------------
    // MOTOR: torque vem da curva Torque x RPM
    // ---------------------------------------------------------------
    void Motor(float acel, float dt)
    {
        float relacaoTotal = RelacaoAtual() * diferencial;
        int nTracao = 0; float omegaRodas = 0f;
        foreach (var r in rodas) if (r.tracao) { omegaRodas += r.velAngular; nTracao++; }
        omegaRodas /= Mathf.Max(1, nTracao);

        bool engatado = marchaAtual != 0 && timerTroca <= 0f;
        float rpmRodas = Mathf.Abs(omegaRodas * relacaoTotal) * 60f / (2f * Mathf.PI);

        if (engatado)
        {
            // embreagem de arrancada: abaixo do giro de embreagem o motor patina até rpmEmbreagem
            float rpmLivre = Mathf.Lerp(rpmMarchaLenta, rpmEmbreagem, acel);
            float alvo = Mathf.Max(rpmRodas, rpmRodas < rpmEmbreagem ? rpmLivre : rpmRodas);
            currentRPM = Mathf.Lerp(currentRPM, alvo, 1f - Mathf.Exp(-dt * 25f));
        }
        else
        {
            // ponto morto/troca: motor acelera ou cai livre pela inércia
            float torqueLivre = curvaTorque.Evaluate(currentRPM) * acel - curvaTorque.Evaluate(currentRPM) * freioMotor * 2f;
            currentRPM += torqueLivre / inerciaMotor * dt * 60f / (2f * Mathf.PI);
        }
        currentRPM = Mathf.Clamp(currentRPM, rpmMarchaLenta, rpmMaximo + 50f);

        // limitador de giro corta a injeção
        bool corte = currentRPM >= rpmMaximo;
        float torque = corte ? 0f : curvaTorque.Evaluate(currentRPM) * acel;
        // freio-motor quando solta o acelerador
        torque -= curvaTorque.Evaluate(currentRPM) * freioMotor * (1f - acel) * (currentRPM > rpmMarchaLenta + 200f ? 1f : 0f);

        float torqueRodas = engatado ? torque * relacaoTotal * eficiencia : 0f;
        if (marchaAtual == -1) torqueRodas = -Mathf.Abs(torqueRodas);

        // controle de tração: corta torque se a roda patinar demais
        if (controleTracao > 0f)
            foreach (var r in rodas)
                if (r.tracao && Mathf.Abs(r.slipRatio) > 0.12f) torqueRodas *= 1f - controleTracao * 0.7f;

        // diferencial aberto: divide igualmente entre as rodas de tração
        foreach (var r in rodas) r.torqueMotor = r.tracao ? torqueRodas / Mathf.Max(1, nTracao) : 0f;
    }

    void Freios(float frear)
    {
        foreach (var r in rodas)
        {
            float t = torqueFreioMax * frear * (r.dianteira ? distribuicaoFreioFrente : 1f - distribuicaoFreioFrente) * 2f;
            if (!r.dianteira) t += torqueFreioDeMao * freioMao;
            r.torqueFreio = t;
        }
    }

    // ---------------------------------------------------------------
    // PNEUS: rotação da roda + forças pela Magic Formula + círculo de atrito
    // ---------------------------------------------------------------
    static float MagicFormula(float x, float B, float C, float E)
    {
        float Bx = B * x;
        return Mathf.Sin(C * Mathf.Atan(Bx - E * (Bx - Mathf.Atan(Bx))));
    }

    void Pneus(float dt)
    {
        foreach (var r in rodas)
        {
            Quaternion rot = r.ponto.rotation * Quaternion.Euler(0f, r.anguloDirecao * Mathf.Rad2Deg, 0f);
            Vector3 frente = rot * Vector3.forward, lado = rot * Vector3.right;
            Vector3 pontoContato = r.noChao ? r.hit.point : r.ponto.position - r.ponto.up * (cursoSuspensao + raioRoda);
            Vector3 vel = rb.GetPointVelocity(pontoContato);
            r.velLocal = new Vector3(Vector3.Dot(vel, lado), 0f, Vector3.Dot(vel, frente));
            float vx = r.velLocal.z, vy = r.velLocal.x;

            float gripEixo = r.dianteira ? 1f : aderenciaTraseira;
            float gripLateral = (!r.dianteira && freioMao > 0f) ? aderenciaFreioDeMao : 1f;
            float maxF = muPneu * gripEixo * r.carga;

            // ângulo de deriva (com velocidade mínima para não explodir parado)
            r.slipAngle = Mathf.Atan2(-vy, Mathf.Max(Mathf.Abs(vx), 2f));

            // a roda gira em sub-passos (rígida: a força do pneu muda rápido com o escorregamento)
            float h = dt / SUBPASSOS_RODA, Fx = 0f;
            for (int k = 0; k < SUBPASSOS_RODA; k++)
            {
                float vRoda = r.velAngular * raioRoda;
                r.slipRatio = (vRoda - vx) / Mathf.Max(Mathf.Abs(vx), 3f);
                Fx = maxF * MagicFormula(r.slipRatio, B_long, C_long, E_long);
                float torqueReacao = Fx * raioRoda;
                float torqueLiquido = r.torqueMotor - torqueReacao;
                float novaOmega = r.velAngular + torqueLiquido / inerciaRoda * h;
                // freio: puxa a rotação para zero sem inverter o sentido
                float reducao = r.torqueFreio / inerciaRoda * h;
                if (Mathf.Abs(novaOmega) <= reducao) novaOmega = 0f; else novaOmega -= Mathf.Sign(novaOmega) * reducao;
                r.velAngular = novaOmega;
            }
            if (!r.noChao) continue;

            float Fy = maxF * gripLateral * MagicFormula(r.slipAngle, B_lat, C_lat, E_lat);

            // círculo de atrito: força combinada não passa do limite do pneu.
            // Na traseira, acelerar forte "rouba" a aderência lateral → drift.
            float total = Mathf.Sqrt(Fx * Fx + Fy * Fy), limite = maxF * Mathf.Max(gripLateral, 0.5f);
            if (total > limite && total > 1e-3f) { float s = limite / total; Fx *= s; Fy *= s; }

            // resistência de rolagem
            Fx -= Mathf.Sign(vx) * resistenciaRolagem * r.carga * Mathf.Clamp01(Mathf.Abs(vx));

            Vector3 forca = frente * Fx + lado * Fy;
            forca -= Vector3.Project(forca, r.hit.normal); // só no plano do chão
            rb.AddForceAtPosition(forca, pontoContato);
        }
    }

    void Aerodinamica(Vector3 velLocalCarro)
    {
        float v2 = rb.velocity.sqrMagnitude;
        float q = 0.5f * densidadeAr * v2;
        rb.AddForce(-rb.velocity.normalized * q * Cd * areaFrontal);
        rb.AddForce(-transform.up * q * Cl * areaFrontal);
    }

    // ---------------------------------------------------------------
    // CÂMBIO
    // ---------------------------------------------------------------
    float RelacaoAtual()
    {
        if (marchaAtual == -1) return -relacaoRe;
        if (marchaAtual == 0) return 0f;
        return relacoes[marchaAtual - 1];
    }

    void TrocarMarcha(int nova)
    {
        marchaAtual = Mathf.Clamp(nova, -1, relacoes.Length);
        timerTroca = tempoTroca;
    }

    void CambioAutomatico()
    {
        if (timerTroca > 0f || marchaAtual <= 0) return;
        bool patinando = false;
        foreach (var r in rodas) if (r.tracao && Mathf.Abs(r.slipRatio) > 0.2f) patinando = true;
        if (currentRPM > rpmSubirMarcha && marchaAtual < relacoes.Length && !patinando) TrocarMarcha(marchaAtual + 1);
        else if (currentRPM < rpmDescerMarcha && marchaAtual > 1)
        {
            // só desce se a marcha de baixo não passar do limitador
            float rpmAbaixo = currentRPM * relacoes[marchaAtual - 2] / relacoes[marchaAtual - 1];
            if (rpmAbaixo < rpmSubirMarcha - 400f) TrocarMarcha(marchaAtual - 1);
        }
    }

    // ---------------------------------------------------------------
    // VISUAL DAS RODAS
    // ---------------------------------------------------------------
    void AtualizarVisual()
    {
        foreach (var r in rodas)
        {
            if (!r.visual) continue;
            float distancia = r.noChao ? r.hit.distance - raioRoda : cursoSuspensao;
            r.visual.position = r.ponto.position - r.ponto.up * distancia;
            r.rotacaoVisual += r.velAngular * Mathf.Rad2Deg * Time.deltaTime;
            r.visual.rotation = r.ponto.rotation * Quaternion.Euler(r.rotacaoVisual, r.anguloDirecao * Mathf.Rad2Deg, 0f);
        }
    }

#if UNITY_EDITOR
    void OnDrawGizmos()
    {
        if (rodas == null) return;
        foreach (var r in rodas)
        {
            if (r == null || !r.ponto) continue;
            Gizmos.color = r.noChao ? Color.Lerp(Color.green, Color.red, Mathf.Abs(r.slipRatio) * 3f) : Color.gray;
            Gizmos.DrawLine(r.ponto.position, r.ponto.position - r.ponto.up * (cursoSuspensao + raioRoda));
        }
    }
#endif
}
