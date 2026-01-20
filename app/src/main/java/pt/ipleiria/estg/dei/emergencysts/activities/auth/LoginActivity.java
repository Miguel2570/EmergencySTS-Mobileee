package pt.ipleiria.estg.dei.emergencysts.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import pt.ipleiria.estg.dei.emergencysts.R;
import pt.ipleiria.estg.dei.emergencysts.activities.enfermeiro.EnfermeiroActivity;
import pt.ipleiria.estg.dei.emergencysts.activities.paciente.PacienteActivity;
import pt.ipleiria.estg.dei.emergencysts.listeners.LoginListener;
import pt.ipleiria.estg.dei.emergencysts.modelo.Enfermeiro;
import pt.ipleiria.estg.dei.emergencysts.mqtt.MqttClientManager;
import pt.ipleiria.estg.dei.emergencysts.network.VolleySingleton;
import pt.ipleiria.estg.dei.emergencysts.utils.SharedPrefManager;

public class LoginActivity extends AppCompatActivity implements LoginListener {

    private EditText etUsername, etPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Se já tem login guardado, valida e navega diretamente
        if (SharedPrefManager.getInstance(this).isLoggedIn()) {
            checkRoleAndNavigate();
            return;
        }

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        ImageView btnConfig = findViewById(R.id.btnAtras); // Assumi que o ID da roda dentada é btnAtras ou btnConfig

        // Registar o listener (Importante fazer isto também no onResume)
        VolleySingleton.getInstance(this).setLoginListener(this);

        btnLogin.setOnClickListener(v -> loginUser());

        btnConfig.setOnClickListener(v -> {
            Intent i = new Intent(LoginActivity.this, ConfigActivity.class);
            startActivity(i);
            // Não fazemos finish() aqui para o user poder voltar ao login com o botão back do telemóvel
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        VolleySingleton.getInstance(this).setLoginListener(this);
    }

    private void loginUser() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            etUsername.setError("Preencha este campo");
            return;
        }

        if (!SharedPrefManager.getInstance(this).hasServerConfigured()) {
            Toast.makeText(this, "Configure o IP do servidor primeiro!", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, ConfigActivity.class));
            return;
        }

        // Bloquear botão para evitar cliques múltiplos
        btnLogin.setEnabled(false);
        btnLogin.setText("A entrar...");

        VolleySingleton.getInstance(this).loginAPI(username, password);
    }

    @Override
    public void onValidateLogin(String token, String username, String role) {
        btnLogin.setEnabled(true);
        btnLogin.setText("Login"); // Repor texto original
        Toast.makeText(this, "Bem-vindo, " + username, Toast.LENGTH_SHORT).show();

        checkRoleAndNavigate();
    }

    @Override
    public void onLoginError(String error) {
        // Erro!
        btnLogin.setEnabled(true);
        btnLogin.setText("Login"); // Repor texto original

        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
    }

    private void checkRoleAndNavigate() {
        MqttClientManager.getInstance(this).connect();

        Enfermeiro user = SharedPrefManager.getInstance(this).getEnfermeiroBase();

        if (user == null) {
            SharedPrefManager.getInstance(this).logout();
            return;
        }

        String role = user.getRole();
        if(role == null) role = "";

        if (role.equalsIgnoreCase("medico")) {
            SharedPrefManager.getInstance(this).logout();
            MqttClientManager.getInstance(this).disconnect();
            Toast.makeText(this, "Médicos devem usar a plataforma Web.", Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent;
        if (role.equalsIgnoreCase("paciente") || role.equalsIgnoreCase("utente")) {
            intent = new Intent(this, PacienteActivity.class);
        } else {
            intent = new Intent(this, EnfermeiroActivity.class);
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}