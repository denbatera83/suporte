# Turbo Noturno

Jogo de corrida em 3D que roda direto no navegador, com modo carreira de 20 fases, oficina para melhorar as peças do carro e personalização visual. A física e os gráficos tentam chegar o mais perto possível de um carro de verdade.

## Como jogar

Abra o arquivo `index.html` no navegador (Chrome, Edge ou Firefox). É preciso estar conectado à internet, porque o jogo carrega a biblioteca 3D (three.js) online. O progresso (dinheiro, fases, peças e visual) fica salvo no próprio navegador.

| Tecla | Ação |
|---|---|
| ↑ / W | Acelerar |
| ↓ / S | Frear (parado, segurar engata a ré) |
| ← → / A D | Virar |
| Shift | Nitro |
| Espaço | Freio de mão |
| L | Farol alto ou baixo |
| C | Trocar a câmera (atrás, longe, capô, para-choque) |
| R | Voltar para a pista (na cidade: desvirar o carro na rua) |
| E | Na cidade: sair/entrar no carro e começar missões |
| M | Ligar ou desligar o som |
| P / Esc | Pausa |

Também funciona com controle de videogame. No celular, jogue com o aparelho deitado: arraste o botão redondo da esquerda para virar e use os botões da direita para acelerar, frear, nitro e freio de mão.

## Menu principal

- Três botões grandes para jogar: **Carreira**, **Cidade Livre** e **Multiplayer**.
- Grade de ícones: **Carros**, **Oficina**, **Visual**, **Conquistas**, **Fases** e **Opções**.
- No alto, o perfil do piloto com nível, barra de experiência e dinheiro.
- No canto, a vitrine do carro em uso com velocidade, aceleração e controle (botão *Trocar carro*).
- Tela **Carros**: os 8 veículos com descrição e barras; os bloqueados liberam com o anúncio de cada um.
- Conquistas e subida de nível aparecem como cartões animados com som; a tela de carregamento mostra dicas.

## Cidade viva

- **Dia e noite:** o tempo passa sozinho na Cidade Livre (noite, amanhecer, dia, entardecer) e às vezes **chove** (a pista fica mais escorregadia).
- **⛽ Posto e lava-jato:** pare no marcador verde e aperte E. Por R$ 100 o carro é lavado e o nitro enche.
- **🚁 Heliporto:** um helicóptero de verdade no heliporto. Aperte E no marcador (pode ser a pé) para decolar. Acelera para frente, Espaço sobe, Q desce, V pousa e E sai.
- **🚕 Táxi:** busque 2 passageiros (eles acenam no ponto amarelo) e leve cada um ao destino. Quanto mais rápido, mais gorjeta.
- **🍕 Pizza:** entregue 3 pizzas antes de esfriarem.
- Conquistas novas: Taxista e Pizza quentinha.

## Modos de corrida

No menu, **Modos de corrida** abre a escolha da pista (qualquer fase já liberada) e os modos:

| Modo | Como funciona |
|---|---|
| ⏱️ Contra o relógio | Sozinho, 3 voltas. A melhor volta vira um **fantasma** azul transparente que corre com você na próxima vez. |
| 🚓 Fuga da polícia | 3 viaturas com sirene perseguem você por 2 voltas. Se ficar devagar perto delas, a barra de **cerco** enche e você é pego. |
| 🚦 Arrancada | 400 metros lado a lado contra um rival. |
| 💨 Desafio de drift | 2 voltas fazendo pontos derrapando. O combo cresce enquanto você derrapa e zera se bater no muro. Medalhas: bronze 1.500, prata 3.500, ouro 6.000. |
| 🏆 Campeonato | 5 copas (Bronze, Prata, Ouro, Platina, Diamante), 4 corridas cada. Pontos 10-8-6-4-2-1; quem somar mais leva o troféu e o prêmio. Cada copa libera junto com as fases da carreira. |

Novas conquistas: fantasma, fugitivo, largada perfeita, rei do drift e campeão de copa.

## Opções

- **Direção:** Grudado (padrão: o carro não escorrega nas curvas), Drift (a traseira solta, para quem gosta de derrapar) ou Simulação (física completa). Em todos, o freio de mão faz o carro derrapar.
- **Gráficos:** Baixo, Médio, Alto ou Ultra. A resolução também se ajusta sozinha durante o jogo para não travar.
- **Computador fraco:** diminui bastante os gráficos (resolução, sombras, reflexos, árvores, prédios, trânsito e pedestres) para rodar em máquinas fracas. Se o jogo ficar muito lento, ele mesmo sugere ligar essa opção.
- **Ajudas de pilotagem** e **som** ligados ou desligados.

## Cidade Livre (mundo aberto)

No menu, o botão **🌆 Cidade Livre** abre uma cidade grande para explorar à vontade, de noite, com ruas, quarteirões, praças, estacionamentos e prédios iluminados.

- **Sair do carro e andar a pé:** aperte **E** com o carro parado. A pé, as setas andam e viram, **Shift** corre e **Espaço** pula.
- **Entrar em qualquer carro:** chegue perto de um carro do trânsito ou estacionado e aperte **E**. O motorista desce e vai embora. Seu carro fica estacionado onde você deixou (aparece em azul no mapa).
- **Trânsito:** carros andam pelas ruas, viram nas esquinas, param quando tem algo na frente e acendem a luz de freio.
- **Pedestres:** andam nas calçadas e pulam para o lado quando um carro vem. Se alguém for atropelado, cai e levanta.
- **Polícia e estrelas de procurado:** bater em carros, correr acima de 120 km/h perto de uma viatura, atropelar ou pegar um carro na frente da polícia sobe as estrelas (até 5). As viaturas chegam com sirene e luzes piscando. Fique longe delas até a barrinha encher para **despistar** (e ganhar dinheiro). Se parar perto da viatura, você é **preso** e paga multa.
- **Missões** (marcadores coloridos, aperte **E** dentro deles):
  - 🏁 **Corrida de rua:** passe pelos pontos amarelos antes do tempo acabar.
  - 📦 **Entrega:** leve a encomenda até o ponto azul. Cada batida diminui o pagamento.
  - 🚨 **Fuga da polícia:** começa com 3 estrelas; despiste para ganhar.
  - 🔧 **Oficina:** melhora as peças e o visual do carro ali mesmo.
- **Dinheiro nas ruas:** moedas douradas espalhadas valem R$ 100 cada.
- Uma seta amarela em cima do carro aponta para o objetivo da missão, e o mapa mostra missões, moedas e viaturas.

O dinheiro ganho na cidade é o mesmo da carreira, então dá para juntar dinheiro na cidade e gastar na oficina.

## Multiplayer (2 jogadores)

No menu, **Multiplayer** (ou em **Opções → Ativar multiplayer**):

1. Uma pessoa digita o nome e toca em **Criar sala**. Aparece uma **senha de 4 números** em cima.
2. No outro celular ou computador, a outra pessoa abre o jogo, vai em **Multiplayer**, digita a senha e toca em **Entrar na sala**.
3. Quem criou a sala escolhe **Corrida** (e a pista) ou **Cidade Livre** e toca em **Começar**. Os dois entram juntos.

Na corrida, só vocês dois disputam (sem os rivais do computador). Na Cidade Livre, um vê o outro dirigindo, voando com o Roadster Foguete ou andando a pé. O nome de cada um aparece em cima do carro, e o amigo aparece em laranja no mapa.

Como funciona a conexão:
- **Pelo link do Claude:** a sala usa a sala ao vivo do próprio Claude. A outra pessoa precisa conseguir abrir o link logada no Claude (compartilhe o jogo com ela pelo botão Compartilhar).
- **Pelo arquivo `index.html`:** os dois aparelhos se conectam direto pela internet (PeerJS). Os dois precisam estar com internet. Em algumas redes (por exemplo, certas redes de empresa), a conexão direta pode ser bloqueada.

## Modo carreira: 20 fases

Cada fase tem uma pista diferente, um cenário, uma hora do dia e um clima. Chegar em 1º, 2º ou 3º libera a próxima fase, e toda corrida paga um prêmio em dinheiro. Os rivais ficam mais rápidos a cada fase.

| # | Fase | Cenário | Hora | Clima |
|---|---|---|---|---|
| 1 | Centro da Cidade | Cidade | Noite | Limpo |
| 2 | Orla ao Pôr do Sol | Litoral | Entardecer | Limpo |
| 3 | Deserto Vermelho | Deserto | Dia | Limpo |
| 4 | Serra Enevoada | Serra | Noite | Neblina |
| 5 | Porto Industrial | Porto | Noite | Limpo |
| 6 | Pico Nevado | Neve | Dia | Nevando |
| 7 | Cidade na Chuva | Cidade | Noite | Chuva |
| 8 | Praia ao Luar | Litoral | Noite | Limpo |
| 9 | Cânion ao Entardecer | Deserto | Entardecer | Limpo |
| 10 | Floresta Molhada | Serra | Dia | Chuva |
| 11 | Docas ao Amanhecer | Porto | Amanhecer | Limpo |
| 12 | Nevasca Noturna | Neve | Noite | Nevando |
| 13 | Avenida Neon | Cidade | Noite | Limpo |
| 14 | Litoral na Tempestade | Litoral | Entardecer | Chuva |
| 15 | Deserto sob as Estrelas | Deserto | Noite | Limpo |
| 16 | Montanha Alta | Serra | Entardecer | Neblina |
| 17 | Terminal de Cargas | Porto | Noite | Chuva |
| 18 | Vale Gelado | Neve | Amanhecer | Nevando |
| 19 | Metrópole ao Entardecer | Cidade | Entardecer | Limpo |
| 20 | Grande Final | Cidade | Noite | Chuva |

Na chuva o asfalto fica molhado e reflete as luzes, e o carro escorrega mais. Na neve escorrega ainda mais.

## Oficina: peças do básico ao avançado

Cada peça tem 3 níveis: **Básico** (de fábrica), **Intermediário** e **Avançado**. As peças compradas ficam suas e podem ser trocadas a qualquer momento.

| Peça | O que melhora |
|---|---|
| Motor | Torque e potência |
| Turbo | Força em giro alto (e o assobio do turbo) |
| Câmbio | Trocas de marcha mais rápidas |
| Pneus | Aderência em curvas, largadas e frenagens |
| Freios | Força de frenagem |
| Suspensão | Carro mais baixo, inclina menos e segura mais |
| Aerodinâmica | Mais pressão para baixo e menos arrasto |
| Nitro | Nitro mais forte e que dura mais |
| Redução de peso | Carro mais leve: acelera, freia e vira melhor |

## Roadster Foguete (o carro que voa)

Na garagem (**Visual → Carro**) dá para ganhar de graça o **Roadster Foguete** assistindo a um anúncio de 15 segundos do próprio jogo: um esportivo elétrico, inspirado no carro-foguete do Elon Musk, com arrancada instantânea e som elétrico.

Na **Cidade Livre** ele voa: aperte **V** e as rodas viram propulsores com jatos azuis. **Espaço** sobe, **Q** desce, as setas pilotam, **Shift** dá nitro e **V** de novo pousa. Dá para passar por cima dos prédios e fugir da polícia pelo céu. No celular aparecem os botões VOAR, SUBIR e DESCER. Nas corridas ele anda no chão.

## Blindado X (picape blindada)

Também em **Visual → Carro**, o **Blindado X** é uma picape elétrica de aço, em ângulos retos, inspirada na picape blindada do Elon Musk. Para liberar, é preciso assistir ao anúncio do jogo, mesmo para quem já tem o Roadster Foguete.

- **Muito rápido:** passa de 350 km/h.
- **Antiderrapante:** não sai de traseira nem faz drift, nem com o freio de mão.
- **Gruda na parede:** na corrida, encostar no muro não tira a velocidade; ele desliza junto do muro. Na Cidade Livre, acelere contra um prédio e ele gruda e sobe pela parede.
- **Voa:** na Cidade Livre, aperte **V**, como o Roadster Foguete.

## Touro V12 e Moto Relâmpago

- **🐂 Touro V12:** supercarro baixo e largo, inspirado nas Lamborghini. É o mais rápido do jogo: chega a quase 400 km/h, mais que o Blindado X, com ronco de motor V12.
- **🏍️ Moto Relâmpago:** moto com piloto, leve (260 kg), com arrancada forte e que deita de verdade nas curvas.

Cada um tem o seu próprio anúncio, diferente dos outros, e só é liberado assistindo a esse anúncio até o fim (Visual → Carro).

## Kart, Monstro e Clássico 70

- **🏎️ Kart Faísca:** pequeno (180 kg), com o piloto à mostra e muito ágil nas curvas.
- **🛻 Monstro:** caminhão monstro com rodas gigantes e faróis no teto. Na Cidade Livre, empurra os carros do trânsito para longe.
- **🚘 Clássico 70:** carro dos anos 70 com cromados, faróis redondos, motor V8 de ronco grosso e traseira solta para derrapar.

Cada um tem o seu anúncio e libera em Visual → Carro.

## Rampas e manobras

A Cidade Livre tem rampas listradas nas praças e estacionamentos (uma rampa gigante na praça central). Pegue velocidade e salte:
- **← →** no ar: giro 360.
- **Espaço** no ar: parafuso (gira de lado). Termine o giro antes de cair, senão é "pouso torto".
- Cada salto dá pontos, dinheiro e XP.

## Conquistas e nível

No menu, **Conquistas** mostra 18 medalhas (primeira vitória, 300 km/h, giro 360, fugitivo, colecionador e outras) e o nível do piloto. Cada conquista dá R$ 500 e cada nível novo dá R$ 1.000. O XP vem de corridas, missões, manobras, fugas e moedas.

## Rádio e buzina

- **O:** troca a estação do rádio (Neon FM, Pista FM, Retrô FM ou desligado). As músicas são feitas pelo próprio jogo. Também dá para escolher em Opções.
- **H:** buzina (cada veículo tem a sua). Na cidade, os pedestres pulam para o lado.

## Cinema na Cidade Livre

Na Cidade Livre há um **cinema** com letreiro luminoso e cartazes na fachada (marcador rosa 🎬 no mapa). Chegue a pé ou de carro e aperte **E** para entrar. Lá dentro dá para escolher e assistir no telão aos vídeos: o trailer do Turbo Noturno e os anúncios do Roadster Foguete, Blindado X, Touro V12 e Moto Relâmpago. Dá para sair de um vídeo a qualquer momento.

## Visual: pintura e acessórios

- **Pintura:** 16 cores prontas ou qualquer cor escolhida, com acabamento metálico, fosco, perolado ou cromado.
- **Rodas:** 5 raios, 10 raios, multi-raios ou turbina, nas cores prata, preto, bronze, dourado ou vermelho.
- **Aerofólio:** sem, discreto, rabo de pato ou asa GT.
- **Faixas:** duplas, central larga ou lateral, em qualquer cor.
- **Neon embaixo do carro:** azul, rosa, verde, roxo ou laranja.
- **Vidros:** normal, película escura ou espelhado.
- **Faróis:** branco, xenon azulado ou amarelo.
- **Escapamento:** duplo, quádruplo ou central esportivo.

## Física e gráficos

- Esportivo de tração traseira, com aderência dos pneus, transferência de peso, downforce e câmbio de 6 marchas.
- Ajudas de pilotagem (controle de tração, estabilidade e ABS) que podem ser desligadas.
- Reflexos na pintura, sombras, brilho nas luzes, faróis que iluminam a pista, marcas de pneu e fumaça.
- Som do motor que segue o giro, turbo, cantada de pneu, vento, chuva e batidas.
- Opção de gráficos "Leve" para celular ou computador mais fraco.
