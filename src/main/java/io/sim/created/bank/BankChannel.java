package io.sim.created.bank;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

import java.sql.Timestamp;

import io.sim.created.JSONConverter;
import io.sim.created.Account;
import io.sim.created.BankService;

public class BankChannel extends Thread
{
    private Socket socket;
    private Account pagador;
    private Account recebedor;
    private double valor;
    private AlphaBank bank;
    private boolean on;
    
    public BankChannel(Socket _socket, AlphaBank _bank)
    {
        this.socket = _socket;
        this.bank = _bank;
        this.on = true;
    }

    @Override
    public void run()
    {
        try
        {
            DataInputStream entrada = new DataInputStream(socket.getInputStream());

            while(on)
            {
                String recebido = entrada.readUTF();
                BankService bankService = JSONConverter.stringToBankService(recebido);
                if(bankService.getService().equals("Encerrar"))
                {
                    on = false;
                    System.out.println("BC - Pedido de encerramento.");
                    break;
                }
                pagador = bank.searchAccount(bankService.getOrigem());
                recebedor = bank.searchAccount(bankService.getDestino());
                valor =  bankService.getValor();

                String senha = bankService.getSenha();

                if(this.bank.confereConta(pagador.getLogin(), senha))
                {
                    pagamento();
                }
                else
                {
                    System.out.println("senha incorreta!");
                }
            }

            // while(!service.equals("Encerrar"))
            // {
            //     recebido = entrada.readUTF();
            //     BankService bankService = JSONConverter.stringToBankService(recebido);
            //     service = bankService.getService();
            //     System.out.println("BC recebeu - " + service);
            //     if(service.equals("Encerrar"))
            //     {
            //         System.out.println("BC - Pedido de encerramento.");
            //         break;
            //     }
            //     System.out.println("BC - Realizando pagamento.");
            //     AlphaBank.transfer(bankService);
            //     AlphaBank.addBankService(bankService);
            // }

            System.out.println("Encerrando canal BC.");
            entrada.close();
            socket.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public boolean transacao()
    {
        if(pagador.getSaldo() < valor)
        {
            return false;
        }
        else{
            pagador.pay(valor);
            recebedor.recieve(valor);
            return true;
        }
    }

    public void pagamento()
    {
        if(transacao())
        {
            System.out.println();
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            BankService bs = new BankService("Pagamento", pagador.getSenha(), pagador.getLogin(), valor, recebedor.getLogin());

            AlphaBank.addBankService(bs);
        }
        else
        {
            System.out.println("Saldo insuficiente.");
        }
    }

    public void setOn(boolean _on)
    {
        this.on = _on;
    }
}
