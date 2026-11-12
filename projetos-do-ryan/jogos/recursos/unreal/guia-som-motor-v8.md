# Som de motor V8 na Unreal Engine guiado por `currentRPM`

Guia técnico para fazer o som do motor (frequência e volume) acompanhar a variável `currentRPM` e soar como um V8 de verdade. Funciona na Unreal Engine 5 com **MetaSounds** (recomendado). No fim há a versão com **Sound Cue**, para projetos antigos.

---

## 1. Por que um V8 soa como V8

Antes de mexer no mixer, vale entender o que se está imitando:

| Característica | O que significa para o som |
|---|---|
| **Frequência de explosão** | Num 4 tempos, cada cilindro explode uma vez a cada 2 voltas. Num V8: `f = currentRPM / 60 × 8 / 2 = currentRPM / 15` Hz. Na marcha lenta (800 rpm) dá cerca de 53 Hz, um ronco grave. No corte (7.000 rpm) dá cerca de 467 Hz. |
| **"Borbulhar" do V8 americano** | Virabrequim *cross-plane*: as explosões não ficam igualmente espaçadas em cada bancada, e isso cria a batida irregular ("blub-blub") em giro baixo. Por isso não se usa um oscilador puro: o ideal é gravação real ou síntese com modulação irregular. |
| **Carga x sem carga** | Acelerando (*on-load*), o som é cheio e áspero. Soltando o pedal (*off-load*), fica oco, com estalos no escape. São **duas camadas diferentes**, não a mesma com volume menor. |
| **Três fontes** | Escape (traseira, grave e dominante), admissão (frente, ronco aspirado) e mecânica (correntes e válvulas, agudo). Cada fonte fica numa camada. |

---

## 2. Material de áudio (o que gravar ou comprar)

Use **loops gravados em RPM fixo**, não um único arquivo com pitch esticado:

- **On-load:** 8 a 10 loops, por exemplo 1000, 1500, 2000, 2500, 3500, 4500, 5500, 6500 e 7200 rpm.
- **Off-load:** os mesmos giros, com o pé fora do acelerador.
- **Extras:** marcha lenta, limitador, estalos de escape (*pops/crackles*), troca de marcha e partida.

Regras dos loops:

- Duração entre 1 e 3 segundos, com corte em cruzamento por zero e no mesmo ponto do ciclo, para não estalar na emenda.
- Grave em 48 kHz e normalize os loops de forma **relativa** entre si. Não normalize cada um no máximo: o volume natural por RPM faz parte do realismo.
- Anote o **RPM de referência** de cada loop. Ele entra na fórmula de pitch abaixo.

> Sem gravações reais, dá para começar com síntese (seção 7), mas o resultado mais convincente vem de loops gravados.

---

## 3. Fórmulas de modulação

### 3.1 Pitch de cada loop

Cada loop toca na velocidade certa quando:

```
razão_pitch     = currentRPM / RPM_referência_do_loop
semitons        = 12 × log2(currentRPM / RPM_referência_do_loop)
```

O nó **Wave Player** do MetaSounds recebe o pitch em **semitons** (entrada *Pitch Shift*). Limite cada loop a no máximo **±4 semitons** do original. Acima disso o timbre fica com cara de "esquilo" ou de "câmera lenta". É por isso que se usam vários loops.

### 3.2 Crossfade entre loops (volume)

Para um `currentRPM` entre dois loops vizinhos A (RPM_a) e B (RPM_b):

```
t       = clamp((currentRPM - RPM_a) / (RPM_b - RPM_a), 0, 1)
ganho_A = cos(t × π/2)        // potência constante (equal power)
ganho_B = sin(t × π/2)
```

Use **potência constante**, não linear. No crossfade linear o volume afunda no meio da transição e o motor parece "perder força".

### 3.3 Volume geral pelo RPM e pela carga

```
volume_motor   = lerp(0.55, 1.0, (currentRPM - 800) / (7200 - 800))
mistura_carga  = throttle suavizado (0 = off-load, 1 = on-load)
ganho_onload   = sin(mistura_carga × π/2)
ganho_offload  = cos(mistura_carga × π/2)
```

### 3.4 Suavização (obrigatório)

`currentRPM` chega da física a 50–120 Hz e pode ter degraus (troca de marcha, roda patinando). Suavize antes de usar:

```
rpm_suave += (currentRPM - rpm_suave) × (1 - exp(-dt × 18))
```

Use **18 a 25** de constante para o pitch, que precisa responder rápido, e **6 a 10** para a carga, para evitar que o som "pisque" quando o acelerador oscila.

---

## 4. MetaSounds passo a passo

### 4.1 Criar o MetaSound Source

1. Botão direito em **Content Browser → Audio → MetaSound Source**. Nome sugerido: `MS_MotorV8`.
2. Em **Source Settings**, marque **Looping** (o som do motor nunca termina sozinho) e escolha **Mono**. Motor é uma fonte pontual; a espacialização fica a cargo da atenuação.
3. Crie as **entradas** (painel *Members → Inputs*):

| Nome | Tipo | Padrão |
|---|---|---|
| `currentRPM` | Float | 800 |
| `Throttle` | Float | 0 |
| `RevLimiter` | Bool | false |
| `ShiftTrigger` | Trigger | — |

### 4.2 Grafo (camada on-load)

Para **cada** loop on-load:

```
[currentRPM] ──► [Divide (÷ RPM_ref)] ──► [Log2] ──► [Multiply (× 12)] ──► Wave Player.Pitch Shift
                                                                          Wave Player (Loop = true, Wave Asset = loop_XXXXrpm)
```

Depois **misture os loops** com o nó **Crossfade** (*Crossfade (Audio, N)*): ligue os Wave Players nas entradas em ordem de RPM e mande para a entrada de valor do crossfade o "índice fracionário" do RPM:

```
indice = Map Range(currentRPM, [RPM_min .. RPM_max] → [0 .. N-1])
```

Se os loops não estiverem igualmente espaçados em RPM, use uma **Float Curve / interpolação por faixas** (ou vários *Map Range* com *Clamp*) para calcular `indice`, garantindo que cada número inteiro caia exatamente no RPM de referência de um loop.

> Os nomes dos nós mudam um pouco entre versões da UE5. Procure por "Crossfade", "Map Range", "Log" e "Wave Player" na busca do grafo.

### 4.3 Camada off-load

Repita o 4.2 com os loops off-load, gerando a saída `Mix_OffLoad`.

### 4.4 Mistura de carga, volume e limitador

```
Throttle ─► [Interp / suavização] ─► carga
Mix_OnLoad  × sin(carga × π/2) ┐
Mix_OffLoad × cos(carga × π/2) ┴─► [Add] ─► [× volume_motor] ─► [× corte_limitador] ─► Out Mono
```

- **Limitador:** quando `RevLimiter = true`, module o ganho com uma **LFO quadrada de 12 a 18 Hz** entre 1,0 e 0,35. Isso imita o corte de injeção ("bra-bra-bra").
- **Troca de marcha:** no `ShiftTrigger`, aplique um **envelope AD** que baixe o ganho para cerca de 0,6 por 80–120 ms e toque um "pop" curto do escape.

### 4.5 Estalos de escape (off-load)

Dispare um **Random Get** com a lista de amostras de estalo quando **todas** as condições abaixo forem verdadeiras:

- `Throttle` caiu de mais de 0,7 para menos de 0,1 nos últimos 300 ms;
- `currentRPM > 3500`;
- um teste de probabilidade de 35% a 50% passou.

Toque com pitch aleatório de ±2 semitons e espaçamento aleatório de 60 a 180 ms, 2 a 5 estalos por vez.

### 4.6 Camadas extras (dão o "corpo" do V8)

| Camada | Como modular | Peso na mistura |
|---|---|---|
| **Sub-grave** (seno na frequência de explosão `currentRPM/15` Hz) | Volume sobe com a carga, filtro passa-baixa em 150 Hz | Baixo (−18 dB). É sentido mais do que ouvido. |
| **Admissão** | Volume = `Throttle × (currentRPM/7200)`, passa-faixa 300–1.500 Hz | −10 dB |
| **Mecânica** | Volume cresce com o RPM, passa-alta em 2 kHz | −20 dB |

---

## 5. Ligando à física (Blueprint ou C++)

No ator do carro, adicione um **Audio Component** com `MS_MotorV8` e atualize os parâmetros **todo Tick**:

**Blueprint (Event Tick):**
```
Get currentRPM (do componente de física) ─► Set Float Parameter (In Name = "currentRPM")
Get Throttle                              ─► Set Float Parameter (In Name = "Throttle")
RPM >= RPM do limitador                    ─► Set Bool Parameter  (In Name = "RevLimiter")
Ao trocar de marcha                        ─► Set Trigger Parameter (In Name = "ShiftTrigger")
```

**C++:**
```cpp
void AMeuCarro::Tick(float DeltaTime)
{
    Super::Tick(DeltaTime);
    // suavização (ver seção 3.4)
    RpmSuave += (CurrentRPM - RpmSuave) * (1.f - FMath::Exp(-DeltaTime * 20.f));
    AudioMotor->SetFloatParameter(TEXT("currentRPM"), RpmSuave);
    AudioMotor->SetFloatParameter(TEXT("Throttle"), Throttle);
    AudioMotor->SetBoolParameter(TEXT("RevLimiter"), CurrentRPM >= RpmLimitador);
}

void AMeuCarro::AoTrocarMarcha()
{
    AudioMotor->SetTriggerParameter(TEXT("ShiftTrigger"));
}
```

> O nome do parâmetro precisa ser **idêntico** ao da entrada do MetaSound, inclusive maiúsculas.

---

## 6. Mixer: Submix, efeitos e espaço

1. Crie um **Sound Submix** `SM_Motor` e aponte o MetaSound para ele (*Base Submix*). Dentro dele, em ordem:
   - **EQ:** +3 dB em 80–120 Hz (peito do V8), −2 dB em 250–400 Hz (tira o "som de caixa"), +2 dB em 2–3 kHz (aspereza do escape).
   - **Distorção leve / saturação:** drive baixo, mais forte com carga alta. Dá a sujeira dos escapes abertos.
   - **Compressor:** razão 3:1, ataque 10 ms e release 120 ms, para segurar os picos dos crossfades.
2. **Atenuação** (*Sound Attenuation*): raio interno de 3 m, queda até 60–80 m, com **filtro passa-baixa pela distância** (motor longe perde agudos).
3. **Doppler:** ative no Audio Component para carros que passam pela câmera.
4. **Câmera interna x externa:** na câmera de dentro do carro, aplique um passa-baixa em cerca de 2,5 kHz, +3 dB de grave e −6 dB na camada de escape. Sem isso, a câmera interna soa igual à de fora.
5. **Audio Modulation (opcional):** crie um **Control Bus** `CB_RPM`, alimente com o RPM normalizado (0–1) e use *Parameter Patches* para controlar EQ e volume de vários sons ao mesmo tempo (motor, turbo, transmissão) a partir de um só valor.

---

## 7. Alternativa sem gravação: síntese de V8

Se ainda não houver loops gravados:

1. **Gerador de pulsos** na frequência de explosão `currentRPM / 15` Hz. Use pulso estreito ou dente de serra passando por um passa-baixa de 600 Hz a 2 kHz que abre conforme o RPM.
2. **Irregularidade cross-plane:** module a fase/tempo dos pulsos com um padrão repetido a cada 8 explosões (por exemplo, atrasos de +6%, −4%, +3%, −5%...). É isso que cria o "blub-blub" característico.
3. **Ruído rosa** filtrado e modulado em amplitude pela mesma frequência, simulando o ar do escape.
4. **Ressonância do escape:** 1 ou 2 filtros passa-faixa fixos (por exemplo, 90 Hz e 180 Hz, Q alto). Eles não acompanham o RPM, e é essa interação com a frequência que muda que soa natural.

---

## 8. Sound Cue (alternativa para projetos antigos)

Sem MetaSounds:

1. Para cada loop, use **Wave Player (Looping)** → **Continuous Modulator**.
   - *Pitch Modulation Params:* `Parameter Name = currentRPM`, Min Input/Max Input = faixa de RPM do loop, Min Output/Max Output = `RPM_min/RPM_ref` e `RPM_max/RPM_ref`.
   - *Volume Modulation Params:* `Parameter Name = currentRPM`, com a curva de entrada e saída do crossfade (sobe na entrada da faixa, cai na saída).
2. Junte tudo num **Mixer**.
3. No Blueprint: `Audio Component → Set Float Parameter("currentRPM", RPM)`.

O Continuous Modulator só faz mapeamento linear, então crie **faixas sobrepostas curtas** para aproximar o crossfade de potência constante.

---

## 9. Checklist de realismo

- [ ] Nenhum loop é tocado com mais de ±4 semitons do RPM original.
- [ ] O crossfade é de potência constante e não há "buraco" de volume entre faixas.
- [ ] A troca de marcha tem queda rápida de RPM, com pitch descendo em cerca de 100 ms, e não um salto instantâneo.
- [ ] Soltar o acelerador muda o **timbre** (off-load), não só o volume.
- [ ] Os estalos de escape aparecem em desaceleração de giro alto, de forma aleatória e sem repetir o mesmo arquivo seguido.
- [ ] O limitador soa como corte intermitente.
- [ ] As câmeras interna e externa soam diferentes.
- [ ] O motor longe perde agudos (atenuação com filtro).
- [ ] O grave do V8 aparece em 80–120 Hz sem embolar a mistura (confira com fones e com caixa de som de celular).
