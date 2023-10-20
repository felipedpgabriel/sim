package io.sim.created;

public class Transaction
{
    private String senha;
    private String origem;    // login
    private String destino;   // login
    private double valor;     // R$
    private long timeStamp;   // nanoseconds    

    public Transaction(String _senha, String _origem, String _destino, double _valor)
    {
        this.origem = _origem;
        this.senha = _senha;
        this.destino = _destino;
        this.valor = _valor;
    }

    public String getSenha() {
        return senha;
    }

    public String getOrigem() {
        return origem;
    }

    public String getDestino() {
        return destino;
    }

    public double getValor() {
        return valor;
    }

    public void setTimeStamp(long _timeStamp)
    {
        this.timeStamp = _timeStamp;
    }
}
