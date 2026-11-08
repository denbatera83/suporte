#!/bin/sh
# Monta o APK do Castelo de Ferro 3D sem Android Studio. Uso: ./build.sh chave.keystore senha
set -e
cd "$(dirname "$0")"
JAR=/usr/lib/android-sdk/platforms/android-23/android.jar
KS=$1; PW=${2:-ryanvr123}
rm -rf assets build && mkdir -p assets build/gen build/obj build/dex
{ printf '<!doctype html>\n<meta charset="utf-8">\n<meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover">\n'; cat ../index.html; } > assets/index.html
aapt package -f -m -J build/gen -M AndroidManifest.xml -S res -A assets -I "$JAR" -F build/app.unsigned.apk
javac --release 8 -nowarn -classpath "$JAR" -d build/obj src/br/ryan/castelo/*.java build/gen/br/ryan/castelo/R.java
dalvik-exchange --dex --output=build/dex/classes.dex build/obj
(cd build/dex && aapt add ../app.unsigned.apk classes.dex)
zipalign -p -f 4 build/app.unsigned.apk build/app.aligned.apk
apksigner sign --ks "$KS" --ks-pass pass:"$PW" --ks-key-alias ryanvr --out build/CasteloDeFerro.apk build/app.aligned.apk
apksigner verify build/CasteloDeFerro.apk && echo "APK pronto: $(pwd)/build/CasteloDeFerro.apk"
