const { app, BrowserWindow, Menu } = require('electron');
const path = require('path');
app.commandLine.appendSwitch('ignore-gpu-blocklist');
app.setName('Estrada Longa');
function criar() {
  const w = new BrowserWindow({ width: 1280, height: 760, title: 'Estrada Longa', backgroundColor: '#05070b', autoHideMenuBar: true, show: false,
    icon: path.join(__dirname, 'icone.png'), webPreferences: { backgroundThrottling: false } });
  Menu.setApplicationMenu(null);
  w.loadFile(path.join(__dirname, 'index.html'));
  w.once('ready-to-show', () => { w.maximize(); w.show(); });
  w.webContents.on('before-input-event', (e, i) => { if (i.type === 'keyDown' && i.key === 'F11') { w.setFullScreen(!w.isFullScreen()); e.preventDefault(); } });
  w.webContents.setWindowOpenHandler(() => ({ action: 'deny' }));
}
app.whenReady().then(criar);
app.on('window-all-closed', () => app.quit());
