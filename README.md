# Avaliação 1 - GAT 108 - Automação Avançada 
Universidade Federal de Lavras - UFLA | Engenharia de Controle e Automação

Este projeto simula o funcionamento de uma companhia de mobilidade, segundo o diagrama abaixo:

![image](https://github.com/felipedpgabriel/sim/assets/79221267/44d56343-0071-453c-93e5-a3a507036046)

Usa como base o repositório [21lab-technology/sim](https://github.com/21lab-technology/sim).
* Arrumar linha 52 de TransportService -> this.sumo.do_timestep() quando tiver mais carros;
* Mecanismo para enviar para todos os carros uma msg que acabaram as rotas, para que eles fechem.
* Drivers não observam corretamente os estados do carro. Ideias: fazer sistema de cliente e servidor com Drivers e Auto (IMP para o caso do abastecimento).