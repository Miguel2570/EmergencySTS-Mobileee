package pt.ipleiria.estg.dei.emergencysts.utils;

import org.json.JSONObject;
import pt.ipleiria.estg.dei.emergencysts.modelo.Enfermeiro;

public class UserJsonParser {

    public static Enfermeiro parseLogin(JSONObject response) {
        try {
            // Verifica se o user vem dentro de "data" ou na raiz
            JSONObject userJson = response.optJSONObject("user");
            if (userJson == null && response.has("data")) {
                userJson = response.getJSONObject("data").optJSONObject("user");
            }

            if (userJson != null) {
                int id = userJson.optInt("id");
                String username = userJson.optString("username");
                String email = userJson.optString("email");

                // Tenta ler a role. Pode vir como string "role" ou array de roles.
                // Ajusta conforme a tua API Yii2 envia as roles.
                String role = userJson.optString("role");
                if (role.isEmpty()) role = "utente"; // Fallback

                return new Enfermeiro(id, username, email, role);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String parseToken(JSONObject response) {
        // Tenta ler o access_token
        String token = response.optString("access_token");
        if (token.isEmpty() && response.has("data")) {
            try {
                token = response.getJSONObject("data").optString("access_token");
            } catch (Exception e) { e.printStackTrace(); }
        }
        return token;
    }
}