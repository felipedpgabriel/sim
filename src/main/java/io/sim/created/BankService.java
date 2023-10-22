package io.sim.created;

import java.io.Serializable;
import java.sql.Timestamp;

public class BankService implements Serializable
{
    private String service;
    private String senha;
    private String origem;    // login
    private double valor;     // R$
    private String destino;   // login
    private Timestamp timestamp;   // nanoseconds    

    public BankService(String _service, String _senha, String _origem, double _valor, String _destino)
    {
        this.service = _service;
        this.senha = _senha;
        // Informacoes para transacoes
        this.origem = _origem;
        this.valor = _valor;
        this.destino = _destino;
        timestamp = new Timestamp(0);
    }

    public String getService() {
        return service;
    }

    public String getSenha() {
        return senha;
    }

    public String getOrigem() {
        return origem;
    }

    public double getValor() {
        return valor;
    }

    public String getDestino() {
        return destino;
    }

    public void setTimeStamp(Timestamp _timestamp)
    {
        this.timestamp = _timestamp;
    }

    public static BankService createService(String _service) // Para encerramento
    {
        BankService bankService = new BankService(_service, "", "", 0, "");
        return bankService;
    }
}
