#!/bin/sh
# Monta o APK do SOS Família sem Android Studio. Uso: ./build.sh chave.keystore senha
set -e
cd "$(dirname "$0")"
JAR=/usr/lib/android-sdk/platforms/android-23/android.jar
KS=$1; PW=${2:-ryanvr123}
rm -rf assets build && mkdir -p assets build/gen build/obj build/dex
{ printf '<!doctype html>\n<html lang="pt-BR">\n<meta charset="utf-8">\n<meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover">\n'; cat ../index.html; } > assets/index.html
aapt package -f -m -J build/gen -M AndroidManifest.xml -S res -A assets -I "$JAR" -F build/app.unsigned.apk
javac --release 8 -nowarn -classpath "$JAR" -d build/obj src/br/familia/sos/*.java build/gen/br/familia/sos/R.java
dalvik-exchange --dex --output=build/dex/classes.dex build/obj
(cd build/dex && aapt add ../app.unsigned.apk classes.dex)
zipalign -p -f 4 build/app.unsigned.apk build/app.aligned.apk
apksigner sign --ks "$KS" --ks-pass pass:"$PW" --ks-key-alias ryanvr --out build/SOSFamilia.apk build/app.aligned.apk
apksigner verify build/SOSFamilia.apk && echo "APK pronto"
