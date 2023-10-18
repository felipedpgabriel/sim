package io.sim.created;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import io.sim.DrivingData;

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
            // System.out.println("CC - entrou no try.");
            DataInputStream entrada = new DataInputStream(socket.getInputStream());
            // System.out.println("CC - passou da entrada.");
            DataOutputStream saida = new DataOutputStream(socket.getOutputStream());
            // System.out.println("CC - passou da saida.");
            // JSONObject jsonOut = new JSONObject();

            String mensagem = "";
            // while(true) // loop do sistema #IMP# adicionar logica
            // {
            //     DrivingData ddIn = (DrivingData) JSONConverter.stringToDrivingData(entrada.readUTF());
            //     saida.writeUTF(JSONConverter.routeNtoString(route));
            // }

            System.out.println("Encerrando canal.");
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
