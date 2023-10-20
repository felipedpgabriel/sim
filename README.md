# Avaliação 1 - GAT 108 - Automação Avançada 
Universidade Federal de Lavras - UFLA | Engenharia de Controle e Automação

Este projeto simula o funcionamento de uma companhia de mobilidade, segundo o diagrama abaixo:

![image](https://github.com/felipedpgabriel/sim/assets/79221267/44d56343-0071-453c-93e5-a3a507036046)

Usa como base o repositório [21lab-technology/sim](https://github.com/21lab-technology/sim).
* Drivers não observam corretamente os estados do carro. Ideias: fazer sistema de cliente e servidor com Drivers e Auto (IMP para o caso do abastecimento), ou ignorar as listas e fazer com flags.
* Buscar sugestões de mudança com TODO
* Falta resolver problema de encerramento: 
java.io.EOFException
        at java.base/java.io.DataInputStream.readUnsignedShort(Unknown Source)
        at java.base/java.io.DataInputStream.readUTF(Unknown Source)
        at java.base/java.io.DataInputStream.readUTF(Unknown Source)
        at io.sim.created.BankChannel.run(BankChannel.java:42)
* Arrumado, falta testar com mais carros. (lembrar de retirar alguns sysout)