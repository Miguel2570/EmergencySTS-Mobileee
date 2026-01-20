package pt.ipleiria.estg.dei.emergencysts.activities.enfermeiro;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import pt.ipleiria.estg.dei.emergencysts.R;
import pt.ipleiria.estg.dei.emergencysts.modelo.Paciente;
import pt.ipleiria.estg.dei.emergencysts.network.VolleySingleton;
import pt.ipleiria.estg.dei.emergencysts.utils.SharedPrefManager;

public class ConsultarPacienteActivity extends AppCompatActivity {

    private EditText edtNif;
    private LinearLayout emptyState;
    private CardView resultCard;
    private TextView tvNome, tvNif, tvSns;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consultar_paciente);

        edtNif = findViewById(R.id.edtNif);
        emptyState = findViewById(R.id.emptyState);
        resultCard = findViewById(R.id.resultCard);

        tvNome = findViewById(R.id.tvNome);
        tvNif = findViewById(R.id.tvNif);
        tvSns = findViewById(R.id.tvSns);

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Quando escrever 9 dígitos
        edtNif.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String nif = s.toString();
                if (nif.isEmpty()) {
                    emptyState.setVisibility(View.VISIBLE);
                    resultCard.setVisibility(View.GONE);
                    return;
                }
                if (nif.length() == 9) searchPaciente(nif);
            }
        });
    }

    private void searchPaciente(String nif) {

        emptyState.setVisibility(View.GONE);
        resultCard.setVisibility(View.GONE);

        String url = VolleySingleton.getInstance(this).getAPIUrl(VolleySingleton.ENDPOINT_PACIENTE + "?nif=" + nif);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                this::handleResponse,
                error -> showNotFound()
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void handleResponse(JSONArray array) {
        System.out.println("JSON RECEBIDO: " + array.toString());

        if (array.length() == 0) {
            showNotFound();
            return;
        }

        try {
            // Usa o novo Parser para converter o primeiro resultado
            JSONObject jsonPaciente = array.getJSONObject(0);
            Paciente p = pt.ipleiria.estg.dei.emergencysts.utils.PacienteJsonParser.parserJsonPaciente(jsonPaciente);

            if (p != null) {
                // Atualizar UI com os dados do objeto Paciente
                tvNome.setText(p.getNome());
                tvNif.setText("NIF: " + p.getNif());
                tvSns.setText("SNS: " + p.getSns());

                resultCard.setVisibility(View.VISIBLE);
                emptyState.setVisibility(View.GONE);

                // Passar dados para a próxima atividade via Intent
                resultCard.setOnClickListener(v -> {
                    Intent intent = new Intent(ConsultarPacienteActivity.this, DetalhesPacienteActivity.class);
                    intent.putExtra("nome", p.getNome());
                    intent.putExtra("nif", p.getNif());
                    intent.putExtra("sns", p.getSns());
                    intent.putExtra("telefone", p.getTelefone());
                    intent.putExtra("morada", p.getMorada());
                    intent.putExtra("genero", p.getGenero());
                    intent.putExtra("datanascimento", p.getDataNascimento());
                    startActivity(intent);
                });
            } else {
                showNotFound();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showNotFound();
        }
    }

    private void showNotFound() {
        Toast.makeText(this, "Paciente não foi encontrado", Toast.LENGTH_SHORT).show();
        resultCard.setVisibility(View.GONE);
        emptyState.setVisibility(View.VISIBLE);
    }
}
