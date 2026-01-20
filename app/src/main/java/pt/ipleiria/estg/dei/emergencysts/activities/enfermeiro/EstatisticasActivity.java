package pt.ipleiria.estg.dei.emergencysts.activities.enfermeiro;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import pt.ipleiria.estg.dei.emergencysts.R;
import pt.ipleiria.estg.dei.emergencysts.network.VolleySingleton;
import pt.ipleiria.estg.dei.emergencysts.utils.SharedPrefManager;

public class EstatisticasActivity extends AppCompatActivity {

    private TextView tvTotalUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estatisticas);

        tvTotalUsers = findViewById(R.id.tv_total_users);
        Button btnVoltar = findViewById(R.id.btn_voltar);

        btnVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        fetchTotalUsers();
    }

    private void fetchTotalUsers() {

        if (!VolleySingleton.getInstance(this).isInternetConnection()) {
            tvTotalUsers.setText("-");
            Toast.makeText(this, "Sem ligação à Internet", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = VolleySingleton.getInstance(this).getAPIUrl(VolleySingleton.ENDPOINT_TOTAL_USERS);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // 🔹 Lê o valor "total" que enviámos do PHP
                            int total = response.getInt("total");
                            tvTotalUsers.setText(String.valueOf(total));
                        } catch (JSONException e) {
                            tvTotalUsers.setText("Erro JSON");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Mostra erro detalhado se possível
                        String erroMsg = error.getMessage();
                        if (error.networkResponse != null) {
                            erroMsg += " (Código: " + error.networkResponse.statusCode + ")";
                        }
                        tvTotalUsers.setText("Erro");
                        Toast.makeText(EstatisticasActivity.this, "Falha: " + erroMsg, Toast.LENGTH_LONG).show();
                    }
                }
        ) {
            //Adiciona o Token de Autenticação no Cabeçalho
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String token = SharedPrefManager.getInstance(EstatisticasActivity.this).getKeyAccessToken();
                if (token != null && !token.isEmpty()) {
                    headers.put("Authorization", "Bearer " + token);
                }
                return headers;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }
}