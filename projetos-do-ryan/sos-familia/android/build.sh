#!/bin/sh
# Monta os dois APKs do SOS Família sem Android Studio. Uso: ./build.sh chave.keystore senha
#  - SOSFamilia.apk: versão simples (só localização; abre o app de mensagens pronto)
#  - SOSAutomatico.apk: manda o SMS sozinho e liga (pede SMS e ligação; o Play Protect pode barrar)
set -e
cd "$(dirname "$0")"
JAR=/usr/lib/android-sdk/platforms/android-23/android.jar
KS=$1; PW=${2:-ryanvr123}
rm -rf assets build && mkdir -p assets build/gen build/obj build/dex build/auto
{ printf '<!doctype html>\n<html lang="pt-BR">\n<meta charset="utf-8">\n<meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover">\n'; cat ../index.html; } > assets/index.html
sed 's#<uses-permission android:name="android.permission.VIBRATE" />#<uses-permission android:name="android.permission.SEND_SMS" />\n    <uses-permission android:name="android.permission.CALL_PHONE" />\n    <uses-permission android:name="android.permission.VIBRATE" />#' AndroidManifest.xml > build/auto/AndroidManifest.xml
aapt package -f -m -J build/gen -M AndroidManifest.xml -S res -A assets -I "$JAR" -F build/simples.unsigned.apk
aapt package -f -M build/auto/AndroidManifest.xml -S res-auto -S res --auto-add-overlay --rename-manifest-package br.familia.sos.auto -A assets -I "$JAR" -F build/auto.unsigned.apk
javac --release 8 -nowarn -classpath "$JAR" -d build/obj src/br/familia/sos/*.java build/gen/br/familia/sos/R.java
dalvik-exchange --dex --output=build/dex/classes.dex build/obj
for v in simples auto; do
  (cd build/dex && aapt add ../$v.unsigned.apk classes.dex >/dev/null)
  zipalign -p -f 4 build/$v.unsigned.apk build/$v.aligned.apk
done
apksigner sign --ks "$KS" --ks-pass pass:"$PW" --ks-key-alias ryanvr --out build/SOSFamilia.apk build/simples.aligned.apk
apksigner sign --ks "$KS" --ks-pass pass:"$PW" --ks-key-alias ryanvr --out build/SOSAutomatico.apk build/auto.aligned.apk
apksigner verify build/SOSFamilia.apk && apksigner verify build/SOSAutomatico.apk && echo "APKs prontos"
