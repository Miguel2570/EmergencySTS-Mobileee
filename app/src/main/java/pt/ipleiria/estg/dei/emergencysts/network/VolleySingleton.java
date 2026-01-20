package pt.ipleiria.estg.dei.emergencysts.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
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
    public static final String ENDPOINT_PACIENTE_PERFIL = "api/paciente/perfil";
    public static final String ENDPOINT_ENFERMEIRO = "api/enfermeiro";
    public static final String ENDPOINT_ENFERMEIRO_PERFIL = "api/enfermeiro/perfil";
    public static final String ENDPOINT_TRIAGEM = "api/triagem";
    public static final String ENDPOINT_PULSEIRA = "api/pulseira";
    public static final String ENDPOINT_TOTAL_USERS = "api/user/total";

    // --- INSTÂNCIA SINGLETON ---
    private static VolleySingleton instance;
    private RequestQueue requestQueue;
    private final Context ctx;

    // --- LISTENERS (Interfaces para a UI) ---
    private LoginListener loginListener;
    private PacienteListener pacienteListener;
    private TriagemListener triagemListener;

    //BASE DE DADOS LOCAL
    private PulseiraBDHelper dbHelper;

    //CONSTRUTOR PRIVADO
    private VolleySingleton(Context context) {
        this.ctx = context.getApplicationContext();
        this.requestQueue = getRequestQueue();
        this.dbHelper = PulseiraBDHelper.getInstance(ctx);
    }

    //OBTER INSTÂNCIA (Thread-Safe)
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

    // --- SETTERS DOS LISTENERS ---
    public void setLoginListener(LoginListener listener) { this.loginListener = listener; }
    public void setPacienteListener(PacienteListener listener) { this.pacienteListener = listener; }
    public void setTriagemListener(TriagemListener listener) { this.triagemListener = listener; }

    //LOGIN API
    public void loginAPI(final String username, final String password) {
        if (!isInternetConnection()) {
            Toast.makeText(ctx, "Sem ligação à Internet", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = getAPIUrl(ENDPOINT_LOGIN);

        StringRequest req = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        boolean status = json.optBoolean("status", false);

                        if (status) {
                            JSONObject data = json.optJSONObject("data");
                            if (data != null) {
                                // Extrair Token
                                String token = data.optString("auth_key");
                                if(token.isEmpty()) token = data.optString("access_token");

                                // Extrair Dados User
                                int id = data.optInt("user_id", -1);
                                if(id == -1) id = data.optInt("id", 0);

                                String email = data.optString("email", "");
                                String role = data.optString("role", "Enfermeiro");

                                // Guardar nas Preferências
                                Enfermeiro user = new Enfermeiro(id, username, email, role);
                                SharedPrefManager.getInstance(ctx).userLogin(user, token);

                                if (loginListener != null) {
                                    loginListener.onValidateLogin(token, username);
                                }
                            } else {
                                if (loginListener != null) loginListener.onLoginError("Resposta do servidor inválida.");
                            }
                        } else {
                            String msg = json.optString("message", "Credenciais Inválidas");
                            if (loginListener != null) loginListener.onLoginError(msg);
                        }
                    } catch (Exception e) {
                        if (loginListener != null) loginListener.onLoginError("Erro ao processar JSON.");
                    }
                },
                error -> {
                    if (loginListener != null) loginListener.onLoginError("Erro de comunicação com o servidor.");
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                // Enviar formato simples E formato Yii2 para garantir compatibilidade
                params.put("username", username);
                params.put("password", password);
                params.put("LoginForm[username]", username);
                params.put("LoginForm[password]", password);
                return params;
            }

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }
        };
        addToRequestQueue(req);
    }

    //PACIENTES API
    public void getAllPacientesAPI() {
        if (!isInternetConnection()) {
            Toast.makeText(ctx, "Sem ligação à Internet", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = getAPIUrl(ENDPOINT_PACIENTE);

        JsonArrayRequest req = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        ArrayList<Paciente> lista = PacienteJsonParser.parserJsonPacientes(response);
                        if (pacienteListener != null) {
                            pacienteListener.onPacientesLoaded(lista);
                        }
                    } catch (Exception e) {
                        if (pacienteListener != null) pacienteListener.onPacienteError("Erro ao ler dados dos pacientes.");
                    }
                },
                error -> {
                    if (pacienteListener != null) pacienteListener.onPacienteError("Não foi possível obter a lista de pacientes.");
                }
        );
        addToRequestQueue(req);
    }

    //HISTÓRICO / TRIAGENS (Com Sincronização SQLite)
    public void getHistoricoTriagensAPI(boolean isPaciente) {

        // A. MODO OFFLINE: Se não há net, carrega da BD Local imediatamente
        if (!isInternetConnection()) {
            Toast.makeText(ctx, "Modo Offline: A mostrar histórico guardado.", Toast.LENGTH_LONG).show();
            ArrayList<Pulseira> locais = dbHelper.getAllPulseiras();
            ArrayList<Triagem> triagensOffline = converterPulseirasParaTriagens(locais);

            if (triagemListener != null) {
                triagemListener.onTriagensLoaded(triagensOffline);
            }
            return;
        }

        //MODO ONLINE: Busca à API, Atualiza BD e Mostra
        String url = getAPIUrl(ENDPOINT_TRIAGEM + "?expand=paciente,pulseira,userprofile");

        JsonArrayRequest req = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        //Converter JSON para Objetos
                        ArrayList<Triagem> todasTriagens = TriagemJsonParser.parserJsonTriagens(response);
                        ArrayList<Triagem> listaFiltrada = new ArrayList<>();
                        ArrayList<Pulseira> pulseirasParaBD = new ArrayList<>();

                        //Filtrar e Preparar Dados
                        for (Triagem t : todasTriagens) {
                            String prioridade = (t.getPulseira() != null && t.getPulseira().getPrioridade() != null) ? t.getPulseira().getPrioridade() : "Pendente";
                            String status = (t.getPulseira() != null && t.getPulseira().getStatus() != null) ? t.getPulseira().getStatus() : "Desconhecido";

                            //Ignorar pendentes no histórico
                            if (prioridade.equalsIgnoreCase("Pendente")) continue;

                            //Se for enfermeiro, não mostrar finalizados (opcional, baseado no teu código antigo)
                            if (!isPaciente) {
                                String s = status.trim();
                                if (s.equalsIgnoreCase("Finalizado") || s.equalsIgnoreCase("Concluída") || s.equalsIgnoreCase("Atendido")) {
                                    continue;
                                }
                            }

                            listaFiltrada.add(t);

                            if (t.getPulseira() != null) {
                                Pulseira p = t.getPulseira();
                                // Preencher dados médicos na pulseira para persistência
                                p.setMotivo(t.getMotivoconsulta());
                                p.setQueixa(t.getQueixaprincipal());
                                p.setDescricao(t.getDescricaosintomas());
                                p.setInicioSintomas(t.getIniciosintomas());
                                p.setDor(String.valueOf(t.getIntensidadedor()));
                                p.setAlergias(t.getAlergias());
                                p.setMedicacao(t.getMedicacao());
                                // Preencher dados paciente
                                if (t.getPaciente() != null) {
                                    p.setNomePaciente(t.getPaciente().getNome());
                                    p.setSns(t.getPaciente().getSns());
                                    p.setTelefone(t.getPaciente().getTelefone());
                                }
                                pulseirasParaBD.add(p);
                            }
                        }

                        //SINCRONIZAR COM BD (Apaga Velhos -> Insere Novos)
                        dbHelper.sincronizarPulseiras(pulseirasParaBD);

                        //Devolver dados frescos à UI
                        if (triagemListener != null) {
                            triagemListener.onTriagensLoaded(listaFiltrada);
                        }

                    } catch (Exception e) {
                        if (triagemListener != null) triagemListener.onTriagemError("Erro ao processar dados.");
                    }
                },
                error -> {
                    // Falha silenciosa da API: Tenta carregar o que houver na BD
                    ArrayList<Pulseira> locais = dbHelper.getAllPulseiras();
                    ArrayList<Triagem> triagensOffline = converterPulseirasParaTriagens(locais);
                    if (triagemListener != null) triagemListener.onTriagensLoaded(triagensOffline);

                    Toast.makeText(ctx, "Erro de rede. A mostrar dados antigos.", Toast.LENGTH_SHORT).show();
                }
        );
        addToRequestQueue(req);
    }

    /**
     * Helper para fazer pedidos genéricos (ex: DELETE, PUT) sem parser automático.
     */
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
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }
        };
        addToRequestQueue(req);
    }

    /**
     * Converte Pulseiras da BD de volta para objetos Triagem para a UI
     */
    private ArrayList<Triagem> converterPulseirasParaTriagens(ArrayList<Pulseira> pulseiras) {
        ArrayList<Triagem> lista = new ArrayList<>();
        for (Pulseira p : pulseiras) {
            Triagem t = new Triagem();
            t.setId(p.getId());
            t.setPulseira(p);

            // Repor dados médicos
            t.setMotivoconsulta(p.getMotivo());
            t.setQueixaprincipal(p.getQueixa());
            t.setDescricaosintomas(p.getDescricao());
            t.setIniciosintomas(p.getInicioSintomas());
            t.setAlergias(p.getAlergias());
            t.setMedicacao(p.getMedicacao());
            t.setDatatriagem(p.getDataEntrada()); // Data visual

            try {
                t.setIntensidadedor(Integer.parseInt(p.getDor()));
            } catch (Exception e) {
                t.setIntensidadedor(0);
            }

            // Repor Paciente
            Paciente pac = new Paciente();
            pac.setNome(p.getNomePaciente());
            pac.setSns(p.getSns());
            pac.setTelefone(p.getTelefone());
            t.setPaciente(pac);

            lista.add(t);
        }
        return lista;
    }

    // Verificar Ligação
    public boolean isInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    // Construir URL com Token
    public String getAPIUrl(String endpoint) {
        String baseUrl = SharedPrefManager.getInstance(ctx).getServerUrl();
        String token = SharedPrefManager.getInstance(ctx).getKeyAccessToken();

        if (!baseUrl.endsWith("/")) baseUrl += "/";
        if (endpoint.startsWith("/")) endpoint = endpoint.substring(1);
        String fullUrl = baseUrl + endpoint;

        // Adiciona token se não for Login
        if (!endpoint.contains("login") && token != null) {
            if (fullUrl.contains("?")) {
                fullUrl += "&auth_key=" + token;
            } else {
                fullUrl += "?auth_key=" + token;
            }
        }
        return fullUrl;
    }
}