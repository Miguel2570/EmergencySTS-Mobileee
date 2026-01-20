package pt.ipleiria.estg.dei.emergencysts.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// NOTA: Os imports do Volley e JSON desapareceram daqui!
import pt.ipleiria.estg.dei.emergencysts.R;
import pt.ipleiria.estg.dei.emergencysts.activities.enfermeiro.EnfermeiroActivity;
import pt.ipleiria.estg.dei.emergencysts.activities.paciente.PacienteActivity;
import pt.ipleiria.estg.dei.emergencysts.listeners.LoginListener; // A tua nova interface
import pt.ipleiria.estg.dei.emergencysts.modelo.Enfermeiro;
import pt.ipleiria.estg.dei.emergencysts.mqtt.MqttClientManager;
import pt.ipleiria.estg.dei.emergencysts.network.VolleySingleton;
import pt.ipleiria.estg.dei.emergencysts.utils.SharedPrefManager;

public class LoginActivity extends AppCompatActivity implements LoginListener { // 1. Implementa a Interface

    private EditText etUsername, etPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 2. Define esta Activity como quem vai "ouvir" a resposta do Singleton
        VolleySingleton.getInstance(this).setLoginListener(this);

        // --- VERIFICAÇÃO AUTOMÁTICA ---
        // Se já tem login guardado, valida e navega
        if (SharedPrefManager.getInstance(this).isLoggedIn()) {
            checkRoleAndNavigate();
            return; // Impede que o resto do onCreate corra se já vamos sair
        }

        // --- UI ---
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
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validações de Interface
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!SharedPrefManager.getInstance(this).hasServerConfigured()) {
            Toast.makeText(this, "Vai às configurações e mete o IP!", Toast.LENGTH_LONG).show();
            return;
        }

        // 3. CHAMADA SIMPLES AO SINGLETON
        // Toda a lógica de Volley, JSON e SharedParams foi movida para lá
        VolleySingleton.getInstance(this).loginAPI(username, password);
    }

    // ==========================================================
    // MÉTODOS DA INTERFACE LOGINLISTENER (Respostas do Singleton)
    // ==========================================================

    @Override
    public void onValidateLogin(String token, String username) {
        // Se este método foi chamado, o Singleton JÁ guardou o user no SharedPrefManager com sucesso.
        Toast.makeText(this, "Login efetuado!", Toast.LENGTH_SHORT).show();

        // Agora só precisamos de ver a Role e mudar de ecrã
        checkRoleAndNavigate();
    }

    @Override
    public void onLoginError(String error) {
        // Se falhou (senha errada ou erro de rede), mostramos a mensagem
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
    }

    // ==========================================================
    // MÉTODOS AUXILIARES
    // ==========================================================

    private void checkRoleAndNavigate() {
        // 1. Ligar ao MQTT (fundamental estar conectado para receber notificações)
        MqttClientManager.getInstance(this).connect();

        // 2. Obter dados do user guardado
        Enfermeiro user = SharedPrefManager.getInstance(this).getEnfermeiroBase();

        if (user == null) return; // Segurança

        String role = user.getRole();

        // 3. Verificar Regras de Acesso (Ex: Barrar Médicos)
        if (role != null && role.equalsIgnoreCase("medico")) {
            SharedPrefManager.getInstance(this).logout();
            MqttClientManager.getInstance(this).disconnect(); // Desligar se conectou
            Toast.makeText(this, "Acesso Médico disponível apenas na Web.", Toast.LENGTH_LONG).show();
            return;
        }

        // 4. Navegação
        Intent intent;
        if (role != null && (role.equalsIgnoreCase("paciente") || role.equalsIgnoreCase("utente"))) {
            intent = new Intent(this, PacienteActivity.class);
        } else {
            intent = new Intent(this, EnfermeiroActivity.class);
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}