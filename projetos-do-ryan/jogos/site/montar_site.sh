#!/bin/sh
# Monta o site público do Ryan VR na pasta docs/ da raiz do repositório (GitHub Pages).
# Rode de novo sempre que o jogo ou o APK mudarem:  sh projetos-do-ryan/jogos/site/montar_site.sh
set -e
J="$(cd "$(dirname "$0")/.." && pwd)"; RAIZ="$(cd "$J/../.." && pwd)"; D="$RAIZ/docs"
rm -rf "$D"; mkdir -p "$D/jogo/ryan-vr" "$D/jogo/blocos-vr" "$D/jogo/turbo-noturno" "$D/jogo/comum"
cp "$J/site/index.html" "$D/index.html"
# tamanho de verdade dos arquivos no botão de baixar (ex.: "APK · 12,6 MB")
mb() { awk -v b="$(wc -c < "$1")" 'BEGIN { printf "%.1f", b / 1000000 }' | tr . ,; }
sed -i "s/APK · [0-9]*,[0-9]* MB/APK · $(mb "$J/ryan-vr/RyanVR.apk") MB/; s/\.zip · [0-9]*,[0-9]* MB/.zip · $(mb "$J/ryan-vr/RyanVR-Computador.zip") MB/" "$D/index.html"
cp "$J/ryan-vr/RyanVR.apk" "$J/ryan-vr/RyanVR-Android.zip" "$J/ryan-vr/RyanVR-Computador.zip" "$D/"
cp "$J/ryan-vr/index.html" "$J/ryan-vr/manifest.json" "$J/ryan-vr/sw.js" "$J/ryan-vr/icon-192.png" "$J/ryan-vr/icon-512.png" "$J/ryan-vr/icon-maskable-512.png" "$D/jogo/ryan-vr/"
cp "$J/blocos-vr/index.html" "$D/jogo/blocos-vr/"
cp "$J/turbo-noturno/index.html" "$D/jogo/turbo-noturno/"
cp "$J/comum/maos-vr.js" "$D/jogo/comum/"
# Ajudante do Ryan (outro app, separado): APK e a versão para testar no navegador
A="$(cd "$J/../ajudante" && pwd)"; mkdir -p "$D/ajudante"
cp "$A/AjudanteDoRyan.apk" "$A/AjudanteDoRyan-Android.zip" "$D/"; cp "$A/index.html" "$A/icone.png" "$D/ajudante/"
# Fuga dos Dinos (outro site, separado)
mkdir -p "$D/fuga-dos-dinos"; cp "$J/../fuga-dos-dinos/index.html" "$D/fuga-dos-dinos/"
touch "$D/.nojekyll"   # o GitHub Pages entrega os arquivos como estão
printf 'User-agent: *\nAllow: /\nSitemap: https://denbatera83.github.io/suporte/sitemap.xml\n' > "$D/robots.txt"
HOJE=$(date +%Y-%m-%d)
cat > "$D/sitemap.xml" <<XML
<?xml version="1.0" encoding="UTF-8"?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
  <url><loc>https://denbatera83.github.io/suporte/</loc><lastmod>$HOJE</lastmod><priority>1.0</priority></url>
  <url><loc>https://denbatera83.github.io/suporte/jogo/ryan-vr/index.html</loc><lastmod>$HOJE</lastmod><priority>0.8</priority></url>
</urlset>
XML
echo "site pronto em $D"
