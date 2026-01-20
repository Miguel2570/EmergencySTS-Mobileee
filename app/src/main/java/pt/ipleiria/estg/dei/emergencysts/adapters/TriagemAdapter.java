package pt.ipleiria.estg.dei.emergencysts.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;

import java.util.ArrayList;

import pt.ipleiria.estg.dei.emergencysts.R;
import pt.ipleiria.estg.dei.emergencysts.listeners.TriagemListener;
import pt.ipleiria.estg.dei.emergencysts.modelo.Triagem;
import pt.ipleiria.estg.dei.emergencysts.utils.SharedPrefManager;

public class TriagemAdapter extends BaseAdapter {

    private final Context context;
    private final ArrayList<Triagem> triagens;
    private final LayoutInflater inflater;
    private final boolean isEnfermeiroView;
    private final TriagemListener listener;

    public TriagemAdapter(Context context, ArrayList<Triagem> triagens, boolean isEnfermeiroView, TriagemListener listener) {
        this.context = context;
        this.triagens = triagens;
        this.inflater = LayoutInflater.from(context);
        this.isEnfermeiroView = isEnfermeiroView;
        this.listener = listener;
    }

    @Override
    public int getCount() { return triagens.size(); }

    @Override
    public Object getItem(int position) { return triagens.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_triagem, parent, false);
            holder = new ViewHolder();
            holder.tvNome = convertView.findViewById(R.id.tvNome);
            holder.tvSNS = convertView.findViewById(R.id.tvSNS);
            holder.tvStatus = convertView.findViewById(R.id.tvStatus);
            holder.tvData = convertView.findViewById(R.id.tvData);
            holder.tvHora = convertView.findViewById(R.id.tvHora);
            holder.tvQueixa = convertView.findViewById(R.id.tvQueixa);
            holder.tvEnfermeiro = convertView.findViewById(R.id.tvEnfermeiro);
            holder.dotPrioridade = convertView.findViewById(R.id.dotPrioridade);
            holder.layoutBotoes = convertView.findViewById(R.id.layoutBotoesAcao);
            holder.btnArquivar = convertView.findViewById(R.id.btnArquivar);
            holder.btnEliminar = convertView.findViewById(R.id.btnEliminar);
            holder.layoutEnfermeiroInfo = convertView.findViewById(R.id.layoutEnfermeiroInfo);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Triagem t = triagens.get(position);

        // Preencher dados básicos (Usa os métodos auxiliares que já tens na Triagem)
        holder.tvNome.setText(t.getNomePaciente());
        holder.tvSNS.setText("SNS: " + t.getSnsPaciente());

        // Data e Hora
        String dataT = t.getDataTriagem();
        if (dataT != null && dataT.contains(" ")) {
            String[] partes = dataT.split(" ");
            holder.tvData.setText(partes[0]);
            holder.tvHora.setText(partes[1].length() >= 5 ? partes[1].substring(0, 5) : partes[1]);
        } else {
            holder.tvData.setText("--/--");
            holder.tvHora.setText("--:--");
        }

        // Queixa
        holder.tvQueixa.setText("Queixa: " + (t.getQueixaprincipal() != null ? t.getQueixaprincipal() : "---"));

        holder.tvStatus.setText("Concluída");
        holder.tvStatus.setTextColor(Color.parseColor("#1DB954"));

        // Cores da prioridade (CORRIGIDO: usa getPulseira() e getPrioridade())
        int res = R.drawable.circle_gray;

        if (t.getPulseira() != null && t.getPulseira().getPrioridade() != null) {
            String p = t.getPulseira().getPrioridade().toLowerCase();

            if (p.contains("vermelho")) res = R.drawable.circle_red;
            else if (p.contains("laranja")) res = R.drawable.circle_orange;
            else if (p.contains("amarelo") || p.contains("amarela")) res = R.drawable.circle_yellow;
            else if (p.contains("verde")) res = R.drawable.circle_green;
            else if (p.contains("azul")) res = R.drawable.circle_blue;
        }
        holder.dotPrioridade.setBackgroundResource(res);

        //  LÓGICA DOS BOTÕES
        if (isEnfermeiroView) {
            holder.layoutBotoes.setVisibility(View.VISIBLE);
            holder.layoutEnfermeiroInfo.setVisibility(View.VISIBLE);

            String enfNome = "---";
            if (SharedPrefManager.getInstance(context).getEnfermeiro() != null) {
                enfNome = SharedPrefManager.getInstance(context).getEnfermeiro().getUsername();
            }
            holder.tvEnfermeiro.setText("Enf. " + enfNome);

            // Cliques nos Botões
            holder.btnEliminar.setOnClickListener(v -> {
                if (listener != null) listener.onEliminarClick(t.getId());
            });

        } else {
            holder.layoutBotoes.setVisibility(View.GONE);
            holder.layoutEnfermeiroInfo.setVisibility(View.GONE);
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView tvNome, tvSNS, tvStatus, tvData, tvHora, tvQueixa, tvEnfermeiro;
        View dotPrioridade;
        LinearLayout layoutBotoes, layoutEnfermeiroInfo;
        ImageView btnArquivar, btnEliminar;
    }
}