package pt.ipleiria.estg.dei.emergencysts.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import pt.ipleiria.estg.dei.emergencysts.R;
import pt.ipleiria.estg.dei.emergencysts.listeners.PulseiraListener;
import pt.ipleiria.estg.dei.emergencysts.modelo.Pulseira;

public class PulseiraAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Pulseira> pulseiras;
    private LayoutInflater inflater;
    private PulseiraListener listener;

    public PulseiraAdapter(Context context, ArrayList<Pulseira> pulseiras, PulseiraListener listener) {
        this.context = context;
        this.pulseiras = pulseiras;
        this.inflater = LayoutInflater.from(context);
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return pulseiras.size();
    }

    @Override
    public Object getItem(int position) {
        return pulseiras.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_pulseira, parent, false);
            holder = new ViewHolder();
            holder.tvNome = convertView.findViewById(R.id.tvNome);
            holder.tvSNS = convertView.findViewById(R.id.tvSNS);
            holder.tvHora = convertView.findViewById(R.id.tvHora);
            holder.tvStatus = convertView.findViewById(R.id.tvStatus);
            holder.imgDot = convertView.findViewById(R.id.imgDot);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Pulseira pulseira = pulseiras.get(position);

        holder.tvNome.setText(pulseira.getNomePaciente());

        holder.tvSNS.setText("SNS: " + (pulseira.getSns() != null ? pulseira.getSns() : "---"));

        holder.tvHora.setText(pulseira.getDataEntrada() != null ? pulseira.getDataEntrada() : "--:--");

        // STATUS E CORES
        String prioridade = pulseira.getPrioridade();
        if (prioridade == null) prioridade = "Pendente";

        holder.tvStatus.setText(prioridade);

        // Lógica de cores (Igual à que fizemos na Activity do Paciente)
        switch (prioridade.toLowerCase()) {
            case "vermelho":
                holder.tvStatus.setBackgroundResource(R.drawable.circle_red);
                holder.tvStatus.setTextColor(Color.WHITE);
                holder.imgDot.setColorFilter(Color.RED);
                break;
            case "laranja":
                holder.tvStatus.setBackgroundResource(R.drawable.circle_orange);
                holder.tvStatus.setTextColor(Color.WHITE);
                holder.imgDot.setColorFilter(Color.parseColor("#FF9800"));
                break;
            case "amarelo":
                holder.tvStatus.setBackgroundResource(R.drawable.circle_yellow);
                holder.tvStatus.setTextColor(Color.BLACK);
                holder.imgDot.setColorFilter(Color.YELLOW);
                break;
            case "verde":
                holder.tvStatus.setBackgroundResource(R.drawable.circle_green);
                holder.tvStatus.setTextColor(Color.WHITE);
                holder.imgDot.setColorFilter(Color.GREEN);
                break;
            case "azul":
                holder.tvStatus.setBackgroundResource(R.drawable.circle_blue);
                holder.tvStatus.setTextColor(Color.WHITE);
                holder.imgDot.setColorFilter(Color.BLUE);
                break;
            default:
                // Pendente (Cinza/Laranja default)
                holder.tvStatus.setBackgroundResource(R.drawable.bg_chip_pendente);
                holder.tvStatus.setTextColor(Color.parseColor("#D84315"));
                holder.imgDot.setColorFilter(Color.GRAY);
                break;
        }

        convertView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPulseiraClick(pulseira);
            }
        });

        return convertView;
    }

    private static class ViewHolder {
        TextView tvNome;
        TextView tvSNS;
        TextView tvHora;
        TextView tvStatus;
        ImageView imgDot;
    }
}