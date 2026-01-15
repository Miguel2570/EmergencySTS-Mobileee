package pt.ipleiria.estg.dei.emergencysts.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import pt.ipleiria.estg.dei.emergencysts.modelo.Paciente;
import pt.ipleiria.estg.dei.emergencysts.modelo.Pulseira;
import pt.ipleiria.estg.dei.emergencysts.modelo.Triagem;

public class TriagemJsonParser {

    public static ArrayList<Triagem> parserJsonTriagens(JSONArray response) {
        ArrayList<Triagem> lista = new ArrayList<>();

        if (response == null) return lista;

        try {
            for (int i = 0; i < response.length(); i++) {
                JSONObject obj = response.optJSONObject(i);
                if (obj != null) {
                    lista.add(parserJsonTriagem(obj));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    public static Triagem parserJsonTriagem(JSONObject json) {

        Triagem t = new Triagem();

        //  DADOS BÁSICOS (Usar Setters)
        t.setId(json.optInt("id"));
        t.setMotivoconsulta(safe(json, "motivoconsulta", "-"));
        t.setQueixaprincipal(safe(json, "queixaprincipal", "-"));
        t.setDescricaosintomas(safe(json, "descricaosintomas", "-"));
        t.setIniciosintomas(safe(json, "iniciosintomas", "-"));
        t.setAlergias(safe(json, "alergias", "-"));
        t.setMedicacao(safe(json, "medicacao", "-"));
        t.setDatatriagem(safe(json, "datatriagem", ""));

        // Converter intensidade de dor (pode vir como int ou string do JSON)
        t.setIntensidadedor(json.optInt("intensidadedor", 0));

        //  PACIENTE
        // O JSON traz "userprofile", mas nós guardamos no objeto "Paciente"
        JSONObject up = json.optJSONObject("userprofile");
        Paciente paciente = new Paciente();

        if (up != null) {
            paciente.setNome(safe(up, "nome", "Sem nome"));
            paciente.setEmail(safe(up, "email", "-"));
            paciente.setSns(safe(up, "sns", "---"));
            paciente.setTelefone(safe(up, "telefone", ""));
        } else {
            paciente.setNome("Sem nome");
            paciente.setSns("---");
        }
        t.setPaciente(paciente);

        //  PULSEIRA
        JSONObject p = json.optJSONObject("pulseira");
        Pulseira pulseira = new Pulseira();

        if (p != null) {
            pulseira.setId(p.optInt("id"));
            pulseira.setCodigo(safe(p, "codigo", "-"));
            pulseira.setPrioridade(safe(p, "prioridade", "Pendente"));
            pulseira.setStatus(safe(p, "status", "Concluída"));
            pulseira.setDataEntrada(safe(p, "tempoentrada", ""));
        } else {
            // Valores por defeito se não vier pulseira
            pulseira.setCodigo("-");
            pulseira.setPrioridade("Pendente");
            pulseira.setStatus("Concluída");
        }

        //  associar a pulseira à triagem
        t.setPulseira(pulseira);

        return t;
    }

    private static String safe(JSONObject json, String key, String defaultValue) {
        if (json == null) return defaultValue;

        String value = json.optString(key, defaultValue);
        if (value == null || value.trim().isEmpty() || value.equalsIgnoreCase("null"))
            return defaultValue;

        return value;
    }
}