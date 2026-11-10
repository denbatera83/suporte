#!/bin/sh
# Monta o APK do SOS Alarme (celular de quem recebe). Uso: ./build.sh chave.keystore senha
set -e
cd "$(dirname "$0")"
JAR=/usr/lib/android-sdk/platforms/android-23/android.jar
KS=$1; PW=${2:-ryanvr123}
rm -rf build && mkdir -p build/gen build/obj build/dex
aapt package -f -m -J build/gen -M AndroidManifest.xml -S res -I "$JAR" -F build/app.unsigned.apk
javac --release 8 -nowarn -classpath "$JAR" -d build/obj src/br/familia/sosalarme/*.java build/gen/br/familia/sosalarme/R.java
dalvik-exchange --dex --output=build/dex/classes.dex build/obj
(cd build/dex && aapt add ../app.unsigned.apk classes.dex)
zipalign -p -f 4 build/app.unsigned.apk build/app.aligned.apk
apksigner sign --ks "$KS" --ks-pass pass:"$PW" --ks-key-alias ryanvr --out build/SOSAlarme.apk build/app.aligned.apk
apksigner verify build/SOSAlarme.apk && echo "APK pronto"
