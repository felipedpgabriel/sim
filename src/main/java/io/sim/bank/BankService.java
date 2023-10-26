package io.sim.bank;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Classe que representa um servico bancario.
 */
public class BankService implements Serializable
{
    private String service;
    private String senha;
    private String origem;    // login
    private double valor;     // R$
    private String destino;   // login
    private Timestamp timestamp;   // nanoseconds    

    /**
     * Construtor da classe BankService.
     * @param _service String - Tipo de servico.
     * @param _senha String - Senha para autenticacao.
     * @param _origem String - Login de origem para a transacao.
     * @param _valor double - Valor da transacao em R$.
     * @param _destino String - Login de destino para a transacao.
     */
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

    /**
     * Cria um serviço bancario para encerramento.
     * @param _service String - Tipo de servico (Encerrar).
     * @return BankService - Servico bancario para encerramento.
     */
    public static BankService createService(String _service)
    {
        BankService bankService = new BankService(_service, "", "", 0, "");
        return bankService;
    }

    /**
     * Get padrao para o atributo service.
     * @return String - Tipo de servico.
     */
    public String getService() {
        return service;
    }

    /**
     * Get padrao para o atributo senha.
     * @return String - Senha para autenticacao.
     */
    public String getSenha() {
        return senha;
    }

    /**
     * Get padrao para o atributo origem.
     * @return String - Login de origem.
     */
    public String getOrigem() {
        return origem;
    }

    /**
     * Get padrao para o atributo valor.
     * @return double - Valor da transacao [R$].
     */
    public double getValor() {
        return valor;
    }

    /**
     * Get padrao para o atributo destino.
     * @return String - Login de destino.
     */
    public String getDestino() {
        return destino;
    }

    /**
     * Get padrao para o atributo timestamp.
     * @return Timestamp - Carimbo de data/hora.
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * Set padrao para o atributo timestamp.
     * @param _timestamp Timestamp - Carimbo de data/hora.
     */
    public void setTimeStamp(Timestamp _timestamp) {
        this.timestamp = _timestamp;
    }
}
