package io.sim.created;

// import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import io.sim.EnvSimulator;

public class BotPayment extends Thread // TODO problema de Socket
{
    Socket socket;
    String loginOrigem;
    String senhaOrigem;
    String loginDestino;
    double valor;

    public BotPayment(Socket _socket, String loginOrigem, String senhaOrigem, String loginDestino,
    double valor)
    {
        this.socket = _socket;
        this.loginOrigem = loginOrigem;
        this.senhaOrigem = senhaOrigem;
        this.loginDestino = loginDestino;
        this.valor = valor;
    }

    @Override
    public void run() // TODO B.O. simplificar logica
    {
        try {
            // DataInputStream entrada = new DataInputStream(socket.getInputStream());]
			// System.out.println("CC - passou da entradaCli.");
            DataOutputStream saida = new DataOutputStream(socket.getOutputStream());

            System.out.println("Criando BotPayment para " + loginDestino);
            BankService transaction = new BankService("Pagamento",senhaOrigem, loginOrigem, valor, loginDestino);
            String enviado = JSONConverter.bankServiceToString(transaction);
            saida.writeUTF(enviado);
            System.out.println("Encerrando BotPayment");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
