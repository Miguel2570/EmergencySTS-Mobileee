package pt.ipleiria.estg.dei.emergencysts.activities.enfermeiro;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import pt.ipleiria.estg.dei.emergencysts.R;
import pt.ipleiria.estg.dei.emergencysts.network.VolleySingleton;

public class TotalUtilizadoresActivity extends AppCompatActivity {

    private TextView tvTotalUsers;
    private ImageView btnVoltar;
    private ArrayList<JSONObject> listaUtilizadores;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_total_utilizadores);

        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        btnVoltar = findViewById(R.id.btnVoltar);

        listaUtilizadores = new ArrayList<>();

        btnVoltar.setOnClickListener(v -> finish());

        carregarUtilizadores();
    }

    private void carregarUtilizadores() {
        
        String url = VolleySingleton.getInstance(this).getAPIUrl(VolleySingleton.ENDPOINT_PACIENTE);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject jsonResponse = new JSONObject(response);

                if (jsonResponse.has("data")) {
                    JSONArray jsonArray = jsonResponse.getJSONArray("data");

                    listaUtilizadores.clear();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject user = jsonArray.getJSONObject(i);
                        listaUtilizadores.add(user);
                    }

                    // Obtemos o total real da lista
                    int total = listaUtilizadores.size();

                    tvTotalUsers.setText("Existem um total de " + total + " pacientes");

                } else {
                    tvTotalUsers.setText("Existem um total de 0 pacientes");
                }

            } catch (Exception e) {
                e.printStackTrace();
                tvTotalUsers.setText("Erro JSON");
                Toast.makeText(this, "Erro ao processar dados: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        },
                error -> {
                    tvTotalUsers.setText("-");
                    Toast.makeText(this, "Erro de Rede: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });

        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }
}