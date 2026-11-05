# Gera o add-on "Robô Gigante" (Minecraft Bedrock 1.21+). Uso: python3 gerar.py  -> RoboGigante.mcaddon
import json, os, shutil, zipfile
from PIL import Image, ImageDraw
U = dict(bp='7c1e4a52-3d1b-4b7e-9a51-1f0c2b8e6a01', bpd='7c1e4a52-3d1b-4b7e-9a51-1f0c2b8e6a02', bps='7c1e4a52-3d1b-4b7e-9a51-1f0c2b8e6a03',
         rp='7c1e4a52-3d1b-4b7e-9a51-1f0c2b8e6a04', rpr='7c1e4a52-3d1b-4b7e-9a51-1f0c2b8e6a05')
V = [1, 0, 0]; MIN = [1, 21, 0]
def w(p, d):
    os.makedirs(os.path.dirname(p), exist_ok=True)
    if isinstance(d, (dict, list)): d = json.dumps(d, indent=2, ensure_ascii=False)
    open(p, 'w', encoding='utf-8').write(d)
shutil.rmtree('pacotes', ignore_errors=True); BP, RP = 'pacotes/RoboGigante_BP', 'pacotes/RoboGigante_RP'

# ---------------- comportamento ----------------
w(f'{BP}/manifest.json', {"format_version": 2, "header": {"name": "Robô Gigante (comportamento)", "description": "Entre no robô gigante e atire bombas! Feito pelo Ryan.", "uuid": U['bp'], "version": V, "min_engine_version": MIN},
  "modules": [{"type": "data", "uuid": U['bpd'], "version": V}, {"type": "script", "language": "javascript", "uuid": U['bps'], "version": V, "entry": "scripts/main.js"}],
  "dependencies": [{"module_name": "@minecraft/server", "version": "1.11.0"}, {"uuid": U['rp'], "version": V}]})
nada = lambda c: {"cause": c, "deals_damage": False}
w(f'{BP}/entities/robo_gigante.json', {"format_version": "1.20.80", "minecraft:entity": {
  "description": {"identifier": "ryan:robo_gigante", "is_spawnable": True, "is_summonable": True, "is_experimental": False},
  "components": {
    "minecraft:type_family": {"family": ["robo_gigante", "mob"]}, "minecraft:nameable": {}, "minecraft:persistent": {}, "minecraft:fire_immune": {},
    "minecraft:health": {"value": 300, "max": 300}, "minecraft:collision_box": {"width": 2.5, "height": 6},
    "minecraft:physics": {}, "minecraft:pushable": {"is_pushable": False, "is_pushable_by_piston": True}, "minecraft:knockback_resistance": {"value": 1},
    "minecraft:movement": {"value": 0.3}, "minecraft:movement.basic": {}, "minecraft:jump.static": {}, "minecraft:input_ground_controlled": {},
    "minecraft:variable_max_auto_step": {"base_value": 1.0625, "controlled_value": 1.5625, "jump_prevented_value": 0.5625},
    "minecraft:rideable": {"seat_count": 1, "family_types": ["player"], "interact_text": "Entrar no robô", "pull_in_entities": False,
                           "seats": {"position": [0, 4.6, 0.2], "lock_rider_rotation": 0}},
    "minecraft:damage_sensor": {"triggers": [nada("fall"), nada("entity_explosion"), nada("block_explosion"), nada("suffocation")]}}}})
w(f'{BP}/entities/bomba.json', {"format_version": "1.20.80", "minecraft:entity": {
  "description": {"identifier": "ryan:bomba", "is_spawnable": False, "is_summonable": True, "is_experimental": False},
  "components": {"minecraft:collision_box": {"width": 0.5, "height": 0.5}, "minecraft:physics": {}, "minecraft:health": {"value": 1, "max": 1},
    "minecraft:pushable": {"is_pushable": False, "is_pushable_by_piston": False}, "minecraft:damage_sensor": {"triggers": [{"cause": "all", "deals_damage": False}]}}}})
w(f'{BP}/items/controle_robo.json', {"format_version": "1.20.80", "minecraft:item": {
  "description": {"identifier": "ryan:controle_robo", "menu_category": {"category": "equipment"}},
  "components": {"minecraft:icon": "ryan_controle", "minecraft:display_name": {"value": "Controle do Robô (atirar bomba)"}, "minecraft:max_stack_size": 1, "minecraft:hand_equipped": True}}})
w(f'{BP}/scripts/main.js', r'''import { world, system, ItemStack } from "@minecraft/server";
// Robô Gigante: montado no robô, use o Controle (toque e segure no celular / botão direito no PC) para atirar uma bomba.
const ROBO = "ryan:robo_gigante", BOMBA = "ryan:bomba", CONTROLE = "ryan:controle_robo";
const espera = new Map(); // jogador -> tick em que pode atirar de novo
const bombas = new Map(); // id da bomba -> { e, idade, robo }

function roboDe(jogador) {
  const r = jogador.getComponent("minecraft:riding");
  const e = r && r.entityRidingOn;
  return e && e.typeId === ROBO ? e : undefined;
}

world.afterEvents.itemUse.subscribe(({ source: jogador, itemStack }) => {
  if (!itemStack || itemStack.typeId !== CONTROLE) return;
  const robo = roboDe(jogador);
  if (!robo) { jogador.onScreenDisplay.setActionBar("§eEntre no Robô Gigante para atirar!"); return; }
  if ((espera.get(jogador.id) || 0) > system.currentTick) return;
  espera.set(jogador.id, system.currentTick + 15);
  const d = jogador.getViewDirection(), p = robo.location;
  const saida = { x: p.x + d.x * 3, y: p.y + 4.2 + d.y * 3, z: p.z + d.z * 3 };
  const b = robo.dimension.spawnEntity(BOMBA, saida);
  b.applyImpulse({ x: d.x * 2.2, y: d.y * 2.2 + 0.35, z: d.z * 2.2 });
  bombas.set(b.id, { e: b, idade: 0, robo: robo.id });
  robo.dimension.playSound("mob.blaze.shoot", saida);
});

system.runInterval(() => { // bomba explode quando bate no chão, num bicho, ou depois de 6 segundos
  for (const [id, b] of bombas) {
    const e = b.e; b.idade += 2;
    if (!e.isValid()) { bombas.delete(id); continue; }
    const v = e.getVelocity(), lenta = Math.abs(v.x) + Math.abs(v.y) + Math.abs(v.z) < 0.08;
    const perto = b.idade > 4 && e.dimension.getEntities({ location: e.location, maxDistance: 1.6 })
      .some(o => o.id !== e.id && o.id !== b.robo && o.typeId !== "minecraft:player" && o.typeId !== BOMBA);
    if ((b.idade > 4 && (e.isOnGround || lenta)) || perto || b.idade > 120) {
      const l = e.location, dim = e.dimension; e.remove(); bombas.delete(id);
      dim.createExplosion(l, 3, { breaksBlocks: true, causesFire: false });
    }
  }
}, 2);

system.runInterval(() => { // quem entra no robô ganha o controle
  for (const j of world.getAllPlayers()) {
    if (!roboDe(j)) continue;
    const inv = j.getComponent("minecraft:inventory").container; let tem = false;
    for (let i = 0; i < inv.size; i++) { const it = inv.getItem(i); if (it && it.typeId === CONTROLE) { tem = true; break; } }
    if (!tem) { inv.addItem(new ItemStack(CONTROLE, 1)); j.onScreenDisplay.setActionBar("§aVocê ganhou o Controle do Robô! Use para atirar bombas."); }
  }
}, 40);
''')

# ---------------- aparência ----------------
w(f'{RP}/manifest.json', {"format_version": 2, "header": {"name": "Robô Gigante (aparência)", "description": "Modelo 3D do Robô Gigante.", "uuid": U['rp'], "version": V, "min_engine_version": MIN},
  "modules": [{"type": "resources", "uuid": U['rpr'], "version": V}]})
# textura 64x64: cada 16x16 é uma cor/peça
CORES = {'metal': (0, 0), 'escuro': (16, 0), 'vermelho': (32, 0), 'visor': (48, 0), 'amarelo': (0, 16), 'bomba': (16, 16)}
im = Image.new('RGBA', (64, 64), (0, 0, 0, 0)); g = ImageDraw.Draw(im)
def bloco(x, y, base, borda, extra=None):
    g.rectangle([x, y, x + 15, y + 15], fill=base); g.rectangle([x, y, x + 15, y + 15], outline=borda)
    if extra: extra(x, y)
bloco(0, 0, (150, 158, 170), (90, 96, 108), lambda x, y: [g.point((x + 3, y + 3), (210, 215, 222)), g.point((x + 12, y + 3), (210, 215, 222)), g.point((x + 3, y + 12), (210, 215, 222)), g.point((x + 12, y + 12), (210, 215, 222)), g.line([x + 1, y + 8, x + 14, y + 8], (120, 128, 140))])
bloco(16, 0, (55, 60, 70), (30, 33, 40), lambda x, y: [g.line([x + 1, y + 4 + k * 4, x + 14, y + 4 + k * 4], (75, 80, 92)) for k in range(3)])
bloco(32, 0, (215, 50, 55), (140, 25, 30), lambda x, y: g.line([x + 1, y + 1, x + 14, y + 1], (255, 120, 120)))
bloco(48, 0, (60, 200, 255), (20, 110, 170), lambda x, y: g.rectangle([x + 2, y + 2, x + 6, y + 5], (210, 245, 255)))
bloco(0, 16, (250, 200, 40), (170, 120, 10), lambda x, y: [g.line([x + k, y, x + k - 15, y + 15], (40, 40, 40)) for k in range(0, 31, 6)])
bloco(16, 16, (30, 30, 34), (10, 10, 12), lambda x, y: g.rectangle([x, y + 6, x + 15, y + 9], (220, 40, 40)))
os.makedirs(f'{RP}/textures/entity', exist_ok=True); im.save(f'{RP}/textures/entity/robo_gigante.png'); im.save(f'{RP}/textures/entity/bomba.png')
def cubo(origem, tam, cor):
    u = list(CORES[cor]); f = {"uv": u, "uv_size": [16, 16]}
    return {"origin": origem, "size": tam, "uv": {k: f for k in ("north", "south", "east", "west", "up", "down")}}
def esp(c):  # espelha um cubo da esquerda para a direita
    o, s = c["origin"], c["size"]; return {**c, "origin": [-o[0] - s[0], o[1], o[2]]}
perna = [cubo([-14, 6, -6], [10, 34, 12], 'metal'), cubo([-13, 18, -7], [8, 8, 1], 'amarelo'), cubo([-16, 0, -10], [14, 6, 18], 'escuro')]
braco = [cubo([-32, 62, -8], [12, 12, 16], 'vermelho'), cubo([-30, 38, -6], [9, 24, 12], 'metal'), cubo([-31, 28, -16], [11, 11, 22], 'escuro'), cubo([-28, 31, -19], [5, 5, 3], 'visor')]
ossos = [
  {"name": "raiz", "pivot": [0, 0, 0]},
  {"name": "perna_e", "parent": "raiz", "pivot": [-9, 40, 0], "cubes": perna},
  {"name": "perna_d", "parent": "raiz", "pivot": [9, 40, 0], "cubes": [esp(c) for c in perna]},
  {"name": "corpo", "parent": "raiz", "pivot": [0, 40, 0], "cubes": [
    cubo([-16, 38, -9], [32, 8, 18], 'escuro'), cubo([-20, 46, -12], [40, 28, 24], 'metal'), cubo([-14, 50, -13], [28, 6, 1], 'amarelo'),
    cubo([-6, 60, -13], [12, 9, 1], 'visor'), cubo([-18, 74, -4], [36, 2, 16], 'escuro'), cubo([-14, 60, 12], [28, 16, 8], 'vermelho'),
    cubo([-10, 76, 14], [4, 6, 4], 'escuro'), cubo([6, 76, 14], [4, 6, 4], 'escuro')]},
  {"name": "cabeca", "parent": "corpo", "pivot": [0, 74, -8], "cubes": [cubo([-9, 74, -14], [18, 12, 10], 'metal'), cubo([-7, 78, -15], [14, 4, 1], 'visor'), cubo([-1, 86, -10], [2, 8, 2], 'vermelho')]},
  {"name": "braco_e", "parent": "corpo", "pivot": [-26, 70, 0], "cubes": braco},
  {"name": "braco_d", "parent": "corpo", "pivot": [26, 70, 0], "cubes": [esp(c) for c in braco]}]
w(f'{RP}/models/entity/robo_gigante.geo.json', {"format_version": "1.12.0", "minecraft:geometry": [{"description": {"identifier": "geometry.robo_gigante", "texture_width": 64, "texture_height": 64, "visible_bounds_width": 6, "visible_bounds_height": 7, "visible_bounds_offset": [0, 3, 0]}, "bones": ossos}]})
w(f'{RP}/models/entity/bomba.geo.json', {"format_version": "1.12.0", "minecraft:geometry": [{"description": {"identifier": "geometry.ryan_bomba", "texture_width": 64, "texture_height": 64}, "bones": [{"name": "b", "pivot": [0, 4, 0], "cubes": [cubo([-4, 0, -4], [8, 8, 8], 'bomba'), cubo([-1, 8, -1], [2, 3, 2], 'amarelo')]}]}]})
w(f'{RP}/animations/robo_gigante.animation.json', {"format_version": "1.8.0", "animations": {
  "animation.robo_gigante.andar": {"loop": True, "bones": {
    "perna_e": {"rotation": ["math.sin(query.anim_time * 260) * 24", 0, 0]}, "perna_d": {"rotation": ["-math.sin(query.anim_time * 260) * 24", 0, 0]},
    "braco_e": {"rotation": ["-math.sin(query.anim_time * 260) * 14", 0, 0]}, "braco_d": {"rotation": ["math.sin(query.anim_time * 260) * 14", 0, 0]},
    "corpo": {"position": [0, "math.abs(math.cos(query.anim_time * 260)) * 1.2", 0]}}},
  "animation.robo_gigante.parado": {"loop": True, "bones": {"corpo": {"position": [0, "math.sin(query.anim_time * 90) * 0.4", 0]}, "cabeca": {"rotation": [0, "math.sin(query.anim_time * 40) * 8", 0]}}},
  "animation.ryan_bomba.girar": {"loop": True, "bones": {"b": {"rotation": ["query.anim_time * 540", 0, 0]}}}}})
def cliente(ident, tex, geo, anims, roda, extra={}):
    return {"format_version": "1.10.0", "minecraft:client_entity": {"description": {"identifier": ident, "materials": {"default": "entity_alphatest"},
      "textures": {"default": tex}, "geometry": {"default": geo}, "animations": anims, "scripts": {"animate": roda}, "render_controllers": ["controller.render.default"], **extra}}}
w(f'{RP}/entity/robo_gigante.entity.json', cliente("ryan:robo_gigante", "textures/entity/robo_gigante", "geometry.robo_gigante",
  {"andar": "animation.robo_gigante.andar", "parado": "animation.robo_gigante.parado"}, [{"andar": "query.modified_move_speed > 0.02"}, {"parado": "query.modified_move_speed <= 0.02"}],
  {"spawn_egg": {"base_color": "#969eaa", "overlay_color": "#d73237"}}))
w(f'{RP}/entity/bomba.entity.json', cliente("ryan:bomba", "textures/entity/bomba", "geometry.ryan_bomba", {"girar": "animation.ryan_bomba.girar"}, ["girar"]))
ic = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); d = ImageDraw.Draw(ic)
d.rounded_rectangle([1, 4, 14, 12], 3, fill=(60, 64, 74), outline=(25, 25, 30)); d.rectangle([3, 7, 6, 8], (230, 230, 230)); d.rectangle([4, 6, 5, 9], (230, 230, 230))
d.ellipse([9, 6, 11, 8], (220, 45, 50)); d.ellipse([11, 8, 13, 10], (60, 200, 255)); d.line([7, 4, 7, 1], (220, 45, 50))
os.makedirs(f'{RP}/textures/items', exist_ok=True); ic.save(f'{RP}/textures/items/controle_robo.png')
w(f'{RP}/textures/item_texture.json', {"resource_pack_name": "robo_gigante", "texture_name": "atlas.items", "texture_data": {"ryan_controle": {"textures": "textures/items/controle_robo"}}})
lang = "entity.ryan:robo_gigante.name=Robô Gigante\nitem.spawn_egg.entity.ryan:robo_gigante.name=Gerar Robô Gigante\nentity.ryan:bomba.name=Bomba do Robô\npack.name=Robô Gigante\n"
w(f'{RP}/texts/pt_BR.lang', lang); w(f'{RP}/texts/en_US.lang', lang); w(f'{RP}/texts/languages.json', ["pt_BR", "en_US"])
# ícone dos pacotes: o robô de frente
p = Image.new('RGB', (256, 256), (40, 90, 160)); d = ImageDraw.Draw(p)
for y in range(256): d.line([0, y, 255, y], (40 + y // 6, 90 + y // 8, 170 - y // 6))
d.rectangle([0, 220, 255, 255], (70, 140, 60))
d.rectangle([88, 150, 116, 225], (150, 158, 170)); d.rectangle([140, 150, 168, 225], (150, 158, 170)); d.rectangle([82, 215, 120, 232], (55, 60, 70)); d.rectangle([136, 215, 174, 232], (55, 60, 70))
d.rectangle([70, 70, 186, 155], (150, 158, 170), outline=(90, 96, 108), width=3); d.rectangle([110, 100, 146, 120], (60, 200, 255)); d.rectangle([84, 128, 172, 138], (250, 200, 40))
d.rectangle([44, 70, 70, 95], (215, 50, 55)); d.rectangle([186, 70, 212, 95], (215, 50, 55)); d.rectangle([48, 95, 66, 150], (150, 158, 170)); d.rectangle([190, 95, 208, 150], (150, 158, 170))
d.rectangle([44, 145, 70, 168], (55, 60, 70)); d.rectangle([186, 145, 212, 168], (55, 60, 70))
d.rectangle([104, 34, 152, 70], (150, 158, 170), outline=(90, 96, 108), width=3); d.rectangle([110, 46, 146, 56], (60, 200, 255)); d.rectangle([125, 18, 131, 34], (215, 50, 55))
d.ellipse([196, 30, 226, 60], (30, 30, 34)); d.ellipse([214, 22, 226, 34], (255, 180, 40))
p.save(f'{BP}/pack_icon.png'); p.save(f'{RP}/pack_icon.png'); p.save('icone.png')
with zipfile.ZipFile('RoboGigante.mcaddon', 'w', zipfile.ZIP_DEFLATED) as z:
    for raiz, _, arqs in os.walk('pacotes'):
        for a in arqs: c = os.path.join(raiz, a); z.write(c, os.path.relpath(c, 'pacotes'))
print('pronto: RoboGigante.mcaddon')
