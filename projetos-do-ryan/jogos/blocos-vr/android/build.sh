#!/bin/sh
# Monta o APK do Blocos VR sem Android Studio (Ubuntu/Debian):
#   sudo apt install aapt apksigner dalvik-exchange zipalign android-sdk-platform-23 default-jdk-headless
# Uso: ./build.sh caminho/da/chave.keystore senha
set -e
cd "$(dirname "$0")"
JAR=/usr/lib/android-sdk/platforms/android-23/android.jar
KS=${1:-blocosvr.keystore}; PW=${2:-blocosvr123}
rm -rf build && mkdir -p build/gen build/obj build/dex
aapt package -f -m -J build/gen -M AndroidManifest.xml -S res -A assets -I "$JAR" -F build/app.unsigned.apk
javac --release 8 -nowarn -classpath "$JAR" -d build/obj src/br/ryan/blocosvr/*.java build/gen/br/ryan/blocosvr/R.java
dalvik-exchange --dex --output=build/dex/classes.dex build/obj
(cd build/dex && aapt add ../app.unsigned.apk classes.dex)
zipalign -p -f 4 build/app.unsigned.apk build/app.aligned.apk
[ -f "$KS" ] || keytool -genkeypair -keystore "$KS" -alias blocosvr -keyalg RSA -keysize 2048 -validity 10000 -storepass "$PW" -keypass "$PW" -dname "CN=Blocos VR, O=Projetos do Ryan, C=BR"
apksigner sign --ks "$KS" --ks-pass pass:"$PW" --ks-key-alias blocosvr --out build/BlocosVR.apk build/app.aligned.apk
apksigner verify --verbose build/BlocosVR.apk
echo "APK pronto: $(pwd)/build/BlocosVR.apk"
