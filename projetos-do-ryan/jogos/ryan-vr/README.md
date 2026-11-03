# Ryan VR

Saguão de realidade virtual para **Cardboard**, feito com A-Frame: um deque de madeira no céu lilás, com painéis curvos em volta (menu, conteúdo e destaques) e uma barra embaixo. Tudo se escolhe pelo olhar (segure até o anel encher) ou pelo botão do Cardboard.

## O que tem
- **Vídeos 360°** (desenhados ao vivo, sem baixar nada), cada um com tela de detalhes e botão **Assistir**:
  - Montanha-Russa nas Nuvens (você anda no carrinho, com subidas, descidas e looping)
  - Fundo do Mar (cardumes, algas, bolhas e uma baleia)
  - Viagem pelo Espaço (asteroides, planeta com anéis e a Terra)
  - Aurora no Lago (aurora boreal com reflexo e vaga-lumes)
  - Durante o vídeo, olhe para baixo: pausar, barra de tempo e sair.
- **Jogos:** Blocos VR (abre em VR e volta para o Ryan VR pelo botão no chão) e Turbo Noturno (tela normal).
- **Meus vídeos:** escolha um vídeo da galeria e assista numa **sala de cinema** com tela curva gigante ou em **360°**. Nada é enviado para a internet.
- **Ajustes:** distância entre os olhos, correção da lente, tempo do olhar e música (valem também no Blocos VR).
- Toque longo na tela centraliza a visão.

## Modo Casa (câmera) e a mão como controle
- Aperte **📷** na barra de baixo (ou em Ajustes). A câmera de trás do celular mostra a sua casa dentro do Cardboard e os painéis flutuam na frente, como uma TV de realidade virtual. Nos vídeos 360° a câmera some para a imersão ser total.
- **Mão de verdade:** com o Modo Casa ligado, mostre a mão para a câmera. Aparecem bolinhas de luz nos dedos. Aponte o indicador para um botão e **junte o dedão com o indicador (pinça)** para apertar, ou deixe o dedo parado no botão por 0,8 s. O olhar continua funcionando.
- O reconhecimento da mão usa o MediaPipe (Google). No app ele vai dentro do APK e funciona sem internet. Nada da câmera é gravado ou enviado.
- **Estoura Balões** (em Jogos): balões aparecem na sua frente por 60 segundos; estoure com o dedo de verdade. Combo, balão dourado (+5) e recorde. Sem câmera, dá para estourar olhando.
- Ajustes: ligar/desligar a mão e o tamanho da imagem da câmera (Automático enche a vista).
- O Cardboard precisa ter o furo da câmera aberto (a maioria tem).

## Se algo der errado
- **Sem óculos**, toque direto nos botões, cartões, blocos e balões (como num app normal). No Cardboard, o botão do óculos escolhe o que está no centro da vista.
- Se o celular não tiver sensor de movimento, o app percebe e passa a usar o dedo para olhar em volta.
- Qualquer erro aparece numa caixa vermelha na tela, com a versão do Android e do navegador: tire uma foto e mande para o Claude.
- A versão aparece na tela de entrada (ex.: Ryan VR 1.2 · Chrome 120 · WebGL2).

## Como usar
- **App Android:** instale o `RyanVR.apk` (permita "instalar apps desconhecidos"). Tudo funciona sem internet, inclusive o Turbo Noturno e o reconhecimento da mão; o botão voltar do Android sai do jogo e volta para o Ryan VR. Na primeira vez que ligar o Modo Casa, o celular pede a permissão da câmera.
- **Navegador:** https://raw.githack.com/denbatera83/suporte/ccr-ac78b9fc-ev2jcd/projetos-do-ryan/jogos/ryan-vr/index.html (ou, com o GitHub Pages ligado: https://denbatera83.github.io/suporte/projetos-do-ryan/jogos/ryan-vr/). No Chrome, os três pontinhos mostram **Instalar app**.

## Montar o APK de novo
`android/build.sh pasta_das_bibliotecas chave.keystore senha` copia as três páginas (Ryan VR, Blocos VR, Turbo Noturno) para dentro do app, troca as bibliotecas da internet por cópias locais e assina o APK.
