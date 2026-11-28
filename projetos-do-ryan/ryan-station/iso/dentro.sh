#!/bin/bash
# roda DENTRO do sistema novo (chroot): instala o Linux, a parte gráfica e o que o programa precisa
set -e
export DEBIAN_FRONTEND=noninteractive LANG=C.UTF-8
cat > /etc/apt/sources.list <<FIM
deb http://archive.ubuntu.com/ubuntu noble main restricted universe multiverse
deb http://archive.ubuntu.com/ubuntu noble-updates main restricted universe multiverse
FIM
apt-get update
apt-get install -y --no-install-recommends linux-image-generic linux-firmware casper initramfs-tools tzdata dbus \
  xserver-xorg-core xserver-xorg-input-libinput xinit x11-xserver-utils \
  libnss3 libatk1.0-0t64 libatk-bridge2.0-0t64 libcups2t64 libdrm2 libgtk-3-0t64 libgbm1 libasound2t64 libxss1 libxshmfence1 libxkbcommon0 \
  libxcomposite1 libxdamage1 libxrandr2 libxtst6 libpango-1.0-0 libcairo2 libgl1-mesa-dri libegl1 libgles2 libnotify4 libsecret-1-0 \
  alsa-utils fonts-dejavu-core fonts-noto-color-emoji
ln -sf /usr/share/zoneinfo/America/Sao_Paulo /etc/localtime; echo America/Sao_Paulo > /etc/timezone
echo ryanstation > /etc/hostname
# firmware: só o que serve para vídeo e som (deixa o ISO bem menor)
cd /lib/firmware && for d in *; do case "$d" in amdgpu|i915|radeon|nvidia|intel|xe|regulatory*|sof*|cirrus|ti*) ;; *) rm -rf "$d" ;; esac; done; cd /
# liga direto no RYAN STATION (sem tela de login) e desliga o PC quando você escolher "Desligar"
cat > /etc/systemd/system/ryanstation.service <<FIM
[Unit]
Description=RYAN STATION
After=systemd-user-sessions.service systemd-udev-settle.service sound.target
Conflicts=getty@tty1.service
[Service]
Environment=HOME=/root
ExecStart=/usr/bin/xinit /opt/ryanstation/iniciar.sh -- :0 vt1 -nolisten tcp -keeptty
ExecStopPost=/bin/systemctl poweroff
StandardInput=tty
TTYPath=/dev/tty1
TTYVTDisallocate=yes
[Install]
WantedBy=multi-user.target
FIM
cat > /opt/ryanstation/iniciar.sh <<FIM
#!/bin/sh
xset s off -dpms s noblank 2>/dev/null
amixer -q sset Master 85% unmute 2>/dev/null; amixer -q sset PCM 85% unmute 2>/dev/null
exec /opt/ryanstation/RyanStation --no-sandbox --ignore-gpu-blocklist
FIM
chmod +x /opt/ryanstation/iniciar.sh /opt/ryanstation/RyanStation
systemctl enable ryanstation.service
systemctl set-default multi-user.target
systemctl mask systemd-networkd-wait-online.service 2>/dev/null || true
echo 'root:ryan' | chpasswd
update-initramfs -c -k all || update-initramfs -u
apt-get clean; rm -rf /var/lib/apt/lists/* /usr/share/doc/* /usr/share/man/* /usr/share/locale/* /var/log/*.log
