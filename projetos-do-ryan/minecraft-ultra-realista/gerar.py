"""Gera o pacote de recursos "Ultra Realista" para Minecraft Bedrock (Visual Vibrante).

Uso: python3 gerar.py <pasta resource_pack do bedrock-samples da Mojang> <pasta de saída>

- Texturas: todas desenhadas aqui por computador (ruído, células e traços), em 64x64,
  com relevo (heightmap) e material (metal, brilho, aspereza, translucidez) para o Visual Vibrante.
- Luz, céu, cores, água e neblina: parte dos arquivos oficiais de exemplo da Mojang (mesmo nome e
  mesmo identificador, para valer em todos os biomas) e ajusta os valores para um visual mais realista.
"""
import json, math, os, shutil, sys
import numpy as np
from PIL import Image

N = 64
VANILLA, SAIDA = sys.argv[1], sys.argv[2]


# ---------------------------------------------------------------- ferramentas de desenho
def norm(a):
    a = a - a.min()
    return a / (a.max() + 1e-9)


def ruido(beta=2.0, seed=0, sx=1.0, sy=1.0):
    """Ruído fractal que emenda nas bordas (a textura repete sem costura). sx/sy esticam o grão."""
    r = np.random.default_rng(seed)
    F = np.fft.fft2(r.standard_normal((N, N)))
    fy = np.fft.fftfreq(N)[:, None] * sy
    fx = np.fft.fftfreq(N)[None, :] * sx
    f = np.sqrt(fx * fx + fy * fy)
    f[0, 0] = 1
    F = F / f ** (beta / 2)
    F[0, 0] = 0
    return norm(np.real(np.fft.ifft2(F)))


def voronoi(qtd, seed):
    """Células (pedras, cascalho) que também emendam nas bordas. Devolve F1, F2 e o número da célula."""
    r = np.random.default_rng(seed)
    pts = r.random((qtd, 2)) * N
    yy, xx = np.mgrid[0:N, 0:N] + 0.5
    d = np.empty((qtd, N, N))
    for i, (px, py) in enumerate(pts):
        dx = np.abs(xx - px); dx = np.minimum(dx, N - dx)
        dy = np.abs(yy - py); dy = np.minimum(dy, N - dy)
        d[i] = np.sqrt(dx * dx + dy * dy)
    o = np.sort(d, axis=0)
    return o[0], o[1], np.argmin(d, axis=0)


def suave(a, b, x):
    t = np.clip((x - a) / (b - a), 0, 1)
    return t * t * (3 - 2 * t)


def paleta(v, cores):
    """Pinta valores 0..1 com uma escala de cores [(posição, (r,g,b)), ...]."""
    v = np.clip(v, 0, 1)
    out = np.zeros(v.shape + (3,))
    pos = [p for p, _ in cores]
    for c in range(3):
        out[..., c] = np.interp(v, pos, [cor[c] for _, cor in cores])
    return out


def linha(img, alt, x0, y0, ang, comp, val, larg=1.0, hval=1.0):
    """Risca um traço (folha de grama) com emenda nas bordas."""
    passos = int(comp * 2) + 1
    for k in range(passos):
        t = k / max(1, passos - 1)
        x = (x0 + math.cos(ang) * comp * t) % N
        y = (y0 + math.sin(ang) * comp * t) % N
        xi, yi = int(x), int(y)
        img[yi, xi] = val * (0.75 + 0.25 * t)
        alt[yi, xi] = max(alt[yi, xi], hval * (0.4 + 0.6 * t))
        if larg > 1:
            img[yi, (xi + 1) % N] = val * 0.85
            alt[yi, (xi + 1) % N] = max(alt[yi, (xi + 1) % N], hval * 0.5)


def mers(m=0.0, e=0.0, r=0.8, s=0.0):
    """Mapa de material: metal (R), brilho próprio (G), aspereza (B), translucidez (A)."""
    f = lambda v: np.broadcast_to(np.asarray(v, dtype=float), (N, N))
    return np.stack([f(m), f(e), f(r), f(s)], -1)


# ---------------------------------------------------------------- os blocos
def pedra(seed, cores, manchas=None):
    base = ruido(2.3, seed) * 0.55 + ruido(1.1, seed + 1) * 0.45
    rachas = 1 - np.abs(2 * ruido(2.0, seed + 2) - 1)
    racha = suave(0.94, 0.995, rachas)
    v = base - racha * 0.35
    cor = paleta(v, cores)
    if manchas:
        for limiar, c, s2 in manchas:
            m = ruido(0.4, seed + s2) > limiar
            cor[m] = cor[m] * 0.35 + np.array(c) * 0.65
    alt = np.clip(base * 0.8 + 0.2 - racha * 0.6, 0, 1)
    return cor, alt, mers(r=0.72 + 0.15 * (1 - base))


def b_stone():
    return pedra(10, [(0, (88, 88, 90)), (0.5, (125, 125, 126)), (1, (160, 159, 157))])


def b_andesite():
    return pedra(20, [(0, (100, 101, 103)), (0.5, (134, 135, 136)), (1, (162, 163, 163))], [(0.93, (190, 190, 190), 5), (0.95, (70, 70, 72), 6)])


def b_diorite():
    return pedra(30, [(0, (170, 170, 168)), (0.5, (205, 204, 200)), (1, (232, 231, 228))], [(0.9, (85, 85, 88), 5), (0.94, (120, 118, 116), 6)])


def b_granite():
    return pedra(40, [(0, (120, 78, 62)), (0.5, (158, 106, 86)), (1, (190, 138, 115))], [(0.9, (60, 45, 40), 5), (0.93, (215, 190, 175), 6)])


def b_cobblestone():
    f1, f2, idx = voronoi(16, 50)
    borda = f2 - f1
    tons = np.random.default_rng(51).random(16)[idx]
    bolha = suave(0.0, 7.0, borda)
    grao = ruido(1.2, 52)
    v = 0.25 + 0.55 * tons + 0.25 * grao - 0.15 * (1 - bolha)
    cor = paleta(v, [(0, (70, 70, 72)), (0.5, (120, 119, 117)), (1, (165, 163, 158))])
    junta = borda < 1.4
    cor[junta] = np.array([52, 49, 46]) * (0.8 + 0.4 * grao[junta, None])
    alt = np.where(junta, 0.05, 0.25 + 0.75 * bolha * (0.85 + 0.15 * grao))
    return cor, alt, mers(r=np.where(junta, 0.95, 0.75))


def terra(seed=60):
    base = ruido(1.8, seed) * 0.7 + ruido(0.9, seed + 1) * 0.3
    cor = paleta(base, [(0, (66, 45, 30)), (0.5, (108, 76, 52)), (1, (140, 102, 72))])
    f1, f2, idx = voronoi(28, seed + 2)
    seixo = (f1 < 1.6) & (np.random.default_rng(seed + 3).random(28)[idx] > 0.55)
    cor[seixo] = cor[seixo] * 0.5 + np.array([150, 132, 110]) * 0.5
    escuro = ruido(0.5, seed + 4) > 0.82
    cor[escuro] *= 0.7
    alt = np.clip(base * 0.7 + seixo * 0.35, 0, 1)
    return cor, alt, mers(r=0.95)


def b_dirt():
    return terra()


def grama_cinza(seed, qtd=1100):
    """Grama em tons de cinza: o jogo pinta de verde conforme o bioma."""
    img = 0.38 + 0.18 * ruido(1.5, seed)
    alt = 0.2 * ruido(1.5, seed + 1)
    r = np.random.default_rng(seed + 2)
    for _ in range(qtd):
        linha(img, alt, r.random() * N, r.random() * N, r.random() * math.tau, r.uniform(2.5, 6.5), r.uniform(0.55, 1.0), hval=r.uniform(0.6, 1.0))
    return np.clip(img, 0, 1), np.clip(alt, 0, 1)


def b_grass_top():
    g, alt = grama_cinza(70)
    v = 95 + g * 115
    cor = np.stack([v, v, v], -1)
    return cor, alt, mers(m=0.0, r=0.62, s=0.3)


def b_grass_side():
    cor, alt, mt = terra(80)
    g, galt = grama_cinza(81, 900)
    r = np.random.default_rng(82)
    borda = 9 + 5 * ruido(1.6, 83)[0] + np.array([r.integers(0, 9) if r.random() < 0.35 else 0 for _ in range(N)])
    yy = np.mgrid[0:N, 0:N][0]
    mascara = yy < borda[None, :]
    v = 90 + g * 120
    cinza = np.stack([v, v, v], -1)
    sombra = (yy >= borda[None, :]) & (yy < borda[None, :] + 3)
    cor[sombra] *= 0.72
    cor = np.where(mascara[..., None], cinza, cor)
    alt = np.where(mascara, 0.55 + 0.45 * galt, alt * 0.8)
    alfa = np.where(mascara, 255, 0)
    mt = mers(r=np.where(mascara, 0.62, 0.95), s=np.where(mascara, 0.3, 0.0))
    return cor, alt, mt, alfa


def b_sand():
    grao = ruido(0.6, 90)
    yy, xx = np.mgrid[0:N, 0:N]
    ondas = 0.5 + 0.5 * np.sin(2 * math.pi * (yy * 4 + xx * 1) / N + ruido(2.0, 91) * 3)  # período que divide 64: emenda sem costura
    v = 0.55 * grao + 0.25 * ondas + 0.2 * ruido(1.8, 92)
    cor = paleta(v, [(0, (196, 178, 128)), (0.5, (219, 203, 155)), (1, (236, 224, 184))])
    escuro = ruido(0.3, 93) > 0.86
    cor[escuro] *= 0.8
    return cor, np.clip(0.3 + 0.4 * ondas + 0.3 * grao, 0, 1), mers(r=0.9)


def b_gravel():
    f1, f2, idx = voronoi(46, 100)
    borda = f2 - f1
    r = np.random.default_rng(101)
    tons = r.random(46)[idx]
    quente = r.random(46)[idx]
    bolha = suave(0.0, 3.5, borda)
    grao = ruido(1.0, 102)
    v = 0.2 + 0.65 * tons + 0.2 * grao
    cor = paleta(v, [(0, (72, 70, 68)), (0.5, (128, 124, 120)), (1, (178, 172, 165))])
    cor = cor * (0.85 + 0.3 * np.stack([quente, quente * 0.7, quente * 0.4], -1) * 0.5)
    vao = borda < 0.9
    cor[vao] = np.array([45, 42, 40])
    return cor, np.where(vao, 0.0, 0.2 + 0.8 * bolha), mers(r=np.where(vao, 0.95, 0.78))


def b_planks_oak():
    veio = ruido(2.2, 110, sx=6.0, sy=1.0)
    fino = ruido(1.0, 111, sx=4.0, sy=1.0)
    yy, xx = np.mgrid[0:N, 0:N]
    tabua = yy // 16
    r = np.random.default_rng(112)
    tom = r.uniform(-0.12, 0.12, 4)[tabua]
    corte = r.integers(6, 58, 4)
    v = 0.5 + 0.35 * (veio - 0.5) + 0.2 * (fino - 0.5) + tom
    v = v - 0.25 * (np.sin(veio * 30) > 0.7)
    cor = paleta(v, [(0, (110, 82, 46)), (0.5, (162, 130, 78)), (1, (196, 163, 108))])
    junta = (yy % 16 == 0) | (yy % 16 == 15)
    cor[junta] *= 0.55
    for t in range(4):
        m = (tabua == t) & (np.abs(xx - corte[t]) < 1)
        cor[m] *= 0.5
        for dx in (-4, 4):
            prego = (np.abs(yy - (t * 16 + 8)) <= 0) & (np.abs(xx - (corte[t] + dx) % N) <= 0)
            cor[prego] = (60, 55, 50)
    alt = np.clip(0.6 + 0.3 * (veio - 0.5) - 0.6 * junta, 0, 1)
    return cor, alt, mers(r=0.7 + 0.15 * fino)


def b_log_oak():
    casca = ruido(2.0, 120, sx=1.0, sy=7.0)
    fino = ruido(1.2, 121, sx=1.0, sy=3.0)
    sulco = suave(0.45, 0.25, casca)
    v = 0.25 + 0.6 * casca + 0.15 * fino - 0.35 * sulco
    cor = paleta(v, [(0, (40, 30, 21)), (0.5, (92, 72, 50)), (1, (138, 115, 86))])
    musgo = (ruido(1.5, 122) > 0.78) & (sulco < 0.3)
    cor[musgo] = cor[musgo] * 0.6 + np.array([90, 100, 55]) * 0.4
    return cor, np.clip(casca * 0.9 + 0.1 * fino - 0.4 * sulco, 0, 1), mers(r=0.93)


def b_log_oak_top():
    yy, xx = np.mgrid[0:N, 0:N] + 0.5
    c = N / 2
    ang = np.arctan2(yy - c, xx - c)
    raio = np.hypot(xx - c, yy - c) + 1.6 * np.sin(ang * 3 + 1) + 2.0 * (ruido(2.0, 130) - 0.5)
    aneis = 0.5 + 0.5 * np.sin(raio * 1.15)
    v = 0.55 + 0.25 * aneis + 0.15 * (ruido(1.0, 131) - 0.5)
    cor = paleta(v, [(0, (125, 92, 55)), (0.5, (170, 133, 84)), (1, (202, 168, 116))])
    linhas = aneis < 0.12
    cor[linhas] *= 0.75
    rach = (np.abs(ang - 0.6) < 0.03) & (raio < 22)
    cor[rach] *= 0.45
    borda = np.maximum(np.abs(xx - c), np.abs(yy - c)) > N / 2 - 5
    casca, _, _ = b_log_oak()
    cor = np.where(borda[..., None], casca, cor)
    cor[np.hypot(xx - c, yy - c) < 2] *= 0.6
    alt = np.where(borda, 0.8, 0.5 + 0.1 * aneis - 0.4 * rach)
    return cor, alt, mers(r=np.where(borda, 0.93, 0.8))


def tijolos(alt_linha, larg, junta, seed, cores_tij, cor_junta, pedra_tex=False):
    yy, xx = np.mgrid[0:N, 0:N]
    linha_i = yy // alt_linha
    desloc = (linha_i % 2) * (larg // 2)
    col = (xx + desloc) % N // larg
    idt = linha_i * 100 + col
    r = np.random.default_rng(seed)
    tons = {}
    for k in np.unique(idt):
        tons[k] = r.uniform(-0.18, 0.18)
    tom = np.vectorize(tons.get)(idt)
    ly = yy % alt_linha
    lx = (xx + desloc) % N % larg
    gasto = ruido(1.4, seed + 1)
    dist = np.minimum.reduce([ly, alt_linha - 1 - ly, lx, larg - 1 - lx]).astype(float) + (gasto - 0.5) * 1.6
    em_junta = dist < junta
    grao = ruido(1.6 if pedra_tex else 0.9, seed + 2)
    v = 0.5 + tom + 0.35 * (grao - 0.5)
    cor = paleta(v, cores_tij)
    if not pedra_tex:
        queimado = ruido(1.8, seed + 3) > 0.72
        cor[queimado] *= 0.78
    cor[em_junta] = np.array(cor_junta) * (0.85 + 0.3 * gasto[em_junta, None])
    chanfro = suave(junta, junta + 2.5, dist)
    alt = np.where(em_junta, 0.05, 0.35 + 0.5 * chanfro + 0.15 * grao)
    return cor, alt, mers(r=np.where(em_junta, 0.95, 0.82 if pedra_tex else 0.86))


def b_stonebrick():
    return tijolos(16, 32, 1.2, 140, [(0, (88, 88, 88)), (0.5, (122, 121, 120)), (1, (155, 154, 151))], (95, 93, 90), pedra_tex=True)


def b_brick():
    return tijolos(8, 16, 1.0, 150, [(0, (112, 52, 40)), (0.5, (150, 74, 56)), (1, (182, 102, 80))], (170, 164, 152))


def metal(seed, cores, rug):
    escovado = ruido(1.6, seed, sx=12.0, sy=1.0)
    yy, xx = np.mgrid[0:N, 0:N]
    d = np.minimum.reduce([yy, N - 1 - yy, xx, N - 1 - xx]).astype(float)
    bisel = suave(0, 4, d)
    v = 0.55 + 0.3 * (escovado - 0.5) + 0.15 * (bisel - 1)
    cor = paleta(v, cores)
    for cx, cy in ((5, 5), (N - 6, 5), (5, N - 6), (N - 6, N - 6)):
        m = (xx - cx) ** 2 + (yy - cy) ** 2 <= 3
        cor[m] *= 0.8
    alt = np.clip(0.4 + 0.5 * bisel + 0.05 * escovado, 0, 1)
    return cor, alt, mers(m=1.0, r=rug + 0.12 * (escovado - 0.5))


def b_iron_block():
    return metal(160, [(0, (150, 152, 156)), (0.5, (196, 198, 202)), (1, (226, 228, 230))], 0.32)


def b_gold_block():
    return metal(170, [(0, (190, 140, 40)), (0.5, (232, 184, 70)), (1, (250, 214, 120))], 0.28)


def b_leaves_oak():
    r = np.random.default_rng(180)
    img = np.full((N, N), 0.22)
    alt = np.zeros((N, N))
    cheio = np.zeros((N, N), bool)
    yy, xx = np.mgrid[0:N, 0:N] + 0.5
    for _ in range(150):
        cx, cy, a = r.random() * N, r.random() * N, r.random() * math.pi
        L, W = r.uniform(3.5, 6.5), r.uniform(1.6, 2.8)
        dx = (xx - cx + N / 2) % N - N / 2
        dy = (yy - cy + N / 2) % N - N / 2
        u = dx * math.cos(a) + dy * math.sin(a)
        w = -dx * math.sin(a) + dy * math.cos(a)
        d = (u / L) ** 2 + (w / W) ** 2
        folha = d < 1
        nervura = folha & (np.abs(w) < 0.35)
        tom = r.uniform(0.5, 1.0)
        img[folha] = tom * (0.85 + 0.15 * (1 - d[folha]))
        img[nervura] = tom * 0.7
        alt[folha] = np.maximum(alt[folha], 0.5 + 0.5 * (1 - d[folha]))
        cheio |= folha
    v = 70 + img * 150
    cor = np.stack([v, v, v], -1)
    alfa = np.where(cheio, 255, 0)
    return cor, alt, mers(r=0.55, s=np.where(cheio, 0.55, 0.0)), alfa


def minerio(seed, cor_min, metalico):
    cor, alt, mt = b_stone()
    f1, f2, idx = voronoi(9, seed)
    r = np.random.default_rng(seed + 1)
    tam = r.uniform(2.0, 4.0, 9)[idx]
    bruto = ruido(1.5, seed + 2)
    m = f1 + (bruto - 0.5) * 2.5 < tam
    cor[m] = np.array(cor_min) * (0.75 + 0.5 * bruto[m, None])
    alt = np.where(m, np.clip(alt + 0.25, 0, 1), alt)
    mt = mers(m=np.where(m, metalico, 0.0), r=np.where(m, 0.45, mt[..., 2]))
    return cor, alt, mt


def b_coal_ore():
    return minerio(190, (38, 36, 36), 0.0)


def b_iron_ore():
    return minerio(200, (214, 168, 128), 0.55)


def b_snow():
    v = 0.6 + 0.4 * ruido(1.6, 210)
    cor = paleta(v, [(0, (214, 222, 232)), (1, (246, 249, 252))])
    brilho = ruido(0.2, 211) > 0.93
    cor[brilho] = (255, 255, 255)
    return cor, 0.4 + 0.3 * ruido(2.2, 212), mers(r=np.where(brilho, 0.25, 0.7), s=0.7)


def b_clay():
    v = 0.5 + 0.3 * (ruido(2.4, 220) - 0.5) + 0.2 * (ruido(1.0, 221) - 0.5)
    cor = paleta(v, [(0, (140, 146, 158)), (0.5, (160, 166, 178)), (1, (182, 188, 198))])
    return cor, 0.5 + 0.2 * ruido(2.4, 222), mers(r=0.75, s=0.1)


BLOCOS = {
    'stone': (b_stone, 'png'), 'stone_andesite': (b_andesite, 'png'), 'stone_diorite': (b_diorite, 'png'), 'stone_granite': (b_granite, 'png'),
    'cobblestone': (b_cobblestone, 'png'), 'dirt': (b_dirt, 'png'), 'grass_top': (b_grass_top, 'png'), 'grass_side': (b_grass_side, 'tga'),
    'sand': (b_sand, 'png'), 'gravel': (b_gravel, 'png'), 'planks_oak': (b_planks_oak, 'png'), 'log_oak': (b_log_oak, 'png'),
    'log_oak_top': (b_log_oak_top, 'png'), 'stonebrick': (b_stonebrick, 'png'), 'brick': (b_brick, 'png'), 'iron_block': (b_iron_block, 'png'),
    'gold_block': (b_gold_block, 'png'), 'leaves_oak': (b_leaves_oak, 'tga'), 'coal_ore': (b_coal_ore, 'png'), 'iron_ore': (b_iron_ore, 'png'),
    'snow': (b_snow, 'png'), 'clay': (b_clay, 'png'),
}


def salvar_texturas(pasta):
    os.makedirs(pasta, exist_ok=True)
    feitas = {}
    for nome, (fn, ext) in BLOCOS.items():
        res = fn()
        cor, alt, mt = res[0], res[1], res[2]
        alfa = res[3] if len(res) > 3 else None
        rgb = np.clip(cor, 0, 255).astype(np.uint8)
        if alfa is not None:
            img = Image.fromarray(np.dstack([rgb, np.asarray(alfa, np.uint8)]), 'RGBA')
        else:
            img = Image.fromarray(rgb, 'RGB')
        img.save(os.path.join(pasta, f'{nome}.{ext}'))
        Image.fromarray((np.clip(alt, 0, 1) * 255).astype(np.uint8), 'L').save(os.path.join(pasta, f'{nome}_realista_altura.png'))
        Image.fromarray((np.clip(mt, 0, 1) * 255).astype(np.uint8), 'RGBA').save(os.path.join(pasta, f'{nome}_realista_mers.png'))
        with open(os.path.join(pasta, f'{nome}.texture_set.json'), 'w') as f:
            json.dump({'format_version': '1.21.30', 'minecraft:texture_set': {
                'color': nome, 'heightmap': f'{nome}_realista_altura', 'metalness_emissive_roughness_subsurface': f'{nome}_realista_mers'}}, f, indent=2)
        feitas[nome] = (cor, alt, mt, alfa)
    return feitas


# ---------------------------------------------------------------- luz, céu, cores, água e neblina
def escala_chave(v, k, teto=None):
    """Multiplica um número ou um conjunto de quadros-chave (hora do dia: valor)."""
    if isinstance(v, (int, float)):
        r = v * k
        return min(r, teto) if teto is not None else r
    if isinstance(v, dict):
        return {h: escala_chave(x, k, teto) for h, x in v.items()}
    return v


def mexer(nome_pasta, func):
    ori = os.path.join(VANILLA, nome_pasta)
    dest = os.path.join(SAIDA, nome_pasta)
    os.makedirs(dest, exist_ok=True)
    n = 0
    for arq in sorted(os.listdir(ori)):
        if not arq.endswith('.json'):
            continue
        with open(os.path.join(ori, arq)) as f:
            d = json.load(f)
        func(d)
        with open(os.path.join(dest, arq), 'w') as f:
            json.dump(d, f, indent=2)
        n += 1
    return n


def luz(d):
    s = d['minecraft:lighting_settings']
    dl = s.get('directional_lights', {})
    orb = dl.get('orbital', dl)
    if 'sun' in orb and 'illuminance' in orb['sun']:
        orb['sun']['illuminance'] = escala_chave(orb['sun']['illuminance'], 1.2)
    if 'moon' in orb and 'illuminance' in orb['moon']:
        orb['moon']['illuminance'] = escala_chave(orb['moon']['illuminance'], 1.3)
    if 'ambient' in s and 'illuminance' in s['ambient']:
        s['ambient']['illuminance'] = escala_chave(s['ambient']['illuminance'], 0.7)


def ceu(d):
    s = d['minecraft:atmosphere_settings']
    if 'rayleigh_strength' in s:
        s['rayleigh_strength'] = escala_chave(s['rayleigh_strength'], 1.15)
    if 'sun_mie_strength' in s:
        s['sun_mie_strength'] = escala_chave(s['sun_mie_strength'], 1.4)


def cores(d):
    s = d['minecraft:color_grading_settings']
    s['tone_mapping'] = {'operator': 'aces'}  # curva de filme, igual em todos os arquivos (não mistura entre biomas)
    mid = s.get('color_grading', {}).get('midtones')
    if mid and 'contrast' in mid:
        mid['contrast'] = [round(min(4.0, c * 1.06), 4) for c in mid['contrast']]


def agua(d):
    s = d['minecraft:water_settings']
    s.setdefault('waves', {})['enabled'] = True
    s.setdefault('caustics', {})['enabled'] = True


def montar():
    if os.path.exists(SAIDA):
        shutil.rmtree(SAIDA)
    os.makedirs(SAIDA)
    feitas = salvar_texturas(os.path.join(SAIDA, 'textures', 'blocks'))
    cont = {p: mexer(p, f) for p, f in (('lighting', luz), ('atmospherics', ceu), ('color_grading', cores), ('water', agua))}
    os.makedirs(os.path.join(SAIDA, 'shadows'))
    with open(os.path.join(SAIDA, 'shadows', 'global.json'), 'w') as f:
        json.dump({'format_version': '1.21.80', 'minecraft:shadow_settings': {'shadow_style': 'soft_shadows'}}, f, indent=2)
    # neblina padrão: a mesma do jogo + neblina de volume leve (raios de sol entre as árvores)
    with open(os.path.join(VANILLA, 'fogs', 'default_fog_setting.json')) as f:
        nev = json.load(f)
    nev['minecraft:fog_settings']['volumetric'] = {
        'density': {'air': {'max_density': 0.035, 'zero_density_height': 140.0, 'max_density_height': 64.0},
                    'water': {'max_density': 0.25, 'uniform': True}},
        'media_coefficients': {'air': {'scattering': [0.03, 0.03, 0.03], 'absorption': [0.0, 0.0, 0.0]},
                               'water': {'scattering': [0.01811, 0.02126, 0.027953], 'absorption': [0.2, 0.07874, 0.083465]}},
        'henyey_greenstein_g': {'air': {'henyey_greenstein_g': 0.55}, 'water': {'henyey_greenstein_g': 0.6}}}
    os.makedirs(os.path.join(SAIDA, 'fogs'))
    with open(os.path.join(SAIDA, 'fogs', 'default_fog_setting.json'), 'w') as f:
        json.dump(nev, f, indent=2)
    manifesto = {
        'format_version': 2,
        'header': {'name': 'Minecraft Ultra Realista', 'description': 'Texturas realistas com relevo, sol mais forte, sombras suaves, ondas na água e raios de luz. Use com o modo Visual Vibrante.',
                   'uuid': 'b3e8f2a4-6c1d-4f7e-9a2b-5d8c1e4f7a01', 'version': [1, 0, 0], 'min_engine_version': [1, 21, 120]},
        'modules': [{'type': 'resources', 'uuid': 'b3e8f2a4-6c1d-4f7e-9a2b-5d8c1e4f7a02', 'version': [1, 0, 0]}],
        'capabilities': ['pbr'],
    }
    with open(os.path.join(SAIDA, 'manifest.json'), 'w') as f:
        json.dump(manifesto, f, indent=2, ensure_ascii=False)
    icone(feitas, os.path.join(SAIDA, 'pack_icon.png'))
    return feitas, cont


def icone(feitas, caminho, T=256):
    """Bloco de grama em perspectiva isométrica, feito com as próprias texturas do pacote."""
    verde = np.array([145, 189, 89]) / 255
    topo = feitas['grass_top'][0] / 255 * verde
    lado = feitas['grass_side'][0] / 255
    lado = np.where((feitas['grass_side'][3] > 0)[..., None], lado * verde, lado)
    img = Image.new('RGBA', (T, T), (0, 0, 0, 0))
    def face(tex, M, luz):
        src = Image.fromarray((np.clip(tex * luz, 0, 1) * 255).astype(np.uint8)).convert('RGBA').resize((128, 128), Image.NEAREST)
        inv = np.linalg.inv(np.array([[M[0], M[1], M[2]], [M[3], M[4], M[5]], [0, 0, 1]]))
        img.alpha_composite(src.transform((T, T), Image.AFFINE, tuple(inv[:2].flatten()), Image.NEAREST))
    s = 1.72
    face(topo, (s * 0.5 * 0.87, -s * 0.5 * 0.87, T / 2, s * 0.25, s * 0.25, 20), 1.05)
    face(lado, (s * 0.5 * 0.87, 0, T / 2 - s * 64 * 0.87, s * 0.25, s * 0.5 * 1.0, 20 + s * 32), 0.85)
    face(lado, (s * 0.5 * 0.87, 0, T / 2, -s * 0.25, s * 0.5 * 1.0, 20 + s * 64), 0.62)
    img.save(caminho)


if __name__ == '__main__':
    feitas, cont = montar()
    print('texturas:', len(feitas), '| arquivos ajustados:', cont)
