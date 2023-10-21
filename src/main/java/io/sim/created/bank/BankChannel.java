package io.sim.created.bank;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import io.sim.created.JSONConverter;
// import io.sim.EnvSimulator;
import io.sim.created.BankService;

public class BankChannel extends Thread
{
    private Socket socket;
    
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
            DataInputStream entrada = new DataInputStream(socket.getInputStream());
            // System.out.println("BC - passou da entrada.");
            DataOutputStream saida = new DataOutputStream(socket.getOutputStream());
            // System.out.println("BC - passou da saida.");

            String service = "";
            while(!service.equals("Encerrar"))
            {
                BankService bankService = (BankService) JSONConverter.stringToBankService(entrada.readUTF());
                service = bankService.getService();
                // System.out.println("BC recebeu - " + service);
                if(service.equals("Encerrar"))
                {
                    System.out.println("Encerrando canal BC.");
                    break;
                }
                else if(service.equals("Pagamento"))
                {
                    saida.writeUTF(JSONConverter.setJSONboolean(true));
                    // saida.writeUTF(JSONConverter.setJSONboolean(true));
                    // System.out.println("BC - Realizando pagamento.");
                    // BankService bankService = (BankService) JSONConverter.stringToBankService(entrada.readUTF());
                    // System.out.println("BC - Dados recebidos.");
                    AlphaBank.transfer(bankService);
                    // System.out.println("BC - Transacao realizada");
                    AlphaBank.addBankService(bankService);
                }
            }

            // System.out.println("Encerrando canal BC.");
            entrada.close();
            saida.close();
            socket.close();
            // serverSocket.close();
        }
        catch (IOException e)
        {
            System.out.println("Erro em: " + this.getName());
            e.printStackTrace();
        }
    }
}
