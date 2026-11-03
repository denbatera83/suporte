# Blocos VR

Jogo de realidade virtual para **Cardboard** no celular, feito com [A-Frame](https://aframe.io) (HTML).

## Como jogar
- Olhe para um bloco e segure o olhar até o anel encher: a mão fecha e pega o bloco.
- Vire a cabeça até a caixa da mesma cor (●vermelha, ▲azul, ■amarela, ★verde) e segure o olhar para soltar.
- O botão do Cardboard (ou um toque na tela, ou Espaço no computador) pega e solta na hora.
- Bloco na caixa errada volta para a mesa. Acertos seguidos dão combo.
- 3 níveis: 8 blocos; 12 blocos + 1 de ouro (vale em qualquer caixa); 16 blocos + 2 de ouro com as caixas balançando. Estrelas pelo tempo e recorde salvo no celular.

## Link para abrir no navegador

- Na hora: https://raw.githack.com/denbatera83/suporte/ccr-ac78b9fc-ev2jcd/projetos-do-ryan/jogos/blocos-vr/index.html
- Fixo (depois de ligar o GitHub Pages neste repositório): https://denbatera83.github.io/suporte/projetos-do-ryan/jogos/blocos-vr/

No Chrome, os três pontinhos (⋮) mostram **Instalar app** / **Adicionar à tela inicial**: o jogo vira um ícone no celular e abre em tela cheia, até sem internet. Na tela de início do jogo também há o botão **Baixar APK**.

## Modo Cardboard

O jogo desenha o próprio modo Cardboard (não depende do WebXR, que a maioria dos celulares não aceita mais para Cardboard):
- duas imagens lado a lado, uma para cada olho (6,4 cm entre os olhos), com correção da curva das lentes;
- a cabeça vem do giroscópio do celular;
- contagem de 5 segundos para encaixar o celular, e a visão se centraliza sozinha nas mesas;
- toque longo na tela (ou no botão do Cardboard) recentraliza a visão;
- uma seta colorida mostra para que lado está a caixa certa quando você segura um bloco;
- se o celular estiver em pé, aparece o aviso para virar de lado.

## Como abrir no Cardboard
O modo Cardboard precisa do sensor de movimento do celular, que só funciona quando o jogo é aberto direto no navegador (o link de página do Claude bloqueia o sensor):
1. Abra o `index.html` no **Chrome do Android** (ou Safari do iPhone) por um endereço `https://` (por exemplo, GitHub Pages).
2. Toque em **Jogar com Cardboard**, coloque o celular deitado no óculos.
3. Use uma cadeira que gira para virar até as caixas.

Sem óculos: toque em **Jogar sem óculos** e mexa o celular ou arraste o dedo para olhar em volta.

## Aplicativo Android (APK)

`BlocosVR.apk` é o jogo como aplicativo: abre direto em tela cheia, deitado, com a tela sempre acesa e o A-Frame dentro do app (funciona sem internet).

Para instalar: baixe o `BlocosVR.apk` no celular Android, toque nele e permita **"instalar apps desconhecidos"** quando o celular pedir. Depois abra **Blocos VR** pelo ícone.

O código do app fica em `android/` e o script `android/build.sh` monta o APK de novo sem precisar do Android Studio.
