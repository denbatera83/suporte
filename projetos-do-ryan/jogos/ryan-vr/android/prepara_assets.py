"""Copia as páginas para assets/ e troca as bibliotecas da internet por cópias locais (funciona sem internet)."""
import os, shutil, sys
AQUI = os.path.dirname(os.path.abspath(__file__))
JOGOS = os.path.normpath(os.path.join(AQUI, '..', '..'))
LIBS = sys.argv[1]  # pasta com: aframe-v1.6.0.min.js, three/ (three.min.js + examples/js), peerjs.min.js, mediapipe/ (vision_bundle.mjs, wasm/, hand_landmarker.task)
A = os.path.join(AQUI, 'assets')
shutil.rmtree(A, ignore_errors=True)
os.makedirs(os.path.join(A, 'lib'))
shutil.copy(os.path.join(LIBS, 'aframe-v1.6.0.min.js'), os.path.join(A, 'lib'))
shutil.copytree(os.path.join(LIBS, 'three'), os.path.join(A, 'lib', 'three'))
shutil.copy(os.path.join(LIBS, 'peerjs.min.js'), os.path.join(A, 'lib'))
shutil.copytree(os.path.join(LIBS, 'mediapipe'), os.path.join(A, 'lib', 'mediapipe'))
TROCAS = {
    'https://cdn.jsdelivr.net/npm/aframe@1.6.0/dist/aframe-v1.6.0.min.js': '../lib/aframe-v1.6.0.min.js',
    'https://cdnjs.cloudflare.com/ajax/libs/three.js/r128/three.min.js': '../lib/three/three.min.js',
    'https://cdn.jsdelivr.net/npm/three@0.128.0/examples/js/': '../lib/three/examples/js/',
    'https://unpkg.com/peerjs@1.5.4/dist/peerjs.min.js': '../lib/peerjs.min.js',
    'https://cdn.jsdelivr.net/npm/@mediapipe/tasks-vision@0.10.14/': '../lib/mediapipe/',
    'https://storage.googleapis.com/mediapipe-models/hand_landmarker/hand_landmarker/float16/1/hand_landmarker.task': '../lib/mediapipe/hand_landmarker.task',
}
for pasta, arquivo, extras in [('ryan-vr', 'index.html', ['icon-192.png']), ('blocos-vr', 'index.html', []), ('turbo-noturno', 'index.html', []), ('comum', 'maos-vr.js', [])]:
    os.makedirs(os.path.join(A, pasta))
    html = open(os.path.join(JOGOS, pasta, arquivo), encoding='utf-8').read()
    for de, para in TROCAS.items():
        html = html.replace(de, para)
    open(os.path.join(A, pasta, arquivo), 'w', encoding='utf-8').write(html)
    for e in extras:
        shutil.copy(os.path.join(JOGOS, pasta, e), os.path.join(A, pasta, e))
print('assets prontos')
