package pt.ipleiria.estg.dei.emergencysts.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import pt.ipleiria.estg.dei.emergencysts.R;
import pt.ipleiria.estg.dei.emergencysts.activities.enfermeiro.EnfermeiroActivity;
import pt.ipleiria.estg.dei.emergencysts.activities.paciente.PacienteActivity;
import pt.ipleiria.estg.dei.emergencysts.modelo.Enfermeiro;
import pt.ipleiria.estg.dei.emergencysts.mqtt.MqttClientManager; // IMPORTANTE: Importar o gestor MQTT
import pt.ipleiria.estg.dei.emergencysts.network.VolleySingleton;
import pt.ipleiria.estg.dei.emergencysts.utils.SharedPrefManager;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // --- VERIFICAÇÃO AUTOMÁTICA (Se já tiver feito login antes) ---
        if (SharedPrefManager.getInstance(this).isLoggedIn()) {

            // IMPORTANTE: Ligar ao MQTT se já estiver logado para receber notificações em background
            MqttClientManager.getInstance(this).connect();

            Enfermeiro user = SharedPrefManager.getInstance(this).getEnfermeiroBase();
            String role = user.getRole();

            // Se por acaso um médico ficou logado, fazemos logout forçado
            if (role != null && role.equalsIgnoreCase("medico")) {
                SharedPrefManager.getInstance(this).logout();
                Toast.makeText(this, "Acesso Médico disponível apenas na Web.", Toast.LENGTH_LONG).show();
            } else {
                // Login Válido: Redireciona para a atividade correta
                Intent intent;
                if (role != null && (role.equalsIgnoreCase("paciente") || role.equalsIgnoreCase("utente"))) {
                    intent = new Intent(this, PacienteActivity.class);
                } else {
                    // Por defeito vai para Enfermeiro (serve para admin também)
                    intent = new Intent(this, EnfermeiroActivity.class);
                }

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return;
            }
        }

        // --- CONFIGURAÇÃO DOS BOTÕES ---

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        ImageView btnBack = findViewById(R.id.btnAtras);

        btnLogin.setOnClickListener(v -> loginUser());

        btnBack.setOnClickListener(v -> {
            Intent i = new Intent(LoginActivity.this, ConfigActivity.class);
            startActivity(i);
            finish();
        });
    }

    private void loginUser() {
        final String username = etUsername.getText().toString().trim();
        final String password = etPassword.getText().toString().trim();

        if (!VolleySingleton.getInstance(this).isInternetConnection()) {
            Toast.makeText(this, "Sem ligação à Internet. Verifique o Wi-Fi ou Dados Móveis.", Toast.LENGTH_LONG).show();
            return;
        }

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!SharedPrefManager.getInstance(this).hasServerConfigured()) {
            Toast.makeText(this, "Vai às configurações e mete o IP!", Toast.LENGTH_LONG).show();
            return;
        }

        // Enviar credenciais também no URL (Segurança extra para o Yii2 ler se necessário)
        String url = VolleySingleton.getInstance(this).getAPIUrl(VolleySingleton.ENDPOINT_LOGIN);

        // Usamos StringRequest porque é o formato "Formulário" nativo que o PHP adora
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        // O servidor respondeu! Vamos ver se é sucesso.
                        JSONObject json = new JSONObject(response);
                        boolean status = json.optBoolean("status", false);

                        if (status) {
                            JSONObject data = json.optJSONObject("data");
                            if (data != null) {
                                // SUCESSO!
                                int userId = data.optInt("user_id", -1);
                                if (userId == -1) userId = data.optInt("id", -1);

                                String token = data.optString("token");
                                if (token.isEmpty()) token = data.optString("access_token");
                                if (token.isEmpty()) token = data.optString("auth_key");

                                String role = data.optString("role", "paciente");

                                // Barrar o médico
                                if (role.equalsIgnoreCase("medico")) {
                                    Toast.makeText(this, "Acesso negado: Médicos devem usar a plataforma Web.", Toast.LENGTH_LONG).show();
                                    return;
                                }

                                String email = data.optString("email", "");

                                if (!token.isEmpty()) {
                                    // Guarda os dados base do utilizador (ID, username, role)
                                    Enfermeiro enfermeiro = new Enfermeiro(userId, username, email, role);
                                    SharedPrefManager.getInstance(this).userLogin(enfermeiro, token);

                                    Toast.makeText(this, "Login efetuado!", Toast.LENGTH_SHORT).show();

                                    // IMPORTANTE: Ligar ao MQTT e Subscrever agora que temos o ID guardado
                                    MqttClientManager.getInstance(this).connect();

                                    SharedPrefManager.getInstance(this).navigateOnStart(this);
                                    finish();
                                }
                            }
                        } else {
                            String msg = json.optString("message", "Credenciais Inválidas");
                            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Erro ao ler resposta JSON", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    String erroMsg = "Erro de Ligação";
                    if (error.networkResponse != null) {
                        erroMsg = "Erro Servidor: " + error.networkResponse.statusCode;
                    }
                    Toast.makeText(this, erroMsg, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                // Enviar formato normal
                params.put("username", username);
                params.put("password", password);

                // Enviar formato Yii2 (LoginForm) - Isto ajuda o Yii2 a popular o model automaticamente
                params.put("LoginForm[username]", username);
                params.put("LoginForm[password]", password);

                return params;
            }

            @Override
            public String getBodyContentType() {
                // Garante que vai como formulário
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
}