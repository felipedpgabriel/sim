package io.sim.created;

// import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import io.sim.created.messages.Cryptography;
import io.sim.created.messages.JSONconverter;

public class BotPayment extends Thread
{
    private Socket socket;
    private DataOutputStream saida;
    private String loginOrigem;
    private String senhaOrigem;
    private String loginDestino;
    private double valor;

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
            saida = new DataOutputStream(socket.getOutputStream());

            // System.out.println("Criando BotPayment para " + loginDestino);
            BankService bankService = new BankService("Pagamento",senhaOrigem, loginOrigem, valor, loginDestino);
            write(bankService);
            // System.out.println("Encerrando BotPayment");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void write(BankService _bankService) throws Exception
	{
		String jsMsg = JSONconverter.bankServiceToString(_bankService);
		byte[] msgEncrypt = Cryptography.encrypt(jsMsg);
		saida.writeInt(msgEncrypt.length);
		saida.write(msgEncrypt);
	}
}
