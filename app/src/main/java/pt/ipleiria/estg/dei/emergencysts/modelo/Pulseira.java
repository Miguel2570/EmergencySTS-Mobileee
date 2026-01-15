package pt.ipleiria.estg.dei.emergencysts.modelo;

public class Pulseira {
    // Identificação
    private int id;
    private String codigo; // Novo campo P-12345
    private String prioridade;
    private String status;
    private String dataEntrada; // antigo 'hora' ou 'tempoentrada'

    // Dados Paciente
    private int userProfileId;
    private String nomePaciente;
    private String sns;
    private String dataNascimento;
    private String telefone;

    // Dados Triagem (Enfermeiro)
    private String motivo;
    private String queixa;
    private String descricao;
    private String inicioSintomas;
    private String dor;
    private String alergias;
    private String medicacao;

    public Pulseira() {
    }

    // Construtor Completo
    public Pulseira(int id, String codigo, String prioridade, String status, String dataEntrada,
                    int userProfileId, String nomePaciente, String sns, String dataNascimento, String telefone,
                    String motivo, String queixa, String descricao, String inicioSintomas,
                    String dor, String alergias, String medicacao) {
        this.id = id;
        this.codigo = codigo;
        this.prioridade = prioridade;
        this.status = status;
        this.dataEntrada = dataEntrada;
        this.userProfileId = userProfileId;
        this.nomePaciente = nomePaciente;
        this.sns = sns;
        this.dataNascimento = dataNascimento;
        this.telefone = telefone;
        this.motivo = motivo;
        this.queixa = queixa;
        this.descricao = descricao;
        this.inicioSintomas = inicioSintomas;
        this.dor = dor;
        this.alergias = alergias;
        this.medicacao = medicacao;
    }

    // --- GETTERS ---
    public int getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getPrioridade() { return prioridade; }
    public String getStatus() { return status; }
    public String getDataEntrada() { return dataEntrada; }

    public int getUserProfileId() { return userProfileId; }
    public String getNomePaciente() { return nomePaciente; }
    public String getSns() { return sns; }
    public String getDataNascimento() { return dataNascimento; }
    public String getTelefone() { return telefone; }

    public String getMotivo() { return motivo; }
    public String getQueixa() { return queixa; }
    public String getDescricao() { return descricao; }
    public String getInicioSintomas() { return inicioSintomas; }
    public String getDor() { return dor; }
    public String getAlergias() { return alergias; }
    public String getMedicacao() { return medicacao; }

    // SETTERS (ADICIONADOS PARA O MODO OFFLINE FUNCIONAR)

    public void setId(int id) { this.id = id; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public void setPrioridade(String prioridade) { this.prioridade = prioridade; }
    public void setStatus(String status) { this.status = status; }
    public void setDataEntrada(String dataEntrada) { this.dataEntrada = dataEntrada; }

    public void setUserProfileId(int userProfileId) { this.userProfileId = userProfileId; }
    public void setNomePaciente(String nomePaciente) { this.nomePaciente = nomePaciente; }
    public void setSns(String sns) { this.sns = sns; }
    public void setDataNascimento(String dataNascimento) { this.dataNascimento = dataNascimento; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public void setMotivo(String motivo) { this.motivo = motivo; }
    public void setQueixa(String queixa) { this.queixa = queixa; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public void setInicioSintomas(String inicioSintomas) { this.inicioSintomas = inicioSintomas; }
    public void setDor(String dor) { this.dor = dor; }
    public void setAlergias(String alergias) { this.alergias = alergias; }
    public void setMedicacao(String medicacao) { this.medicacao = medicacao; }
}