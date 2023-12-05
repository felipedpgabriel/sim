package io.sim.bank;

import java.io.DataInputStream;
import java.net.Socket;

import io.sim.messages.Cryptography;
import io.sim.messages.JSONconverter;
import io.sim.repport.ExcelRepport;

/**
 * Classe que representa o canal de comunicacao com o banco (Thread especifica apra cada cliente).
 */
public class BankChannel extends Thread
{
    private Socket socket;
    private DataInputStream entrada;

    // Escalonamento
    private long initRunTime;
    private long endRunTime;
    private long birthTime;
    
    /**
     * Construtor da classe BankChannel.
     * @param nome String - Nome do canal.
     * @param _socket Socket - Socket para comunicacao.
     */
    public BankChannel(String nome, Socket _socket)
    {
        super(nome);
        this.birthTime = System.nanoTime();
        this.socket = _socket;
    }

    @Override
    public void run()
    {
        this.initRunTime = System.nanoTime();
        try
        {
            // Variavel de saida do servidor
            entrada = new DataInputStream(socket.getInputStream());

            String service = "";
            while(!service.equals("Encerrar"))
            {
                BankService bankService = (BankService) read();
                service = bankService.getService();
                
                // Thread cliente encerrada
                if(service.equals("Encerrar"))
                {
                    System.out.println("Encerrando canal BC.");
                    break;
                } // Operacao de pagamento
                else if(service.equals("Pagamento"))
                {
                    AlphaBank.transfer(bankService);
                    AlphaBank.addBankService(bankService);
                }
            }

            entrada.close();
            socket.close();
            this.endRunTime = System.nanoTime();
            ExcelRepport.updateSSScheduling("BankChannel", this.initRunTime, this.endRunTime, this.birthTime);
        }
        catch (Exception e)
        {
            System.out.println("Erro em: " + this.getName());
            e.printStackTrace();
        }
    }

    /**
     * Descriptografa, converte de JSON e le a mensagem recebida.
     * @return BankService - Servico bancario recebido.
     * @throws Exception - Excecao em caso de erro na leitura ou descriptografia.
     */
    private BankService read() throws Exception
    {
        int numBytes = entrada.readInt();
        byte[] msgEncrypt = entrada.readNBytes(numBytes);
        String msgDecrypt = Cryptography.decrypt(msgEncrypt);
        return JSONconverter.stringToBankService(msgDecrypt);
    }
}
