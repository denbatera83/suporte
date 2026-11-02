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

## Opções

- **Direção:** Arcade (padrão, mais aderência e mais fácil) ou Simulação (física completa).
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

Na garagem (**Visual → Carro**) dá para escolher de graça o **Roadster Foguete**: um esportivo elétrico, inspirado no carro-foguete do Elon Musk, com arrancada instantânea e som elétrico.

Na **Cidade Livre** ele voa: aperte **V** e as rodas viram propulsores com jatos azuis. **Espaço** sobe, **Q** desce, as setas pilotam, **Shift** dá nitro e **V** de novo pousa. Dá para passar por cima dos prédios e fugir da polícia pelo céu. No celular aparecem os botões VOAR, SUBIR e DESCER. Nas corridas ele anda no chão.

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
