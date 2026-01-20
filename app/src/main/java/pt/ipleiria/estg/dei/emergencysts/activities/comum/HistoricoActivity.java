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
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import pt.ipleiria.estg.dei.emergencysts.R;
import pt.ipleiria.estg.dei.emergencysts.activities.enfermeiro.DetalhesTriagemActivity;
import pt.ipleiria.estg.dei.emergencysts.adapters.TriagemAdapter;
import pt.ipleiria.estg.dei.emergencysts.listeners.TriagemListener;
import pt.ipleiria.estg.dei.emergencysts.modelo.Paciente;
import pt.ipleiria.estg.dei.emergencysts.modelo.Pulseira;
import pt.ipleiria.estg.dei.emergencysts.modelo.Triagem;
import pt.ipleiria.estg.dei.emergencysts.network.VolleySingleton;
import pt.ipleiria.estg.dei.emergencysts.utils.PulseiraBDHelper;
import pt.ipleiria.estg.dei.emergencysts.utils.SharedPrefManager;
import pt.ipleiria.estg.dei.emergencysts.utils.TriagemJsonParser;

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
                Triagem t = listaTriagens.get(position);
                onTriagemClick(t.getId());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarHistorico();
    }

    private void carregarHistorico() {
        if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(true);

        String url = VolleySingleton.getInstance(this)
                .getAPIUrl(VolleySingleton.ENDPOINT_TRIAGEM + "?expand=paciente,pulseira,userprofile");

        JsonArrayRequest req = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                    try {
                        listaTriagens.clear();

                        // OFFLINE: Limpar BD antiga para atualizar com os dados novos
                        PulseiraBDHelper db = PulseiraBDHelper.getInstance(this);
                        db.removeAllPulseiras();

                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            Triagem t = TriagemJsonParser.parserJsonTriagem(obj);

                            String prioridade = "Pendente";
                            String status = "Desconhecido";

                            if (t.getPulseira() != null) {
                                if (t.getPulseira().getPrioridade() != null) prioridade = t.getPulseira().getPrioridade();
                                if (t.getPulseira().getStatus() != null) status = t.getPulseira().getStatus();
                            }

                            // Filtros
                            if (prioridade.equalsIgnoreCase("Pendente")) continue;

                            if (!isPaciente) {
                                String s = status.trim();
                                if (s.equalsIgnoreCase("Finalizado") ||
                                        s.equalsIgnoreCase("Concluída") ||
                                        s.equalsIgnoreCase("Concluida") ||
                                        s.equalsIgnoreCase("Atendido")) {
                                    continue;
                                }
                            }

                            listaTriagens.add(t);

                            // OFFLINE: Guardar na BD Local
                            if (t.getPulseira() != null) {
                                Pulseira p = t.getPulseira();

                                // Usar Getters da Triagem e Setters da Pulseira
                                p.setMotivo(t.getMotivoconsulta());
                                p.setQueixa(t.getQueixaprincipal());
                                p.setDescricao(t.getDescricaosintomas());
                                p.setInicioSintomas(t.getIniciosintomas());
                                p.setDor(String.valueOf(t.getIntensidadedor()));
                                p.setAlergias(t.getAlergias());
                                p.setMedicacao(t.getMedicacao());

                                // Dados do Paciente
                                if (t.getPaciente() != null) {
                                    p.setNomePaciente(t.getPaciente().getNome());
                                    p.setSns(t.getPaciente().getSns());
                                    p.setTelefone(t.getPaciente().getTelefone());
                                }

                                db.adicionarPulseira(p);
                            }
                        }

                        adapter.notifyDataSetChanged();
                        if (tvTotalTriagens != null) tvTotalTriagens.setText("Total triagens: " + listaTriagens.size());

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);

                    Toast.makeText(this, "Modo Offline: A carregar dados locais...", Toast.LENGTH_LONG).show();
                    carregarHistoricoOffline();
                }
        );

        req.setShouldCache(false);
        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    private void carregarHistoricoOffline() {
        PulseiraBDHelper db = PulseiraBDHelper.getInstance(this);
        ArrayList<Pulseira> pulseirasOffline = db.getAllPulseiras();

        listaTriagens.clear();

        for (Pulseira p : pulseirasOffline) {
            Triagem t = new Triagem();
            t.setId(p.getId());
            t.setPulseira(p);

            // Preencher dados médicos
            t.setMotivoconsulta(p.getMotivo());
            t.setQueixaprincipal(p.getQueixa());
            t.setDescricaosintomas(p.getDescricao());
            t.setIniciosintomas(p.getInicioSintomas());
            t.setAlergias(p.getAlergias());
            t.setMedicacao(p.getMedicacao());

            // Passar a data da pulseira para a triagem para aparecer na lista
            t.setDatatriagem(p.getDataEntrada());

            try {
                t.setIntensidadedor(Integer.parseInt(p.getDor()));
            } catch (Exception e) {
                t.setIntensidadedor(0);
            }

            // Recriar Paciente
            Paciente pac = new Paciente();
            pac.setNome(p.getNomePaciente());
            pac.setSns(p.getSns());
            pac.setTelefone(p.getTelefone());

            t.setPaciente(pac);

            listaTriagens.add(t);
        }

        adapter.notifyDataSetChanged();
        if (tvTotalTriagens != null) tvTotalTriagens.setText("Total (Offline): " + listaTriagens.size());
    }

    // INTERFACE TriagemListener
    @Override
    public void onTriagemClick(int id) {
        // Atualizado para usar o VolleySingleton (Padrão do projeto)
        if (!VolleySingleton.getInstance(this).isInternetConnection()) {
            Toast.makeText(this, "Sem internet: Não é possível ver os detalhes.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, DetalhesTriagemActivity.class);
        intent.putExtra("ID_TRIAGEM", id);
        startActivity(intent);
    }

    // O método onArquivarClick foi REMOVIDO daqui.
    // Lembre-se de o remover também da interface TriagemListener e do Adapter.

    @Override
    public void onEliminarClick(int id) {
        if (!VolleySingleton.getInstance(this).isInternetConnection()) {
            Toast.makeText(this, "Funcionalidade disponível apenas Online.", Toast.LENGTH_SHORT).show();
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

        // Map para passar o parâmetro _method=DELETE no corpo (compatibilidade Yii2/PHP)
        Map<String, String> params = new HashMap<>();
        params.put("_method", "DELETE");

        VolleySingleton.getInstance(this).apiRequest(Request.Method.POST, url, params,
                response -> {
                    Toast.makeText(this, "Eliminado.", Toast.LENGTH_SHORT).show();
                    carregarHistorico();
                },
                error -> Toast.makeText(this, "Erro ao eliminar.", Toast.LENGTH_SHORT).show()
        );
    }
}