package pt.ipleiria.estg.dei.emergencysts.activities.comum;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import pt.ipleiria.estg.dei.emergencysts.R;
import pt.ipleiria.estg.dei.emergencysts.activities.enfermeiro.DetalhesTriagemActivity;
import pt.ipleiria.estg.dei.emergencysts.adapters.TriagemAdapter;
import pt.ipleiria.estg.dei.emergencysts.listeners.TriagemListener;
import pt.ipleiria.estg.dei.emergencysts.modelo.Triagem;
import pt.ipleiria.estg.dei.emergencysts.network.VolleySingleton;
import pt.ipleiria.estg.dei.emergencysts.utils.SharedPrefManager;

public class HistoricoActivity extends AppCompatActivity implements TriagemListener {

    private ListView listView;
    private TriagemAdapter adapter;
    private TextView tvTitulo, tvTotalTriagens;
    private ImageView btnBack;
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean isPaciente;
    private ArrayList<Triagem> listaTriagens;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico);

        VolleySingleton.getInstance(this).setTriagemListener(this);

        tvTitulo = findViewById(R.id.tvTitulo);
        tvTotalTriagens = findViewById(R.id.tvTotalTriagens);
        btnBack = findViewById(R.id.btnBack);
        listView = findViewById(R.id.listViewTriagens);
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);

        listaTriagens = new ArrayList<>();

        String role = "";
        if (SharedPrefManager.getInstance(this).getEnfermeiroBase() != null) {
            role = SharedPrefManager.getInstance(this).getEnfermeiroBase().getRole();
        }
        isPaciente = role != null && (role.equalsIgnoreCase("paciente") || role.equalsIgnoreCase("utente"));

        tvTitulo.setText(isPaciente ? "O Meu Histórico" : "Histórico Geral");
        btnBack.setOnClickListener(v -> finish());

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(this::carregarHistorico);
        }

        adapter = new TriagemAdapter(this, listaTriagens, !isPaciente, this);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < listaTriagens.size()) {
                onTriagemClick(listaTriagens.get(position).getId());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Garante que o listener está ativo ao voltar a esta activity
        VolleySingleton.getInstance(this).setTriagemListener(this);
        carregarHistorico();
    }

    private void carregarHistorico() {
        if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(true);
        // Toda a lógica foi movida para o Singleton!
        VolleySingleton.getInstance(this).getHistoricoTriagensAPI(isPaciente);
    }

    @Override
    public void onTriagensLoaded(ArrayList<Triagem> triagens) {
        if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
        listaTriagens.clear();
        listaTriagens.addAll(triagens);
        adapter.notifyDataSetChanged();
        if (tvTotalTriagens != null) tvTotalTriagens.setText("Total: " + listaTriagens.size());
    }

    @Override
    public void onTriagemError(String error) {
        if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTriagemClick(int id) {
        if (!VolleySingleton.getInstance(this).isInternetConnection()) {
            Toast.makeText(this, "Sem internet: Detalhes indisponíveis.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, DetalhesTriagemActivity.class);
        intent.putExtra("ID_TRIAGEM", id);
        startActivity(intent);
    }

    @Override
    public void onEliminarClick(int id) {
        if (!VolleySingleton.getInstance(this).isInternetConnection()) {
            Toast.makeText(this, "Apenas Online.", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Eliminar")
                .setMessage("Tem a certeza?")
                .setPositiveButton("Eliminar", (d, w) -> eliminarTriagemPermanente(id))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarTriagemPermanente(int idTriagem) {
        String url = VolleySingleton.getInstance(this).getAPIUrl(VolleySingleton.ENDPOINT_TRIAGEM + "/" + idTriagem);
        Map<String, String> params = new HashMap<>();
        params.put("_method", "DELETE");

        VolleySingleton.getInstance(this).apiRequest(Request.Method.POST, url, params,
                response -> {
                    Toast.makeText(this, "Eliminado.", Toast.LENGTH_SHORT).show();
                    carregarHistorico(); // Recarrega
                },
                error -> Toast.makeText(this, "Erro ao eliminar.", Toast.LENGTH_SHORT).show()
        );
    }
}