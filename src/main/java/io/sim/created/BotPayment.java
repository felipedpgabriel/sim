package io.sim.created;

// import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class BotPayment extends Thread
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
    public void run()
    {
        try {
            // DataInputStream entrada = new DataInputStream(socket.getInputStream());
            DataOutputStream saida = new DataOutputStream(socket.getOutputStream());

            // System.out.println("Criando BotPayment para " + loginDestino);
            BankService transaction = new BankService("Pagamento",senhaOrigem, loginOrigem, valor, loginDestino);
            saida.writeUTF(JSONConverter.bankServiceToString(transaction));
            // System.out.println("Encerrando BotPayment");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
