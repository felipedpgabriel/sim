package io.sim.created;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class BotPayment extends Thread
{
    DataOutputStream saida;
    DataInputStream entrada;
    String loginOrigem;
    String senhaOrigem;
    String loginDeestino;
    double valor;

    public BotPayment(DataInputStream _entrada, DataOutputStream saida, String loginOrigem, String senhaOrigem, String loginDeestino,
    double valor)
    {
        this.saida = saida;
        this.entrada = _entrada;
        this.loginOrigem = loginOrigem;
        this.senhaOrigem = senhaOrigem;
        this.loginDeestino = loginDeestino;
        this.valor = valor;
    }

    @Override
    public void run()
    {
        try {
            saida.writeUTF(JSONConverter.setJSONservice("Pagamento"));
            boolean liberado = JSONConverter.getJSONboolean(entrada.readUTF());
            if(liberado) // sinalizacao de pedido recebido
            {
                Transaction transaction = new Transaction(senhaOrigem, loginOrigem, loginDeestino, valor);
                saida.writeUTF(JSONConverter.transactionToString(transaction));
                // boolean transacaoEfetuada = JSONConverter.getJSONboolean(entrada.readUTF());
                // if(transacaoEfetuada)
                // {
                //     System.out.println("");
                // }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
