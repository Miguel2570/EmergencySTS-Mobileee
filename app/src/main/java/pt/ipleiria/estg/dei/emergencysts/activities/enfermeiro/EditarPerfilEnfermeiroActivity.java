package pt.ipleiria.estg.dei.emergencysts.activities.enfermeiro;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

import pt.ipleiria.estg.dei.emergencysts.R;
import pt.ipleiria.estg.dei.emergencysts.modelo.Enfermeiro;
import pt.ipleiria.estg.dei.emergencysts.network.VolleySingleton;
import pt.ipleiria.estg.dei.emergencysts.utils.SharedPrefManager;

public class EditarPerfilEnfermeiroActivity extends AppCompatActivity {

    private EditText etNome, etEmail, etTelefone, etMorada, etNif, etSns;
    private ProgressBar progressBar;

    private Enfermeiro original;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil_enfermeiro);

        ImageView btnCancel = findViewById(R.id.btnCancel);
        Button btnSaveBottom = findViewById(R.id.btnSaveBottom);

        etNome = findViewById(R.id.etNome);
        etEmail = findViewById(R.id.etEmail);
        etTelefone = findViewById(R.id.etTelefone);
        etMorada = findViewById(R.id.etMorada);
        etNif = findViewById(R.id.etNif);
        etSns = findViewById(R.id.etSns);
        progressBar = findViewById(R.id.progressBar);

        // 1. Carregar e GUARDAR o original
        original = SharedPrefManager.getInstance(this).getEnfermeiro();
        carregarDadosAtuais();

        // Ações dos botões
        btnCancel.setOnClickListener(v -> finish());
        btnSaveBottom.setOnClickListener(v -> guardarAlteracoes());
    }

    private void carregarDadosAtuais() {
        if (original != null) {
            etNome.setText(original.getNome());
            etEmail.setText(original.getEmail());
            etTelefone.setText(original.getTelefone());
            etMorada.setText(original.getMorada());
            etNif.setText(original.getNif());
            etSns.setText(original.getSns());
        }
    }

    private void guardarAlteracoes() {
        final String nome = etNome.getText().toString().trim();
        final String email = etEmail.getText().toString().trim();

        if (nome.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Nome e Email são obrigatórios!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        if (original == null) original = SharedPrefManager.getInstance(this).getEnfermeiro();

        String url = VolleySingleton.getInstance(this).getAPIUrl(VolleySingleton.ENDPOINT_ENFERMEIRO + "/" + original.getId());

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Perfil atualizado com sucesso!", Toast.LENGTH_LONG).show();
                    atualizarSharedPrefsLocalmente();
                    finish();
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    if (error.networkResponse != null) {
                        String body = new String(error.networkResponse.data);
                        Log.e("API_ERRO", "Status: " + error.networkResponse.statusCode + " Body: " + body);

                        String msg = "Erro ao guardar.";
                        if(error.networkResponse.statusCode == 422) msg = "Erro: Dados duplicados ou inválidos.";
                        if(error.networkResponse.statusCode == 401) msg = "Sessão expirada.";

                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Erro de ligação ao servidor", Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                params.put("_method", "PUT");

                // Campos que enviamos sempre
                params.put("Enfermeiro[nome]", etNome.getText().toString());
                params.put("Enfermeiro[telefone]", etTelefone.getText().toString());
                params.put("Enfermeiro[morada]", etMorada.getText().toString());


                String novoEmail = etEmail.getText().toString();
                if (!novoEmail.equalsIgnoreCase(original.getEmail())) {
                    params.put("Enfermeiro[email]", novoEmail);
                }

                String novoNif = etNif.getText().toString();
                if (!novoNif.equals(original.getNif())) {
                    params.put("Enfermeiro[nif]", novoNif);
                }

                String novoSns = etSns.getText().toString();
                if (!novoSns.equals(original.getSns())) {
                    params.put("Enfermeiro[sns]", novoSns);
                }

                return params;
            }

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String token = SharedPrefManager.getInstance(getApplicationContext()).getKeyAccessToken();
                if (token != null) {
                    headers.put("Authorization", "Bearer " + token);
                }
                headers.put("X-HTTP-Method-Override", "PUT"); // Segurança extra
                return headers;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void atualizarSharedPrefsLocalmente() {
        if (original != null) {
            original.setNome(etNome.getText().toString());
            original.setEmail(etEmail.getText().toString());
            original.setTelefone(etTelefone.getText().toString());
            original.setMorada(etMorada.getText().toString());
            original.setNif(etNif.getText().toString());
            original.setSns(etSns.getText().toString());

            SharedPrefManager.getInstance(this).saveEnfermeiro(original);
        }
    }
}