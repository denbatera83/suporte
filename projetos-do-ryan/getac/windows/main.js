const { app, BrowserWindow, Menu, session } = require('electron');
const path = require('path');
app.commandLine.appendSwitch('ignore-gpu-blocklist');
app.setName('GETAC');
function criar() {
  const w = new BrowserWindow({ width: 1280, height: 760, title: 'GETAC', backgroundColor: '#05070b', autoHideMenuBar: true, show: false,
    icon: path.join(__dirname, 'icone.png'), webPreferences: { backgroundThrottling: false } });
  Menu.setApplicationMenu(null);
  w.loadFile(path.join(__dirname, 'index.html'));
  w.once('ready-to-show', () => { w.maximize(); w.show(); });
  w.webContents.on('before-input-event', (e, i) => { if (i.type === 'keyDown' && i.key === 'F11') { w.setFullScreen(!w.isFullScreen()); e.preventDefault(); } });
  w.webContents.setWindowOpenHandler(() => ({ action: 'deny' }));
}
app.whenReady().then(() => {
  const ses = session.defaultSession;
  // modo direto USB: escolhe sozinho o controle de jogo da lista (o jogo só pede joystick/gamepad)
  ses.on('select-hid-device', (event, details, callback) => { event.preventDefault(); const d = details.deviceList[0]; callback(d ? d.deviceId : ''); });
  ses.setPermissionCheckHandler((wc, permissao) => permissao === 'hid' || permissao === 'fullscreen' || permissao === 'gamepad' ? true : false);
  ses.setDevicePermissionHandler(d => d.deviceType === 'hid');
  criar();
});
app.on('window-all-closed', () => app.quit());
