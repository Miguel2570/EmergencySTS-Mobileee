package pt.ipleiria.estg.dei.emergencysts.utils;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import pt.ipleiria.estg.dei.emergencysts.modelo.Paciente;

public class PacienteJsonParser {

    /**
     * Converte um objeto JSON num objeto Paciente
     */
    public static Paciente parserJsonPaciente(JSONObject json) {
        try {
            // Verifica se os dados estão dentro de "userprofile", "data" ou na raiz
            JSONObject u = json.optJSONObject("userprofile");
            if (u == null) u = json.optJSONObject("data"); // Fallback para a estrutura que vimos nos logs
            if (u == null) u = json;

            // Dados base do utilizador
            int id = json.optInt("user_id", json.optInt("id", -1));
            String username = json.optString("username", "---");
            String email = json.optString("email", u.optString("email", "---"));

            // Dados do perfil
            String nome = u.optString("nome", "Desconhecido");
            String dataNascimento = u.optString("datanascimento", "---");
            String genero = u.optString("genero", "M"); // <--- EXTRAIR O GÉNERO
            String telefone = u.optString("telefone", "---");
            String sns = u.optString("sns", "---");
            String nif = u.optString("nif", "---");
            String morada = u.optString("morada", "---");

            // Cria o objeto usando o novo construtor que inclui o género
            return new Paciente(id, username, email, "paciente",
                    nome, dataNascimento, genero,
                    telefone, sns, nif, morada);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Caso a API devolva uma lista de pacientes
     */
    public static ArrayList<Paciente> parserJsonPacientes(JSONArray array) {
        ArrayList<Paciente> lista = new ArrayList<>();
        try {
            for (int i = 0; i < array.length(); i++) {
                Paciente p = parserJsonPaciente(array.getJSONObject(i));
                if (p != null) {
                    lista.add(p);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }
}