package pt.ipleiria.estg.dei.emergencysts.modelo;

public class Triagem {

    // CAMPOS PRINCIPAIS
    public int id;
    public String motivoconsulta;
    public String queixaprincipal;
    public String descricaosintomas;
    public String iniciosintomas;
    public int intensidadedor;
    public String alergias;
    public String medicacao;
    public String datatriagem;

    // OBJETOS RELACIONADOS
    public Pulseira pulseira;
    public Paciente paciente;

    public Triagem() {
    }

    // GETTERS (Usados pelo HistoricoActivity)
    public int getId() { return id; }
    public String getMotivoconsulta() { return motivoconsulta; }
    public String getQueixaprincipal() { return queixaprincipal; }
    public String getDescricaosintomas() { return descricaosintomas; }
    public String getIniciosintomas() { return iniciosintomas; }
    public int getIntensidadedor() { return intensidadedor; }
    public String getAlergias() { return alergias; }
    public String getMedicacao() { return medicacao; }
    public String getDataTriagem() { return datatriagem; }

    public Pulseira getPulseira() { return pulseira; }
    public Paciente getPaciente() { return paciente; }

    //  SETTERS (Necessários para carregar do SQLite/Offline)
    public void setId(int id) { this.id = id; }
    public void setMotivoconsulta(String motivoconsulta) { this.motivoconsulta = motivoconsulta; }
    public void setQueixaprincipal(String queixaprincipal) { this.queixaprincipal = queixaprincipal; }
    public void setDescricaosintomas(String descricaosintomas) { this.descricaosintomas = descricaosintomas; }
    public void setIniciosintomas(String iniciosintomas) { this.iniciosintomas = iniciosintomas; }
    public void setIntensidadedor(int intensidadedor) { this.intensidadedor = intensidadedor; }
    public void setAlergias(String alergias) { this.alergias = alergias; }
    public void setMedicacao(String medicacao) { this.medicacao = medicacao; }
    public void setDatatriagem(String datatriagem) { this.datatriagem = datatriagem; }

    public void setPulseira(Pulseira pulseira) { this.pulseira = pulseira; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }


    //  MÉTODOS AUXILIARES
    public String getNomePaciente() {
        if (paciente != null && paciente.getNome() != null) {
            return paciente.getNome();
        }
        return "Anónimo";
    }

    public String getSnsPaciente() {
        if (paciente != null && paciente.getSns() != null) {
            return paciente.getSns();
        }
        return "---";
    }
}