package io.sim.created;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

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
                service = (String) JSONConverter.getJSONservice(entrada.readUTF());
                if(service.equals("Encerrar"))
                {
                    System.out.println("Pedido de encerramento.");
                    break;
                }
                else if(service.equals("Pagamento"))
                {
                    saida.writeUTF(JSONConverter.setJSONboolean(true));
                    Transaction transaction = (Transaction) JSONConverter.stringToTransaction(entrada.readUTF());
                    AlphaBank.transfer(transaction);
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
