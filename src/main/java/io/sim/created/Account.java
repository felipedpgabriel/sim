package io.sim.created;

import io.sim.created.bank.AlphaBank;

public class Account
{
    private double saldo;
    private String login;
    private String senha;
    private static Thread oWatch;

    public Account(double _saldo, String _login, String _senha)
    {
        this.saldo = _saldo;
        this.login = _login;
        this.senha = _senha;
        oWatch = new Thread();
        AlphaBank.addAccount(this);
    }

    public String getLogin() {
        return this.login;
    }

    public String getSenha() {
        return this.senha;
    }

    public double getSaldo()
    {
        synchronized(oWatch)
        {
            return this.saldo;
        }
    }

    public void setSaldo(double _newSaldo)
    {
        synchronized(oWatch)
        {
            this.saldo = _newSaldo;
        }
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
