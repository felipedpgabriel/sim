package io.sim.company;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import io.sim.simulation.EnvSimulator;
import io.sim.bank.Account;
import io.sim.bank.AlphaBank;
import io.sim.driver.DrivingData;
import io.sim.messages.Cryptography;
import io.sim.messages.JSONconverter;

/**
 * Classe que representa o canal de comunicacao com a empresa (Thread especifica para cada cliente).
 */
public class CompanyChannel extends Thread
{
    // Atributos de servidor
    private Socket socketServ;
    private DataInputStream entradaServ;
    private DataOutputStream saidaServ;
    // Atributos de cliente
    private Socket socketCli;
    // Atributos da classe
    private Account account;
    private boolean rotaFinalizada;

    /**
     * Construtor da classe CompanyChannel.
     * @param _socketCli Socket - Socket para comunicacao com o cliente.
     * @param _socketServ Socket - Socket para comunicacao com o servidor.
     * @param _account Account - Conta da empresa associada a este canal.
     */
    public CompanyChannel(Socket _socketCli, Socket _socketServ, Account _account)
    {
        this.socketCli = _socketCli;
        this.socketServ = _socketServ;
        this.account = _account;
        rotaFinalizada = false;
    }

    @Override
    public void run()
    {
        try
        {
            entradaServ = new DataInputStream(socketServ.getInputStream());
            saidaServ = new DataOutputStream(socketServ.getOutputStream());

            String mensagem = "";
            double previusDistance = 0;
            while(!mensagem.equals("encerrado")) // Loop do sistema
            {
                DrivingData ddIn = (DrivingData) read();
                // Verifica distancia para pagamento
                if(payableDistanceReached(previusDistance, ddIn.getDistance()))
                {
                    // TODO: Verificar se o carro existe antes de prosseguir, senao fechar
                    previusDistance = ddIn.getDistance();
                    AlphaBank.payment(socketCli, account.getLogin(), account.getSenha(), ddIn.getDriverLogin(),
                    EnvSimulator.RUN_PRICE);
                }
                mensagem = ddIn.getCarState(); // Le solicitacao do cliente
                if (mensagem.equals("aguardando"))
                {
                    previusDistance = 0;
                    if(!MobilityCompany.areRoutesAvailable()) // routesToExe.isEmpty()
                    {
                        System.out.println("CC - Sem mais rotas para liberar.");
                        RouteN route = new RouteN("-1", "00000");
                        write(route);
                        break;
                    }
                    if(MobilityCompany.areRoutesAvailable())
                    {
                        RouteN resposta = MobilityCompany.liberarRota();
                        write(resposta);
                        rotaFinalizada = false;
                    }
                }
                else if(mensagem.equals("finalizado"))
                {
                    String routeID = ddIn.getRouteIDSUMO();
                    MobilityCompany.arquivarRota(routeID);
                    System.out.println("Rotas para executar: " + MobilityCompany.getRoutesToExeSize() +"\nRotas em execucao: " 
                    + MobilityCompany.getRoutesInExeSize() + "\nRotas executadas: "+ MobilityCompany.getRoutesExecutedSize());
                    rotaFinalizada = true;
                }
                else if(mensagem.equals("rodando") || mensagem.equals("abastecendo"))
                {
                    MobilityCompany.addRepport(ddIn);
                }
                else if (mensagem.equals("encerrado"))
                {
                    if(!rotaFinalizada)
                    {
                        String routeID = ddIn.getRouteIDSUMO();
                        MobilityCompany.arquivarRota(routeID);
                        System.out.println("Rotas para executar: " + MobilityCompany.getRoutesToExeSize() +"\nRotas em execucao: " 
                        + MobilityCompany.getRoutesInExeSize() + "\nRotas executadas: "+ MobilityCompany.getRoutesExecutedSize());
                        rotaFinalizada = true;
                    }
                    break;
                }
            }

            System.out.println("Encerrando canal CC.");
            entradaServ.close();
            saidaServ.close();
            socketServ.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Verifica se a distancia para pagamento foi atingida.
     * @param _previusDistance double - Distancia anterior.
     * @param _currentDistance double - Distancia atual.
     * @return boolean
     */
    private boolean payableDistanceReached(double _previusDistance, double _currentDistance)
    {
        return (_currentDistance >= (_previusDistance + EnvSimulator.PAYABLE_DISTANCE));
    }

    /**
     * Converte para JSON, criptografa e envia a mensagem.
     * @param _route RouteN - Rota a ser escrita.
     * @throws Exception - Excecao em caso de erro na escrita.
     */
    private void write(RouteN _route) throws Exception
    {
        String jsMsg = JSONconverter.routeNtoString(_route);
        byte[] msgEncrypt = Cryptography.encrypt(jsMsg);
        saidaServ.writeInt(msgEncrypt.length);
        saidaServ.write(msgEncrypt);
    }

    /**
     * Descriptografa, converte de JSON e le a mensagem
     * @return DrivingData - Dados de direcao lidos.
     * @throws Exception - Excecao em caso de erro na leitura.
     */
    private DrivingData read() throws Exception
    {
        int numBytes = entradaServ.readInt();
        byte[] msgEncrypt = entradaServ.readNBytes(numBytes);
        String msgDecrypt = Cryptography.decrypt(msgEncrypt);
        return JSONconverter.stringToDrivingData(msgDecrypt);
    }
}
