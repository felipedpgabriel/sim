package io.sim.bank;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import io.sim.messages.Cryptography;
import io.sim.messages.JSONconverter;

/**
 * Classe que representa um bot de pagamento para transferencias bancarias.
 */
public class BotPayment extends Thread
{
    private Socket socket;
    private DataOutputStream saida;
    private String loginOrigem;
    private String senhaOrigem;
    private String loginDestino;
    private double valor;

    /**
     * Construtor da classe BotPayment.
     * @param _socket Socket - Canal de comunicacao com o cliente.
     * @param loginOrigem String - Login da conta pagadora.
     * @param senhaOrigem String - Senha da conta pagadora.
     * @param loginDestino String - Login da conta receptora.
     * @param valor double - Valor da transferencia em R$.
     */
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
        try
        {
            saida = new DataOutputStream(socket.getOutputStream());

            // Cria um objeto BankService para representar a transacao de pagamento
            BankService bankService = new BankService("Pagamento", senhaOrigem, loginOrigem, valor, loginDestino);
            write(bankService);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Converte para JSON e criptografa a mensagem para envio.
     * @param _bankService BankService - Objeto representando a transacao bancaria.
     * @throws Exception - Excecao em caso de erro na escrita ou criptografia.
     */
    private void write(BankService _bankService) throws Exception
    {
        String jsMsg = JSONconverter.bankServiceToString(_bankService);
        byte[] msgEncrypt = Cryptography.encrypt(jsMsg);
        saida.writeInt(msgEncrypt.length);
        saida.write(msgEncrypt);
    }
}
