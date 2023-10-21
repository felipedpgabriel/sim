package io.sim.created;

public class Account
{
    private double saldo;
    private String login;
    private String senha;
    // private static Thread oWatch;

    public Account(double _saldo, String _login, String _senha)
    {
        this.saldo = _saldo;
        this.login = _login;
        this.senha = _senha;
        // oWatch = new Thread();
        // AlphaBank.addAccount(this);
    }

    public String getLogin() {
        return this.login;
    }

    public String getSenha() {
        return this.senha;
    }

    public synchronized double getSaldo()
    {
        return this.saldo;
    }

    public synchronized void setSaldo(double _newSaldo)
    {
        this.saldo = _newSaldo;
    }

    public void recieve(double _valor)
    {
        this.setSaldo(this.saldo + _valor);
    }

    public void pay(double _valor)
    {
        this.setSaldo(this.saldo - _valor);
    }
}
