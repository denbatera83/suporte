# Recursos para motores de jogo

Material para quem quiser levar a ideia do Turbo Noturno para a Unity ou a Unreal.

| Arquivo | O que é |
|---|---|
| [`unity/CarroEsportivo.cs`](unity/CarroEsportivo.cs) | Script C# de física de carro esportivo, sem `WheelCollider`: suspensão por raycast (transferência de peso real na frenagem), pneus com Magic Formula e círculo de atrito (perda de tração traseira e drift), torque por RPM, embreagem, câmbio e freio de mão. Expõe `currentRPM` para o som. |
| [`unreal/guia-som-motor-v8.md`](unreal/guia-som-motor-v8.md) | Guia para modular o som do motor (pitch e volume) com `currentRPM` em MetaSounds/Sound Cue na Unreal, para soar como um V8 real. |

As instruções de montagem estão no começo de cada arquivo.
