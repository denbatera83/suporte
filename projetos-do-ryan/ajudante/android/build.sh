#!/bin/sh
# Monta o APK do Ajudante do Ryan sem Android Studio (Ubuntu/Debian):
#   sudo apt install aapt apksigner dalvik-exchange zipalign android-sdk-platform-23 default-jdk-headless
# Uso: ./build.sh chave.keystore senha
set -e
cd "$(dirname "$0")"
JAR=/usr/lib/android-sdk/platforms/android-23/android.jar
KS=${1:-ajudante.keystore}; PW=${2:-ryanvr123}
rm -rf assets build && mkdir -p assets build/gen build/obj build/dex
cp ../index.html ../icone.png assets/
aapt package -f -m -J build/gen -M AndroidManifest.xml -S res -A assets -I "$JAR" -F build/app.unsigned.apk
javac --release 8 -nowarn -classpath "$JAR" -d build/obj src/br/ryan/ajudante/*.java build/gen/br/ryan/ajudante/R.java
dalvik-exchange --dex --output=build/dex/classes.dex build/obj
(cd build/dex && aapt add ../app.unsigned.apk classes.dex)
zipalign -p -f 4 build/app.unsigned.apk build/app.aligned.apk
[ -f "$KS" ] || keytool -genkeypair -keystore "$KS" -alias ryanvr -keyalg RSA -keysize 2048 -validity 10000 -storepass "$PW" -keypass "$PW" -dname "CN=Ryan VR, O=Projetos do Ryan, C=BR"
apksigner sign --ks "$KS" --ks-pass pass:"$PW" --ks-key-alias ryanvr --out build/AjudanteDoRyan.apk build/app.aligned.apk
apksigner verify build/AjudanteDoRyan.apk && echo "APK pronto: $(pwd)/build/AjudanteDoRyan.apk"
