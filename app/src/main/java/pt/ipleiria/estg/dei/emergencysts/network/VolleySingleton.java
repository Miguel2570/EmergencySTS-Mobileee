package pt.ipleiria.estg.dei.emergencysts.network;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import pt.ipleiria.estg.dei.emergencysts.utils.SharedPrefManager;

public class VolleySingleton {

    // --- CONSTANTES DOS ENDPOINTS ---
    // Assim ficam todos num só sítio e evitas erros de escrita nas Activities
    public static final String ENDPOINT_LOGIN = "api/auth/login";
    public static final String ENDPOINT_PACIENTE = "api/paciente"; // Serve para lista e pesquisa
    public static final String ENDPOINT_PACIENTE_PERFIL = "api/paciente/perfil";
    public static final String ENDPOINT_ENFERMEIRO = "api/enfermeiro";
    public static final String ENDPOINT_ENFERMEIRO_PERFIL = "api/enfermeiro/perfil";
    public static final String ENDPOINT_TRIAGEM = "api/triagem";
    public static final String ENDPOINT_PULSEIRA = "api/pulseira";
    public static final String ENDPOINT_TOTAL_USERS = "api/user/total";


    private static VolleySingleton instance;
    private RequestQueue requestQueue;
    private final Context ctx;

    private VolleySingleton(Context context) {
        this.ctx = context;
        this.requestQueue = getRequestQueue();
    }

    public static synchronized VolleySingleton getInstance(Context context) {
        if (instance == null) {
            instance = new VolleySingleton(context.getApplicationContext());
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(ctx);
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
    public String getAPIUrl(String endpoint) {
        String baseUrl = SharedPrefManager.getInstance(ctx).getServerUrl();
        String token = SharedPrefManager.getInstance(ctx).getKeyAccessToken();

        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }

        // Remove barra inicial do endpoint se existir para não ficar duplicada
        if (endpoint.startsWith("/")) {
            endpoint = endpoint.substring(1);
        }

        String fullUrl = baseUrl + endpoint;

        // Se o endpoint for o de Login, não leva auth_key (normalmente)
        // Mas se o teu backend exigir, a lógica abaixo funciona para todos.

        // Verifica se já temos parâmetros na query string (?)
        if (fullUrl.contains("?")) {
            fullUrl += "&auth_key=" + token;
        } else {
            fullUrl += "?auth_key=" + token;
        }

        return fullUrl;
    }
}