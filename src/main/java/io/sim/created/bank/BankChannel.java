package io.sim.created.bank;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import io.sim.created.JSONConverter;
import io.sim.created.Transaction;

public class BankChannel extends Thread
{
    private Socket socket;
    
    public BankChannel(Socket _socket) {
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
                System.out.println("BC - Aguardando mensagem.");
                service = (String) JSONConverter.getJSONservice(entrada.readUTF());
                System.out.println("BC ouviu - " + service);
                if(service.equals("Encerrar"))
                {
                    System.out.println("BC - Pedido de encerramento.");
                    break;
                }
                else if(service.equals("Pagamento"))
                {
                    saida.writeUTF(JSONConverter.setJSONboolean(true));
                    System.out.println("BC - Iniciando pagamento.");
                    Transaction transaction = (Transaction) JSONConverter.stringToTransaction(entrada.readUTF());
                    System.out.println("BC - Dados recebidos.");
                    AlphaBank.transfer(transaction);
                    System.out.println("BC - Transacao realizada");
                    transaction.setTimeStamp(System.nanoTime());
                    AlphaBank.addTransaction(transaction);
                }
            }

            System.out.println("Encerrando canal BC.");
            entrada.close();
            saida.close();
            socket.close();
            // serverSocket.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
