package pt.ipleiria.estg.dei.emergencysts.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import pt.ipleiria.estg.dei.emergencysts.modelo.Pulseira;

public class PulseiraJsonParser {

    public static ArrayList<Pulseira> parserJsonPulseiras(JSONArray response) {
        ArrayList<Pulseira> pulseiras = new ArrayList<>();
        if (response == null) return pulseiras;

        for (int i = 0; i < response.length(); i++) {
            try {
                JSONObject obj = response.getJSONObject(i);
                Pulseira p = parserJsonPulseira(obj);
                if (p != null) {
                    pulseiras.add(p);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return pulseiras;
    }

    public static Pulseira parserJsonPulseira(JSONObject obj) {
        if (obj == null) return null;

        try {
            // Campos Tabela Pulseira
            int id = obj.optInt("id");
            String codigo = obj.optString("codigo", "---");
            String prioridade = obj.optString("prioridade", "Pendente");
            String status = obj.optString("status", "Desconhecido");
            String dataEntrada = obj.optString("tempoentrada", "");
            int userProfileId = obj.optInt("userprofile_id", -1);

            // Dados Tabela UserProfile
            String nome = "Anónimo";
            String sns = "";
            String dataNasc = "";
            String telefone = "";

            if (obj.has("userprofile") && !obj.isNull("userprofile")) {
                JSONObject user = obj.getJSONObject("userprofile");
                nome = user.optString("nome", "Sem Nome");
                sns = user.optString("sns", "---");
                // CORREÇÃO: datanascimento (tudo junto, como no SQL)
                dataNasc = user.optString("datanascimento", "--/--/----");
                telefone = user.optString("telefone", "---");
            }

            //  Dados Tabela Triagem
            JSONObject triagemObj = obj;
            if (obj.has("triagem") && !obj.isNull("triagem")) {
                triagemObj = obj.getJSONObject("triagem");
            }

            //  Nomes exatos da Base de Dados
            String motivo = triagemObj.optString("motivoconsulta", "");
            String queixa = triagemObj.optString("queixaprincipal", "");
            String descricao = triagemObj.optString("descricaosintomas", "");
            String inicio = triagemObj.optString("iniciosintomas", "");
            String dor = triagemObj.optString("intensidadedor", ""); // No SQL é int, mas optString lê na mesma
            String alergias = triagemObj.optString("alergias", "");
            String medicacao = triagemObj.optString("medicacao", "");

            return new Pulseira(id, codigo, prioridade, status, dataEntrada,
                    userProfileId, nome, sns, dataNasc, telefone,
                    motivo, queixa, descricao, inicio, dor, alergias, medicacao);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isConnectionInternet(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnected();
    }
}