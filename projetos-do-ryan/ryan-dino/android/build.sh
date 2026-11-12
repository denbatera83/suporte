#!/bin/sh
# Monta o APK do Ryan Dino sem Android Studio (Ubuntu/Debian):
#   sudo apt install aapt apksigner dalvik-exchange zipalign android-sdk-platform-23 default-jdk-headless
# Uso: ./build.sh pasta_mediapipe(vision_bundle.mjs + wasm/) three.min.js pose_landmarker_lite.task chave.keystore senha
set -e
cd "$(dirname "$0")"
JAR=/usr/lib/android-sdk/platforms/android-23/android.jar
MP=$1; THREE=$2; MODELO=$3; KS=$4; PW=${5:-ryanvr123}
rm -rf assets build && mkdir -p assets/lib/mediapipe build/gen build/obj build/dex
sed 's#https://cdnjs.cloudflare.com/ajax/libs/three.js/r128/three.min.js#/lib/three.min.js#' ../index.html > assets/index.html
cp ../icone.png assets/; cp "$THREE" assets/lib/three.min.js; cp "$MODELO" assets/lib/pose_landmarker_lite.task
cp "$MP/vision_bundle.mjs" assets/lib/mediapipe/; cp -r "$MP/wasm" assets/lib/mediapipe/
aapt package -f -m -J build/gen -M AndroidManifest.xml -S res -A assets -0 task -0 wasm -I "$JAR" -F build/app.unsigned.apk
javac --release 8 -nowarn -classpath "$JAR" -d build/obj src/br/ryan/dino/*.java build/gen/br/ryan/dino/R.java
dalvik-exchange --dex --output=build/dex/classes.dex build/obj
(cd build/dex && aapt add ../app.unsigned.apk classes.dex)
zipalign -p -f 4 build/app.unsigned.apk build/app.aligned.apk
apksigner sign --ks "$KS" --ks-pass pass:"$PW" --ks-key-alias ryanvr --out build/RyanDino.apk build/app.aligned.apk
apksigner verify build/RyanDino.apk && echo "APK pronto: $(pwd)/build/RyanDino.apk"
