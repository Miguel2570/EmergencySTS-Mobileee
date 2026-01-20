package pt.ipleiria.estg.dei.emergencysts.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import pt.ipleiria.estg.dei.emergencysts.modelo.Enfermeiro;
import pt.ipleiria.estg.dei.emergencysts.modelo.Paciente;
import pt.ipleiria.estg.dei.emergencysts.modelo.Pulseira;
import pt.ipleiria.estg.dei.emergencysts.modelo.Triagem;

// Parsers
import pt.ipleiria.estg.dei.emergencysts.utils.PacienteJsonParser;
import pt.ipleiria.estg.dei.emergencysts.utils.PulseiraBDHelper;
import pt.ipleiria.estg.dei.emergencysts.utils.SharedPrefManager;
import pt.ipleiria.estg.dei.emergencysts.utils.TriagemJsonParser;

// Listeners
import pt.ipleiria.estg.dei.emergencysts.listeners.LoginListener;
import pt.ipleiria.estg.dei.emergencysts.listeners.PacienteListener;
import pt.ipleiria.estg.dei.emergencysts.listeners.TriagemListener;

public class VolleySingleton {

    // --- ENDPOINTS DA API ---
    public static final String ENDPOINT_LOGIN = "api/auth/login";
    public static final String ENDPOINT_PACIENTE = "api/paciente";
    public static final String ENDPOINT_PACIENTE_PERFIL = "api/paciente/view-profile";
    public static final String ENDPOINT_ENFERMEIRO = "api/enfermeiro";
    public static final String ENDPOINT_ENFERMEIRO_PERFIL = "api/enfermeiro/view-profile";
    public static final String ENDPOINT_TRIAGEM = "api/triagem";
    public static final String ENDPOINT_PULSEIRA = "api/pulseira";
    public static final String ENDPOINT_TOTAL_USERS = "api/users/total";

    private static VolleySingleton instance;
    private RequestQueue requestQueue;
    private final Context ctx;

    private LoginListener loginListener;
    private PacienteListener pacienteListener;
    private TriagemListener triagemListener;

    private PulseiraBDHelper dbHelper;

    private VolleySingleton(Context context) {
        this.ctx = context.getApplicationContext();
        this.requestQueue = getRequestQueue();
        this.dbHelper = PulseiraBDHelper.getInstance(ctx);
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

    // Setters dos Listeners
    public void setLoginListener(LoginListener listener) { this.loginListener = listener; }
    public void setPacienteListener(PacienteListener listener) { this.pacienteListener = listener; }
    public void setTriagemListener(TriagemListener listener) { this.triagemListener = listener; }

    // --------------------------------------------------------------------------------
    // 1. LOGIN API (ATUALIZADO PARA PASSAR A ROLE)
    // --------------------------------------------------------------------------------
    public void loginAPI(final String username, final String password) {
        if (!isInternetConnection()) {
            Toast.makeText(ctx, "Sem ligação à Internet", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = getAPIUrl(ENDPOINT_LOGIN);

        StringRequest req = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        System.out.println(">>> LOGIN RESPONSE: " + response);
                        JSONObject json = new JSONObject(response);
                        String token = "";

                        // 1. EXTRAIR TOKEN
                        if (!json.isNull("auth_key")) token = json.optString("auth_key");
                        if (token.isEmpty() && !json.isNull("access_token")) token = json.optString("access_token");

                        if (token.isEmpty() && json.has("data")) {
                            JSONObject data = json.optJSONObject("data");
                            if (data != null && !data.isNull("auth_key")) token = data.optString("auth_key");
                        }

                        if (!token.isEmpty()) {
                            // 2. EXTRAIR DADOS BÁSICOS
                            int id = -1;
                            String role = "Enfermeiro"; // Default
                            String email = "";

                            id = json.optInt("id", json.optInt("user_id", -1));
                            role = json.optString("role", role);
                            email = json.optString("email", "");

                            if (json.has("data")) {
                                JSONObject data = json.optJSONObject("data");
                                if (data != null) {
                                    if(data.has("id")) id = data.optInt("id");
                                    if(data.has("role")) role = data.optString("role");
                                    if(data.has("email")) email = data.optString("email");
                                }
                            }

                            // 3. GUARDAR TUDO
                            Enfermeiro userBase = new Enfermeiro(id, username, email, role);
                            SharedPrefManager.getInstance(ctx).userLogin(userBase, token);

                            // -----------------------------------------------------------
                            // MUDANÇA PRINCIPAL: Passamos 'role' como 3º argumento
                            // -----------------------------------------------------------
                            if (loginListener != null) {
                                loginListener.onValidateLogin(token, username, role);
                            }

                            // 4. PEDIR O PERFIL EM "BACKGROUND"
                            if (role.equalsIgnoreCase("paciente") || role.equalsIgnoreCase("utente")) {
                                getPacientePerfilAPI(token, username);
                            } else {
                                getEnfermeiroPerfilAPI(token, username);
                            }

                        } else {
                            String errorMsg = json.optString("message", "Token não recebido.");
                            if (loginListener != null) loginListener.onLoginError(errorMsg);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        if (loginListener != null) loginListener.onLoginError("Erro JSON: " + e.getMessage());
                    }
                },
                error -> {
                    if (loginListener != null) {
                        String msg = "Erro de conexão";
                        if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                            msg = "Dados incorretos";
                        }
                        loginListener.onLoginError(msg);
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("password", password);
                params.put("LoginForm[username]", username);
                params.put("LoginForm[password]", password);
                return params;
            }
        };
        addToRequestQueue(req);
    }

    // --------------------------------------------------------------------------------
    // 2. MÉTODOS DE PERFIL (Também atualizados para passar a role)
    // --------------------------------------------------------------------------------

    private void getEnfermeiroPerfilAPI(String token, String username) {
        String url = getAPIUrl(ENDPOINT_ENFERMEIRO_PERFIL);

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        Enfermeiro enf = SharedPrefManager.getInstance(ctx).getEnfermeiroBase();

                        enf.setNome(response.optString("nome", username));
                        enf.setNif(response.optString("nif", ""));
                        enf.setTelefone(response.optString("telefone", ""));
                        enf.setSns(response.optString("num_sns", ""));
                        enf.setMorada(response.optString("morada", ""));
                        enf.setDataNascimento(response.optString("data_nasc", ""));

                        SharedPrefManager.getInstance(ctx).saveEnfermeiro(enf);

                        // Atualização aqui também (passamos a role do objeto)
                        if (loginListener != null) loginListener.onValidateLogin(token, username, enf.getRole());

                    } catch (Exception e) {
                        // Se der erro, tentamos passar a role que temos guardada
                        String roleGuardada = SharedPrefManager.getInstance(ctx).getEnfermeiroBase().getRole();
                        if (loginListener != null) loginListener.onValidateLogin(token, username, roleGuardada);
                    }
                },
                error -> {
                    String roleGuardada = SharedPrefManager.getInstance(ctx).getEnfermeiroBase().getRole();
                    if (loginListener != null) loginListener.onValidateLogin(token, username, roleGuardada);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        addToRequestQueue(req);
    }

    private void getPacientePerfilAPI(String token, String username) {
        String url = getAPIUrl(ENDPOINT_PACIENTE_PERFIL);

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        Paciente pac = SharedPrefManager.getInstance(ctx).getPacienteBase();

                        pac.setNome(response.optString("nome", username));
                        pac.setNif(response.optString("nif", ""));
                        pac.setTelefone(response.optString("telefone", ""));
                        pac.setSns(response.optString("num_sns", ""));
                        pac.setMorada(response.optString("morada", ""));
                        pac.setDataNascimento(response.optString("data_nasc", ""));
                        pac.setGenero(response.optString("genero", ""));

                        SharedPrefManager.getInstance(ctx).savePaciente(pac);

                        // Atualização aqui também
                        if (loginListener != null) loginListener.onValidateLogin(token, username, pac.getRole());

                    } catch (Exception e) {
                        String roleGuardada = SharedPrefManager.getInstance(ctx).getPacienteBase().getRole();
                        if (loginListener != null) loginListener.onValidateLogin(token, username, roleGuardada);
                    }
                },
                error -> {
                    String roleGuardada = SharedPrefManager.getInstance(ctx).getPacienteBase().getRole();
                    if (loginListener != null) loginListener.onValidateLogin(token, username, roleGuardada);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        addToRequestQueue(req);
    }

    // --------------------------------------------------------------------------------
    // 3. API LISTA DE PACIENTES
    // --------------------------------------------------------------------------------
    public void getAllPacientesAPI() {
        if (!isInternetConnection()) {
            Toast.makeText(ctx, "Sem net", Toast.LENGTH_SHORT).show();
            return;
        }
        String url = getAPIUrl(ENDPOINT_PACIENTE);

        JsonArrayRequest req = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        ArrayList<Paciente> lista = PacienteJsonParser.parserJsonPacientes(response);
                        if (pacienteListener != null) pacienteListener.onPacientesLoaded(lista);
                    } catch (Exception e) {
                        if (pacienteListener != null) pacienteListener.onPacienteError("Erro JSON Pacientes");
                    }
                },
                error -> {
                    if (pacienteListener != null) pacienteListener.onPacienteError("Erro Rede Pacientes");
                }
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String token = SharedPrefManager.getInstance(ctx).getKeyAccessToken();
                if(token != null) headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        addToRequestQueue(req);
    }

    // --------------------------------------------------------------------------------
    // 4. API TRIAGENS / HISTÓRICO
    // --------------------------------------------------------------------------------
    public void getHistoricoTriagensAPI(boolean isPaciente) {

        if (!isInternetConnection()) {
            Toast.makeText(ctx, "Modo Offline: Histórico guardado.", Toast.LENGTH_LONG).show();
            ArrayList<Pulseira> locais = dbHelper.getAllPulseiras();
            if (triagemListener != null) {
                triagemListener.onTriagensLoaded(converterPulseirasParaTriagens(locais));
            }
            return;
        }

        String url = getAPIUrl(ENDPOINT_TRIAGEM + "?expand=paciente,pulseira,userprofile");

        JsonArrayRequest req = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        ArrayList<Triagem> todasTriagens = TriagemJsonParser.parserJsonTriagens(response);
                        ArrayList<Triagem> listaFiltrada = new ArrayList<>();
                        ArrayList<Pulseira> pulseirasParaBD = new ArrayList<>();

                        for (Triagem t : todasTriagens) {
                            String prioridade = (t.getPulseira() != null && t.getPulseira().getPrioridade() != null) ? t.getPulseira().getPrioridade() : "Pendente";
                            String status = (t.getPulseira() != null && t.getPulseira().getStatus() != null) ? t.getPulseira().getStatus() : "Desconhecido";

                            if (prioridade.equalsIgnoreCase("Pendente")) continue;

                            if (!isPaciente) {
                                String s = status.trim();
                                if (s.equalsIgnoreCase("Finalizado") || s.equalsIgnoreCase("Concluída")) {
                                    continue;
                                }
                            }

                            listaFiltrada.add(t);

                            if (t.getPulseira() != null) {
                                Pulseira p = t.getPulseira();
                                p.setMotivo(t.getMotivoconsulta());
                                p.setQueixa(t.getQueixaprincipal());
                                p.setDescricao(t.getDescricaosintomas());
                                p.setInicioSintomas(t.getIniciosintomas());
                                p.setDor(String.valueOf(t.getIntensidadedor()));
                                p.setAlergias(t.getAlergias());
                                p.setMedicacao(t.getMedicacao());

                                if (t.getPaciente() != null) {
                                    p.setNomePaciente(t.getPaciente().getNome());
                                    p.setSns(t.getPaciente().getSns());
                                    p.setTelefone(t.getPaciente().getTelefone());
                                }
                                pulseirasParaBD.add(p);
                            }
                        }

                        dbHelper.sincronizarPulseiras(pulseirasParaBD);

                        if (triagemListener != null) {
                            triagemListener.onTriagensLoaded(listaFiltrada);
                        }

                    } catch (Exception e) {
                        if (triagemListener != null) triagemListener.onTriagemError("Erro processar Triagens");
                    }
                },
                error -> {
                    ArrayList<Pulseira> locais = dbHelper.getAllPulseiras();
                    ArrayList<Triagem> triagensOffline = converterPulseirasParaTriagens(locais);
                    if (triagemListener != null) triagemListener.onTriagensLoaded(triagensOffline);
                    Toast.makeText(ctx, "Erro Rede. A mostrar dados antigos.", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String token = SharedPrefManager.getInstance(ctx).getKeyAccessToken();
                if(token != null) headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        addToRequestQueue(req);
    }

    // --------------------------------------------------------------------------------
    // 5. HELPER GENÉRICO
    // --------------------------------------------------------------------------------
    public void apiRequest(int method, String endpoint, final Map<String, String> params, final Response.Listener<String> onSuccess, final Response.ErrorListener onError) {
        if (!isInternetConnection()) {
            Toast.makeText(ctx, "Sem ligação à Internet", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = endpoint.startsWith("http") ? endpoint : getAPIUrl(endpoint);

        StringRequest req = new StringRequest(method, url, onSuccess, onError) {
            @Override
            protected Map<String, String> getParams() {
                return params != null ? params : new HashMap<>();
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String token = SharedPrefManager.getInstance(ctx).getKeyAccessToken();
                if(token != null) headers.put("Authorization", "Bearer " + token);
                return headers;
            }

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }
        };
        addToRequestQueue(req);
    }

    // --------------------------------------------------------------------------------
    // UTILS
    // --------------------------------------------------------------------------------

    private ArrayList<Triagem> converterPulseirasParaTriagens(ArrayList<Pulseira> pulseiras) {
        ArrayList<Triagem> lista = new ArrayList<>();
        for (Pulseira p : pulseiras) {
            Triagem t = new Triagem();
            t.setId(p.getId());
            t.setPulseira(p);
            t.setMotivoconsulta(p.getMotivo());
            t.setQueixaprincipal(p.getQueixa());
            t.setDescricaosintomas(p.getDescricao());
            t.setIniciosintomas(p.getInicioSintomas());
            t.setAlergias(p.getAlergias());
            t.setMedicacao(p.getMedicacao());
            t.setDatatriagem(p.getDataEntrada());

            try { t.setIntensidadedor(Integer.parseInt(p.getDor())); } catch (Exception e) { t.setIntensidadedor(0); }

            Paciente pac = new Paciente();
            pac.setNome(p.getNomePaciente());
            pac.setSns(p.getSns());
            pac.setTelefone(p.getTelefone());
            t.setPaciente(pac);

            lista.add(t);
        }
        return lista;
    }

    public boolean isInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    public String getAPIUrl(String endpoint) {
        String baseUrl = SharedPrefManager.getInstance(ctx).getServerUrl();
        String token = SharedPrefManager.getInstance(ctx).getKeyAccessToken();

        if (!baseUrl.endsWith("/")) baseUrl += "/";
        if (endpoint.startsWith("/")) endpoint = endpoint.substring(1);

        String fullUrl = baseUrl + endpoint;

        if (!endpoint.contains("login") && token != null && !token.isEmpty()) {
            fullUrl += (fullUrl.contains("?") ? "&" : "?") + "auth_key=" + token;
        }
        return fullUrl;
    }
}