#!/bin/sh
# Monta o APK do Turbo Noturno sem Android Studio. Uso: ./build.sh pasta_libs(three/ + peerjs.min.js) chave.keystore senha
set -e
cd "$(dirname "$0")"
JAR=/usr/lib/android-sdk/platforms/android-23/android.jar
L=$1; KS=$2; PW=${3:-ryanvr123}
rm -rf assets build && mkdir -p assets/lib build/gen build/obj build/dex
sed -e 's#https://cdnjs.cloudflare.com/ajax/libs/three.js/r128/three.min.js#/lib/three/three.min.js#' \
    -e 's#https://cdn.jsdelivr.net/npm/three@0.128.0/#/lib/three/#' \
    -e 's#https://unpkg.com/peerjs@1.5.4/dist/peerjs.min.js#/lib/peerjs.min.js#' ../index.html > assets/index.html
cp -r "$L/three" assets/lib/; cp "$L/peerjs.min.js" assets/lib/
aapt package -f -m -J build/gen -M AndroidManifest.xml -S res -A assets -I "$JAR" -F build/app.unsigned.apk
javac --release 8 -nowarn -classpath "$JAR" -d build/obj src/br/ryan/turbo/*.java build/gen/br/ryan/turbo/R.java
dalvik-exchange --dex --output=build/dex/classes.dex build/obj
(cd build/dex && aapt add ../app.unsigned.apk classes.dex)
zipalign -p -f 4 build/app.unsigned.apk build/app.aligned.apk
apksigner sign --ks "$KS" --ks-pass pass:"$PW" --ks-key-alias ryanvr --out build/TurboNoturno.apk build/app.aligned.apk
apksigner verify build/TurboNoturno.apk && echo "APK pronto: $(pwd)/build/TurboNoturno.apk"
