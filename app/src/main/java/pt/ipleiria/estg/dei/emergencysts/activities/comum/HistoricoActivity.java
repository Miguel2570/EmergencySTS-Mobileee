package pt.ipleiria.estg.dei.emergencysts.activities.comum;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
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
    private Spinner spinnerFiltro;
    private ImageView btnOrdenar;
    private ImageView btnCalendario;
    private ImageView btnLimpar;
    private boolean isPaciente;
    private ArrayList<Triagem> listaTriagensExibida;
    private ArrayList<Triagem> listaOriginal;

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
        spinnerFiltro = findViewById(R.id.spinnerFiltro);
        btnOrdenar = findViewById(R.id.btnOrdenar);
        btnCalendario = findViewById(R.id.btnCalendario);
        btnLimpar = findViewById(R.id.btnLimpar);

        listaTriagensExibida = new ArrayList<>();
        listaOriginal = new ArrayList<>();

        String[] cores = {"Todas", "Vermelho", "Laranja", "Amarelo", "Verde", "Azul"};
        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cores);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFiltro.setAdapter(adapterSpinner);

        btnOrdenar.setOnClickListener(v -> mostrarMenuOrdenacao());
        btnCalendario.setOnClickListener(v -> mostrarMenuCalendario());
        btnLimpar.setOnClickListener(v -> limparFiltros());
        

        spinnerFiltro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String corSelecionada = cores[position];
                filtrarPorCor(corSelecionada);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

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

        adapter = new TriagemAdapter(this, listaTriagensExibida, !isPaciente, this);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < listaTriagensExibida.size()) {
                onTriagemClick(listaTriagensExibida.get(position).getId());
            }
        });
    }

    private void limparFiltros() {
        // Limpa a lista visual
        listaTriagensExibida.clear();

        listaTriagensExibida.addAll(listaOriginal);

        if (spinnerFiltro != null) {
            spinnerFiltro.setSelection(0);
        }

        adapter.notifyDataSetChanged();

        if (tvTotalTriagens != null) {
            tvTotalTriagens.setText("Total: " + listaTriagensExibida.size());
        }
        Toast.makeText(this, "Filtros limpos", Toast.LENGTH_SHORT).show();
    }

    private void mostrarMenuCalendario() {
        Calendar calendar = Calendar.getInstance();
        int ano = calendar.get(Calendar.YEAR);
        int mes = calendar.get(Calendar.MONTH);
        int dia = calendar.get(Calendar.DAY_OF_MONTH);

        android.app.DatePickerDialog datePicker = new android.app.DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    // Formatar a data para String (YYYY-MM-DD)
                    String dataEscolhida = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);

                    filtrarPorData(dataEscolhida);
                }, ano, mes, dia);

        datePicker.show();
    }
    private void filtrarPorData(String data) {
        listaTriagensExibida.clear();

        for (Triagem t : listaOriginal) {
            String dataTriagem = t.getDataTriagem();

            if (dataTriagem != null) {
                if (dataTriagem.startsWith(data)) {
                    listaTriagensExibida.add(t);
                }
            }
        }

        adapter.notifyDataSetChanged();

        if (listaTriagensExibida.isEmpty()) {
            Toast.makeText(this, "Nenhuma triagem encontrada em " + data, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Filtrado por: " + data, Toast.LENGTH_SHORT).show();
        }

        if (tvTotalTriagens != null) {
            tvTotalTriagens.setText("Total (" + data + "): " + listaTriagensExibida.size());
        }
    }

    private void filtrarPorCor(String cor) {
        listaTriagensExibida.clear();

        if (cor.equalsIgnoreCase("Todas")) {
            listaTriagensExibida.addAll(listaOriginal);
        } else {
            for (Triagem t : listaOriginal) {
                // Verifica se tem pulseira e se a cor bate certo
                if (t.getPulseira() != null && t.getPulseira().getPrioridade() != null) {
                    if (t.getPulseira().getPrioridade().equalsIgnoreCase(cor)) {
                        listaTriagensExibida.add(t);
                    }
                }
            }
        }
        adapter.notifyDataSetChanged();
        if (tvTotalTriagens != null) tvTotalTriagens.setText("Total: " + listaTriagensExibida.size());
    }

    @Override
    protected void onResume() {
        super.onResume();
        VolleySingleton.getInstance(this).setTriagemListener(this);
        carregarHistorico();
    }

    private void carregarHistorico() {
        if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(true);
        VolleySingleton.getInstance(this).getHistoricoTriagensAPI(isPaciente);
    }

    @Override
    public void onTriagensLoaded(ArrayList<Triagem> triagens) {
        if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);

        listaOriginal.clear();
        listaOriginal.addAll(triagens);

        if (spinnerFiltro.getSelectedItem() != null) {
            filtrarPorCor(spinnerFiltro.getSelectedItem().toString());
        } else {
            filtrarPorCor("Todas");
        }
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
    private void mostrarMenuOrdenacao() {
        String[] opcoes = {"Mais Recente", "Mais Antigo", "Nome Paciente (A-Z)"};

        new AlertDialog.Builder(this)
                .setTitle("Ordenar por:")
                .setItems(opcoes, (dialog, which) -> {
                    ordenarLista(which);
                })
                .show();
    }

    private void ordenarLista(int tipo) {
        Comparator<Triagem> comparador = null;

        switch (tipo) {
            case 0: // Mais Recente (Data Descendente)
                comparador = (t1, t2) -> {
                    // Corrigido para getDataTriagem()
                    String d1 = t1.getDataTriagem() != null ? t1.getDataTriagem() : "";
                    String d2 = t2.getDataTriagem() != null ? t2.getDataTriagem() : "";
                    return d2.compareTo(d1); // d2 - d1 inverte a ordem
                };
                break;
            case 1: // Mais Antigo (Data Ascendente)
                comparador = (t1, t2) -> {
                    String d1 = t1.getDataTriagem() != null ? t1.getDataTriagem() : "";
                    String d2 = t2.getDataTriagem() != null ? t2.getDataTriagem() : "";
                    return d1.compareTo(d2);
                };
                break;
            case 2: // Nome (A-Z)
                comparador = (t1, t2) -> {
                    String n1 = t1.getNomePaciente().toLowerCase();
                    String n2 = t2.getNomePaciente().toLowerCase();
                    return n1.compareTo(n2);
                };
                break;
        }

        if (comparador != null) {
            // Ordena ambas as listas
            Collections.sort(listaTriagensExibida, comparador);
            Collections.sort(listaOriginal, comparador);

            adapter.notifyDataSetChanged();
            Toast.makeText(this, "Lista ordenada!", Toast.LENGTH_SHORT).show();
        }
    }
}