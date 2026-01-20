package pt.ipleiria.estg.dei.emergencysts.activities.paciente;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

import pt.ipleiria.estg.dei.emergencysts.R;
import pt.ipleiria.estg.dei.emergencysts.modelo.Paciente;
import pt.ipleiria.estg.dei.emergencysts.network.VolleySingleton;
import pt.ipleiria.estg.dei.emergencysts.utils.SharedPrefManager;

public class EditarPerfilPacienteActivity extends AppCompatActivity {

    private EditText etNome, etEmail, etTelefone, etMorada, etNif, etSns;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil_paciente);

        // Inicializar Views
        ImageView btnCancel = findViewById(R.id.btnCancel);
        Button btnSaveBottom = findViewById(R.id.btnSaveBottom);

        etNome = findViewById(R.id.etNome);
        etEmail = findViewById(R.id.etEmail);
        etTelefone = findViewById(R.id.etTelefone);
        etMorada = findViewById(R.id.etMorada);
        etNif = findViewById(R.id.etNif);
        etSns = findViewById(R.id.etSns);
        progressBar = findViewById(R.id.progressBar);

        carregarDadosAtuais();

        btnCancel.setOnClickListener(v -> finish());
        btnSaveBottom.setOnClickListener(v -> guardarAlteracoes());
    }

    private void carregarDadosAtuais() {
        Paciente p = SharedPrefManager.getInstance(this).getPaciente();
        if (p != null) {
            etNome.setText(p.getNome());
            etEmail.setText(p.getEmail());
            etTelefone.setText(p.getTelefone());
            etMorada.setText(p.getMorada());
            etNif.setText(p.getNif());
            etSns.setText(p.getSns());
        }
    }

    private void guardarAlteracoes() {
        final String nome = etNome.getText().toString().trim();
        final String email = etEmail.getText().toString().trim();
        final String nif = etNif.getText().toString().trim();
        final String sns = etSns.getText().toString().trim();
        final String telefone = etTelefone.getText().toString().trim();
        final String morada = etMorada.getText().toString().trim();

        if (nome.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Nome e Email são obrigatórios!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        final Paciente original = SharedPrefManager.getInstance(this).getPaciente();

        String url = VolleySingleton.getInstance(this).getAPIUrl(VolleySingleton.ENDPOINT_PACIENTE_PERFIL);

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
                        Toast.makeText(this, "Erro de Validação: " + body, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Erro de ligação ao servidor", Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                params.put("_method", "PUT");

                params.put("Paciente[nome]", nome);
                params.put("Paciente[telefone]", telefone);
                params.put("Paciente[morada]", morada);

                if (!email.equalsIgnoreCase(original.getEmail())) {
                    params.put("Paciente[email]", email);
                }
                if (!nif.equals(original.getNif())) {
                    params.put("Paciente[nif]", nif);
                }
                if (!sns.equals(original.getSns())) {
                    params.put("Paciente[sns]", sns);
                }
                if (original.getGenero() != null) {
                    params.put("Paciente[genero]", original.getGenero());
                }
                if (original.getDataNascimento() != null) {
                    params.put("Paciente[datanascimento]", original.getDataNascimento());
                }

                return params;
            }

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void atualizarSharedPrefsLocalmente() {
        Paciente atual = SharedPrefManager.getInstance(this).getPaciente();
        if (atual != null) {
            atual.setNome(etNome.getText().toString());
            atual.setEmail(etEmail.getText().toString());
            atual.setTelefone(etTelefone.getText().toString());
            atual.setMorada(etMorada.getText().toString());
            atual.setNif(etNif.getText().toString());
            atual.setSns(etSns.getText().toString());

            SharedPrefManager.getInstance(this).savePaciente(atual);
        }
    }
}