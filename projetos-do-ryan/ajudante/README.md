# Ajudante do Ryan

App **separado do Ryan VR** que conversa por voz (ouve e fala). Aperte o 🎤 e fale:

- 💬 *"manda mensagem pra Mãe dizendo estou com fome"*: o WhatsApp abre com a mensagem pronta para a pessoa certa, e **você aperta Enviar**. Também funciona em duas partes: *"fala pro Pai"* → "O que você quer dizer?" → *"vem me buscar"*.
- 📞 *"liga pro Pai"*: abre a tela de ligar com o número; você aperta o botão verde.
- 🚀 *"abre o YouTube"*, *"abre o Minecraft"*, *"joga Roblox"*: abre o app pelo nome. Entende nomes parecidos e jeitos diferentes que a voz escreve ("mine craft", "maicraft", "robloxx") e todas as versões do Minecraft (normal, Trial, Education). A aba **📱 Apps** mostra todos os apps do celular para abrir com um toque (o Minecraft aparece primeiro).
- ⛏️ *"vamos jogar Minecraft"*: dá um desafio (ex.: "construa uma casa em 5 minutos") e abre o Minecraft; quando você volta, ele pergunta se você conseguiu. *"me dá um desafio"* só sorteia o desafio.
- 🎙️ *"abre o assistente"*: abre o Assistente do Google.
- 😂 piadas, 🧮 contas (*"quanto é 7 vezes 8"*), ⏰ horas e 📅 data.

- 🧹 **Faxina** (aba 🧹 Celular, ou *"faz uma faxina"*): mostra o armazenamento e a memória (usado/livre, com barras), apaga os arquivos temporários do próprio Ajudante (mostrando quanto liberou de verdade) e abre o **"Liberar espaço" do Android**, onde você escolhe o que apagar. Um app comum não consegue apagar arquivos dos outros apps (o Android bloqueia), então o Ajudante não inventa números de "limpeza".
- 🔍 **Varredura** (*"faz uma varredura"*): bateria (carga, saúde, temperatura, voltagem), armazenamento, memória, tela (resolução, Hz, brilho), sensores, sistema (modelo, Android, há quanto tempo está ligado) e internet, com ✅/⚠️ e dicas faladas. Depois, testes que você confere: 🎨 cores da tela (pixel queimado), 👆 toque (passe o dedo em todos os quadrados) e 🔊 som. Também: *"como está a bateria"*, *"quanto espaço eu tenho"*.

Os contatos (nome, WhatsApp com DDD e um emoji) ficam só no celular, na aba 👥 Contatos. Também dá para escrever em vez de falar.

## Por que ele não faz as coisas sozinho
O Android não deixa um app apertar botões dentro de outros apps, e o WhatsApp proíbe programas que mandam mensagens sozinhos (pode banir o número). Por isso o Ajudante deixa tudo pronto e quem confirma é você. Pelo mesmo motivo, ele não consegue jogar *dentro* do Minecraft: ele abre o jogo e brinca com desafios.

## Sem permissões
A voz usa a tela de voz do Google, a fala usa a voz do próprio Android e a ligação abre a tela de ligar: o app não pede nenhuma permissão.

## Montar o APK
`android/build.sh chave.keystore senha` (mesmo esquema do Ryan VR: aapt, javac, dalvik-exchange, zipalign, apksigner). O APK fica em `android/build/AjudanteDoRyan.apk`.
