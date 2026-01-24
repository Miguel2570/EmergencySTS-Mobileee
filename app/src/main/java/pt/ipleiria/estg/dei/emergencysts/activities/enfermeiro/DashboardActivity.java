package pt.ipleiria.estg.dei.emergencysts.activities.enfermeiro;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import pt.ipleiria.estg.dei.emergencysts.R;
import pt.ipleiria.estg.dei.emergencysts.network.VolleySingleton;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvTotalPulseiras, tvTotalTriagens, tvTotalPacientes;
    private Button btnAtualizar;
    private ImageView btnVoltar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        tvTotalPulseiras = findViewById(R.id.tvTotalPulseiras);
        tvTotalTriagens = findViewById(R.id.tvTotalTriagens);
        tvTotalPacientes = findViewById(R.id.tvTotalPacientes);
        btnAtualizar = findViewById(R.id.btnAtualizar);
        btnVoltar = findViewById(R.id.btnVoltar);

        if (btnVoltar != null) btnVoltar.setOnClickListener(v -> finish());

        btnAtualizar.setOnClickListener(v -> {
            carregarPulseiras();
            carregarTriagens();
            carregarPacientes();
            Toast.makeText(this, "Dashboard atualizada", Toast.LENGTH_SHORT).show();
        });

        carregarPulseiras();
        carregarTriagens();
        carregarPacientes();
    }

    private void carregarPulseiras() {
        String url = VolleySingleton.getInstance(this).getAPIUrl(VolleySingleton.ENDPOINT_PULSEIRA);

        StringRequest req = new StringRequest(Request.Method.GET, url, response -> {
            try {
                int total = 0;
                String resp = response.trim();

                if (resp.startsWith("[")) {
                    JSONArray data = new JSONArray(resp);
                    total = data.length();
                }
                else if (resp.startsWith("{")) {
                    JSONObject jsonResponse = new JSONObject(resp);
                    if (jsonResponse.has("data")) {
                        JSONArray data = jsonResponse.getJSONArray("data");
                        total = data.length();
                    }else{
                        total = 0;
                    }
                }
                tvTotalPulseiras.setText(String.valueOf(total));

            } catch (Exception e) {
                e.printStackTrace();
                tvTotalPulseiras.setText("Erro");
            }
        }, error -> {
            Toast.makeText(this, "Erro Rede", Toast.LENGTH_SHORT).show();
        });

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    private void carregarTriagens() {
        String url = VolleySingleton.getInstance(this).getAPIUrl(VolleySingleton.ENDPOINT_TRIAGEM);

        StringRequest req = new StringRequest(Request.Method.GET, url, response -> {
            try {
                int total = 0;
                String resp = response.trim();

                if (resp.startsWith("[")) {
                    JSONArray data = new JSONArray(resp);
                    total = data.length();
                }
                else if (resp.startsWith("{")) {
                    JSONObject jsonResponse = new JSONObject(resp);
                    if (jsonResponse.has("data")) {
                        JSONArray data = jsonResponse.getJSONArray("data");
                        total = data.length();
                    } else {
                        total = 0;
                    }
                }
                tvTotalTriagens.setText(String.valueOf(total));

            } catch (Exception e) {
                e.printStackTrace();
                tvTotalTriagens.setText("Erro");
            }
        }, error -> {
            tvTotalTriagens.setText("Erro Rede");
        });

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }
    private void carregarPacientes() {
        String url = VolleySingleton.getInstance(this).getAPIUrl(VolleySingleton.ENDPOINT_PACIENTE);

        StringRequest req = new StringRequest(Request.Method.GET, url, response -> {
            try {
                int total = 0;
                String resp = response.trim();

                if (resp.startsWith("[")) {
                    JSONArray data = new JSONArray(resp);
                    total = data.length();
                }
                else if (resp.startsWith("{")) {
                    JSONObject jsonResponse = new JSONObject(resp);
                    if (jsonResponse.has("data")) {
                        JSONArray data = jsonResponse.getJSONArray("data");
                        total = data.length();
                    } else {
                        total = 0;
                    }
                }
                tvTotalPacientes.setText(String.valueOf(total));

            } catch (Exception e) {
                e.printStackTrace();
                tvTotalPacientes.setText("Erro");
            }
        }, error -> {
            tvTotalPacientes.setText("Erro Rede");
        });

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }
}