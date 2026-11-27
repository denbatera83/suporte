const { app, BrowserWindow, Menu, session } = require('electron');
const path = require('path');
app.commandLine.appendSwitch('ignore-gpu-blocklist');
app.commandLine.appendSwitch('autoplay-policy', 'no-user-gesture-required');
app.setName('Ryan Station');
function criar() {
  const w = new BrowserWindow({ width: 1280, height: 760, title: 'RYAN STATION', backgroundColor: '#000000', autoHideMenuBar: true, show: false, fullscreen: true,
    icon: path.join(__dirname, 'icone.png'), webPreferences: { backgroundThrottling: false, autoplayPolicy: 'no-user-gesture-required' } });
  Menu.setApplicationMenu(null);
  w.loadFile(path.join(__dirname, 'index.html'));
  w.once('ready-to-show', () => w.show());
  w.webContents.on('before-input-event', (e, i) => { if (i.type !== 'keyDown') return;
    if (i.key === 'F11') { w.setFullScreen(!w.isFullScreen()); e.preventDefault(); }
    if (i.key === 'F4' && i.alt) app.quit(); });
  w.webContents.setWindowOpenHandler(() => ({ action: 'deny' }));
}
app.whenReady().then(() => {
  const ses = session.defaultSession;
  ses.on('select-hid-device', (event, details, callback) => { event.preventDefault(); const d = details.deviceList[0]; callback(d ? d.deviceId : ''); });
  ses.setPermissionCheckHandler((wc, p) => p === 'hid' || p === 'fullscreen' || p === 'gamepad');
  ses.setDevicePermissionHandler(d => d.deviceType === 'hid');
  criar();
});
app.on('window-all-closed', () => app.quit());
