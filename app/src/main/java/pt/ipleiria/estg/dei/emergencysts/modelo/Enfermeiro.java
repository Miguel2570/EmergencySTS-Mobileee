package pt.ipleiria.estg.dei.emergencysts.modelo;

import java.util.Calendar;

public class Enfermeiro {

    private int userId;
    private String username;
    private String email;
    private String role;

    private String nome;
    private String dataNascimento; // formato YYYY-MM-DD
    private String telefone;
    private String sns;
    private String nif;
    private String morada;


    public Enfermeiro(int userId, String username, String email, String role) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role;
    }

    public Enfermeiro(int userId, String username, String email, String role,
                      String nome, String dataNascimento,
                      String telefone, String sns, String nif, String morada) {

        this(userId, username, email, role);

        this.nome = nome;
        this.dataNascimento = dataNascimento;
        this.telefone = telefone;
        this.sns = sns;
        this.nif = nif;
        this.morada = morada;
    }

    public int getId() { return userId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getRole() { return role; }

    public String getNome() { return nome; }
    public String getDataNascimento() { return dataNascimento; }
    public String getTelefone() { return telefone; }
    public String getSns() { return sns; }
    public String getNif() { return nif; }
    public String getMorada() { return morada; }


    public String getIdadeFormatada() {

        try {
            if (dataNascimento == null || dataNascimento.trim().isEmpty())
                return "-- anos";

            String[] parts = dataNascimento.split("-");
            if (parts.length != 3)
                return "-- anos";

            int ano = Integer.parseInt(parts[0]);
            int mes = Integer.parseInt(parts[1]);
            int dia = Integer.parseInt(parts[2]);

            Calendar hoje = Calendar.getInstance();
            int anoAtual = hoje.get(Calendar.YEAR);
            int mesAtual = hoje.get(Calendar.MONTH) + 1;
            int diaAtual = hoje.get(Calendar.DAY_OF_MONTH);

            int idade = anoAtual - ano;

            if (mesAtual < mes || (mesAtual == mes && diaAtual < dia)) {
                idade--;
            }

            return idade + " anos";

        } catch (Exception e) {
            return "-- anos";
        }
    }

    public void setNome(String nome) { this.nome = nome; }
    public void setDataNascimento(String dataNascimento) { this.dataNascimento = dataNascimento; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public void setSns(String sns) { this.sns = sns; }
    public void setNif(String nif) { this.nif = nif; }
    public void setEmail(String email) { this.email = email; }
    public void setMorada(String morada) { this.morada = morada; }
}
