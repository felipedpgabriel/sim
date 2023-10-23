package io.sim.bank;

/**
 * Classe que representa uma conta-corrente.
 */
public class Account
{
    private double saldo;
    private String login;
    private String senha;
    private static Thread oWatch;

    /**
     * Construtor da classe Account.
     * @param _saldo double - Saldo inicial da conta.
     * @param _login String - Login da conta.
     * @param _senha String - Senha da conta.
     */
    public Account(double _saldo, String _login, String _senha)
    {
        this.saldo = _saldo;
        this.login = _login;
        this.senha = _senha;
        oWatch = new Thread();
        AlphaBank.addAccount(this);
    }

    /**
     * Get padrao para o atributo login.
     * @return String - Login da conta.
     */
    public String getLogin() {
        return this.login;
    }

    /**
     * Get padrao para o atributo senha.
     * @return String - Senha da conta.
     */
    public String getSenha() {
        return this.senha;
    }

    /**
     * Get padrao para o atributo saldo (synchronized).
     * @return double - Saldo da conta.
     */
    public double getSaldo() {
        synchronized(oWatch) {
            return this.saldo;
        }
    }

    /**
     * Set padrao para o atributo saldo (synchronized).
     * @param _newSaldo double - Novo saldo da conta.
     */
    public void setSaldo(double _newSaldo) {
        synchronized(oWatch) {
            this.saldo = _newSaldo;
        }
    }

    /**
     * Adiciona um valor ao saldo da conta (recebimento).
     * @param _valor double - Valor a ser adicionado ao saldo.
     */
    public void recieve(double _valor)
    {
        this.setSaldo(this.saldo + _valor);
    }

    /**
     * Subtrai um valor do saldo da conta (pagamento).
     * @param _valor double - Valor a ser subtraido do saldo.
     */
    public void pay(double _valor)
    {
        this.setSaldo(this.saldo - _valor);
    }
}
