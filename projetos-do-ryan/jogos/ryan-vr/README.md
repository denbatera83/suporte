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

## Como usar
- **App Android:** instale o `RyanVR.apk` (permita "instalar apps desconhecidos"). Tudo funciona sem internet, inclusive o Turbo Noturno; o botão voltar do Android sai do jogo e volta para o Ryan VR.
- **Navegador:** https://raw.githack.com/denbatera83/suporte/ccr-ac78b9fc-ev2jcd/projetos-do-ryan/jogos/ryan-vr/index.html (ou, com o GitHub Pages ligado: https://denbatera83.github.io/suporte/projetos-do-ryan/jogos/ryan-vr/). No Chrome, os três pontinhos mostram **Instalar app**.

## Montar o APK de novo
`android/build.sh pasta_das_bibliotecas chave.keystore senha` copia as três páginas (Ryan VR, Blocos VR, Turbo Noturno) para dentro do app, troca as bibliotecas da internet por cópias locais e assina o APK.
