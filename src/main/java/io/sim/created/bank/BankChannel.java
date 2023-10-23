package io.sim.created.bank;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import io.sim.created.BankService;
import io.sim.created.messages.Cryptography;
import io.sim.created.messages.JSONconverter;

public class BankChannel extends Thread
{
    private Socket socket;
    private DataInputStream entrada;
    
    public BankChannel(String nome,Socket _socket) {
        super(nome);
        this.socket = _socket;
    }

    public void run()
    {
        try
        {
            // variaveis de entrada e saida do servidor
            // System.out.println("BC - entrou no try.");
            entrada = new DataInputStream(socket.getInputStream());
            // System.out.println("BC - passou da entrada.");
            DataOutputStream saida = new DataOutputStream(socket.getOutputStream());
            // System.out.println("BC - passou da saida.");

            String service = "";
            while(!service.equals("Encerrar"))
            {
                BankService bankService = (BankService) read();
                service = bankService.getService();
                // System.out.println("BC recebeu - " + service);
                if(service.equals("Encerrar"))
                {
                    System.out.println("Encerrando canal BC.");
                    break;
                }
                else if(service.equals("Pagamento"))
                {
                    AlphaBank.transfer(bankService);
                    AlphaBank.addBankService(bankService);
                }
            }

            // System.out.println("Encerrando canal BC.");
            entrada.close();
            saida.close();
            socket.close();
            // serverSocket.close();
        }
        catch (Exception e)
        {
            System.out.println("Erro em: " + this.getName());
            e.printStackTrace();
        }
    }

	private BankService read() throws Exception
	{
		int numBytes = entrada.readInt();
		byte[] msgEncrypt = entrada.readNBytes(numBytes);
		String msgDecrypt = Cryptography.decrypt(msgEncrypt);
		return JSONconverter.stringToBankService(msgDecrypt);
	}
}
