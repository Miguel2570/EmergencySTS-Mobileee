package pt.ipleiria.estg.dei.emergencysts.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;
import java.util.Calendar;

import pt.ipleiria.estg.dei.emergencysts.R;
import pt.ipleiria.estg.dei.emergencysts.activities.auth.ConfigActivity;
import pt.ipleiria.estg.dei.emergencysts.activities.enfermeiro.EditarPerfilEnfermeiroActivity;
import pt.ipleiria.estg.dei.emergencysts.activities.paciente.EditarPerfilPacienteActivity;
import pt.ipleiria.estg.dei.emergencysts.modelo.Enfermeiro;
import pt.ipleiria.estg.dei.emergencysts.modelo.Paciente;
import pt.ipleiria.estg.dei.emergencysts.network.VolleySingleton;
import pt.ipleiria.estg.dei.emergencysts.utils.SharedPrefManager;

public class PerfilFragment extends Fragment {

    private TextView tvNome, tvEmail, tvDataNasc, tvIdade, tvTelefone, tvSns, tvNif, tvMorada;
    private Button btnLogout;
    private ImageView btnBack, btnSettings, btnEditar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //  Insuflar o layout
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        //  Ligar TODOS os componentes aos IDs do XML
        tvNome      = view.findViewById(R.id.tvNomeCompleto);
        tvEmail     = view.findViewById(R.id.tvEmail);
        tvDataNasc  = view.findViewById(R.id.tvDataNasc);
        tvIdade     = view.findViewById(R.id.tvIdade);
        tvTelefone  = view.findViewById(R.id.tvTelefone);
        tvSns       = view.findViewById(R.id.tvSns);
        tvNif       = view.findViewById(R.id.tvNif);
        tvMorada    = view.findViewById(R.id.tvMorada);

        // Botões
        btnLogout   = view.findViewById(R.id.btnLogout);
        btnBack     = view.findViewById(R.id.btnBack);
        btnSettings = view.findViewById(R.id.btnSettings);
        btnEditar   = view.findViewById(R.id.btnEditar);

        //  CONFIGURAR OS CLIQUES

        // Botão Editar
        if (btnEditar != null) {
            btnEditar.setOnClickListener(v -> {
                if (getContext() == null) return;
                String role = SharedPrefManager.getInstance(getContext()).getEnfermeiroBase().getRole();
                Intent intent;

                // Abre a atividade de edição consoante o tipo de utilizador
                if (role != null && (role.equalsIgnoreCase("paciente") || role.equalsIgnoreCase("utente"))) {
                    intent = new Intent(getContext(), EditarPerfilPacienteActivity.class);
                } else {
                    intent = new Intent(getContext(), EditarPerfilEnfermeiroActivity.class);
                }
                startActivity(intent);
            });
        }

        // Botão Logout
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                if (getContext() == null) return;

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Terminar Sessão");
                builder.setMessage("Tem a certeza que deseja sair?");

                builder.setPositiveButton("Sim", (dialog, which) -> {
                    SharedPrefManager.getInstance(getContext()).logout();
                });

                builder.setNegativeButton("Não", (dialog, which) -> {
                    dialog.dismiss();
                });

                builder.show();
            });
        }

        // Botão Voltar
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() != null) getActivity().finish();
            });
        }

        // Botão Settings
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v ->
                    startActivity(new Intent(getContext(), ConfigActivity.class))
            );
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getContext() == null) return;

        String role = SharedPrefManager.getInstance(getContext()).getEnfermeiroBase().getRole();

        // Se for Paciente carrega paciente
        if (role != null && (role.equalsIgnoreCase("paciente") || role.equalsIgnoreCase("utente"))) {
            carregarPerfilPaciente();
        } else {
            // Caso contrário (enfermeiro ou admin)
            carregarPerfilEnfermeiro();
        }
    }

    private void carregarPerfilEnfermeiro() {
        if (getContext() == null) return;

        Enfermeiro stored = SharedPrefManager.getInstance(getContext()).getEnfermeiro();
        String token = SharedPrefManager.getInstance(getContext()).getKeyAccessToken();
        String baseUrl = SharedPrefManager.getInstance(getContext()).getServerUrl();

        if (!baseUrl.endsWith("/")) baseUrl += "/";
        String url = baseUrl + "api/enfermeiro/perfil?auth_key=" + token;

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    if (getContext() == null) return;
                    try {
                        JSONObject data = response.has("data") ? response.optJSONObject("data") : response;
                        if (data == null) data = response;

                        String nome   = data.optString("nome", "---");
                        String email  = data.optString("email", "---");
                        String nasc   = data.optString("datanascimento", "---");
                        String tel    = data.optString("telefone", "---");
                        String sns    = data.optString("sns", "---");
                        String nif    = data.optString("nif", "---");
                        String morada = data.optString("morada", "---");

                        tvNome.setText(nome);
                        tvEmail.setText(email);
                        tvDataNasc.setText(nasc);
                        tvTelefone.setText(tel);
                        tvSns.setText(sns);
                        tvNif.setText(nif);
                        tvMorada.setText(morada);
                        tvIdade.setText(calcularIdade(nasc) + " anos");

                        // Atualiza Localmente
                        if (stored != null) {
                            stored.setNome(nome);
                            stored.setEmail(email);
                            stored.setDataNascimento(nasc);
                            stored.setTelefone(tel);
                            stored.setSns(sns);
                            stored.setNif(nif);
                            stored.setMorada(morada);
                            SharedPrefManager.getInstance(getContext()).saveEnfermeiro(stored);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> { /* Ignorar erros silenciosos no enfermeiro para não spammar */ }
        );

        VolleySingleton.getInstance(getContext()).addToRequestQueue(req);
    }

    private void carregarPerfilPaciente() {
        if (getContext() == null) return;

        // 1. Preparar Token e URL
        String token = SharedPrefManager.getInstance(getContext()).getKeyAccessToken();
        String baseUrl = SharedPrefManager.getInstance(getContext()).getServerUrl();
        if (!baseUrl.endsWith("/")) baseUrl += "/";

        // MUDANÇA IMPORTANTE: Endpoint específico do perfil do paciente
        String url = baseUrl + "api/paciente/perfil?auth_key=" + token;

        // MUDANÇA IMPORTANTE: JsonObjectRequest em vez de Array
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    if (getContext() == null) return;
                    try {
                        // LOG PARA DEBUG: Vê o que aparece no Logcat com a tag "API_DEBUG"
                        Log.d("API_DEBUG", "Resposta Paciente: " + response.toString());

                        // Verifica se os dados vêm dentro de "data" ou estão na raiz
                        JSONObject data = response.has("data") ? response.optJSONObject("data") : response;
                        if (data == null) data = response;

                        // 2. Extrair dados do JSON (Tenta variações comuns de nomes)
                        int idPac     = data.optInt("id", 0);
                        String nome   = data.optString("nome", "---");
                        String email  = data.optString("email", "---");

                        // Tenta ler "datanascimento" ou "data_nascimento"
                        String nasc = data.has("datanascimento") ? data.optString("datanascimento") : data.optString("data_nascimento", "---");
                        if (nasc.equals("null")) nasc = "---";

                        String tel    = data.optString("telefone", "---");

                        // Tenta ler "sns", "numUtente" ou "numero_utente"
                        String sns = "---";
                        if (data.has("sns")) sns = data.optString("sns");
                        else if (data.has("numUtente")) sns = data.optString("numUtente");
                        else if (data.has("numero_utente")) sns = data.optString("numero_utente");

                        String nif    = data.optString("nif", "---");
                        String morada = data.optString("morada", "---");

                        // Tenta ler "genero" ou "sexo"
                        String genero = data.has("genero") ? data.optString("genero", "M") : data.optString("sexo", "M");

                        // 3. Atualizar a Interface (UI)
                        tvNome.setText(nome);
                        tvEmail.setText(email);
                        tvDataNasc.setText(nasc);
                        tvTelefone.setText(tel);
                        tvSns.setText(sns);
                        tvNif.setText(nif);
                        tvMorada.setText(morada);
                        tvIdade.setText(calcularIdade(nasc) + " anos");

                        // 4. Guardar no SharedPrefManager
                        Paciente stored = SharedPrefManager.getInstance(getContext()).getPaciente();
                        String username = SharedPrefManager.getInstance(getContext()).getEnfermeiroBase().getUsername();

                        // Como Paciente não tem setId(), se o ID for novo, recriamos o objeto
                        if (stored == null || stored.getId() != idPac) {
                            stored = new Paciente(idPac, username, email, "paciente");
                        }

                        // Atualizar restantes campos
                        stored.setNome(nome);
                        stored.setEmail(email);
                        stored.setDataNascimento(nasc);
                        stored.setTelefone(tel);
                        stored.setSns(sns);
                        stored.setNif(nif);
                        stored.setMorada(morada);
                        stored.setGenero(genero);

                        SharedPrefManager.getInstance(getContext()).savePaciente(stored);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Erro ao processar dados do paciente.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    if (getContext() != null) {
                        // Mostra erro apenas se falhar mesmo a ligação
                        String err = error.getMessage();
                        Log.e("API_DEBUG", "Erro Volley: " + err);
                        // Opcional: Toast.makeText(getContext(), "Erro ao carregar perfil", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        VolleySingleton.getInstance(getContext()).addToRequestQueue(req);
    }

    private int calcularIdade(String data) {
        try {
            if (data == null || data.trim().isEmpty() || data.contains("---")) return 0;
            String[] p = data.split("-");
            if (p.length != 3) return 0;
            int ano = Integer.parseInt(p[0]);
            int mes = Integer.parseInt(p[1]);
            int dia = Integer.parseInt(p[2]);
            Calendar hoje = Calendar.getInstance();
            int idade = hoje.get(Calendar.YEAR) - ano;
            if (hoje.get(Calendar.MONTH) + 1 < mes || (hoje.get(Calendar.MONTH) + 1 == mes && hoje.get(Calendar.DAY_OF_MONTH) < dia))
                idade--;
            return idade;
        } catch (Exception ignored) { return 0; }
    }
}