package pt.ipleiria.estg.dei.emergencysts.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import pt.ipleiria.estg.dei.emergencysts.utils.SharedPrefManager;

public class VolleySingleton {

    // --- CONSTANTES DOS ENDPOINTS ---
    public static final String ENDPOINT_LOGIN = "api/auth/login";
    public static final String ENDPOINT_PACIENTE = "api/paciente";
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
        this.ctx = context.getApplicationContext(); // Evita memory leaks
        this.requestQueue = getRequestQueue();
    }

    public static synchronized VolleySingleton getInstance(Context context) {
        if (instance == null) {
            instance = new VolleySingleton(context);
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

    /**
     * Verifica se há ligação à internet antes de fazer pedidos
     */
    public boolean isInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    /**
     * Helper para pedidos GET que retornam JSON (usado na maioria das tuas listas)
     */
    public void apiGetJSON(String endpoint, final Response.Listener<JSONObject> onSuccess, final Response.ErrorListener onError) {
        if (!isInternetConnection()) {
            Toast.makeText(ctx, "Sem ligação à Internet", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = getAPIUrl(endpoint);

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null, onSuccess, onError);
        addToRequestQueue(req);
    }

    /**
     * Helper para pedidos POST/PUT genéricos (ex: Login, Apagar)
     * Permite passar parâmetros no corpo (Body)
     */
    public void apiRequest(int method, String endpoint, final Map<String, String> params, final Response.Listener<String> onSuccess, final Response.ErrorListener onError) {
        if (!isInternetConnection()) {
            Toast.makeText(ctx, "Sem ligação à Internet", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = getAPIUrl(endpoint);

        StringRequest req = new StringRequest(method, url, onSuccess, onError) {
            @Override
            protected Map<String, String> getParams() {
                // Se não houver parâmetros, devolve um mapa vazio
                return params != null ? params : new HashMap<>();
            }

            @Override
            public String getBodyContentType() {
                // Garante compatibilidade com formulários PHP/Yii2
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }
        };
        addToRequestQueue(req);
    }

    public String getAPIUrl(String endpoint) {
        String baseUrl = SharedPrefManager.getInstance(ctx).getServerUrl();
        String token = SharedPrefManager.getInstance(ctx).getKeyAccessToken();

        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }

        if (endpoint.startsWith("/")) {
            endpoint = endpoint.substring(1);
        }

        String fullUrl = baseUrl + endpoint;

        // Adiciona token automaticamente se não for Login
        if (!endpoint.contains("login")) {
            if (fullUrl.contains("?")) {
                fullUrl += "&auth_key=" + token;
            } else {
                fullUrl += "?auth_key=" + token;
            }
        }

        return fullUrl;
    }
}