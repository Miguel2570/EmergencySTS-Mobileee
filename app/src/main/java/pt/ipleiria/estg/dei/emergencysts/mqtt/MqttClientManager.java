package pt.ipleiria.estg.dei.emergencysts.mqtt;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONException;
import org.json.JSONObject;

import pt.ipleiria.estg.dei.emergencysts.R;
import pt.ipleiria.estg.dei.emergencysts.activities.comum.HistoricoActivity;
import pt.ipleiria.estg.dei.emergencysts.activities.comum.MostrarPulseirasActivity;
import pt.ipleiria.estg.dei.emergencysts.activities.enfermeiro.EnfermeiroActivity;
import pt.ipleiria.estg.dei.emergencysts.utils.SharedPrefManager;

public class MqttClientManager {

    private static final String TAG = "MqttClientManager";
    private static final String CHANNEL_ID = "emergency_channel_id_v3";
    private static MqttClientManager instance;
    private MqttClient client;
    private Context context;

    // Variável para impedir múltiplas tentativas ao mesmo tempo
    private boolean isConnecting = false;

    private MqttClientManager(Context context) {
        this.context = context.getApplicationContext();
        createNotificationChannel();
    }

    public static synchronized MqttClientManager getInstance(Context context) {
        if (instance == null) {
            instance = new MqttClientManager(context);
        }
        return instance;
    }

    public void subscribe(String topic) {
        if (client != null && client.isConnected()) {
            try {
                client.subscribe(topic, 1);
                Log.d(TAG, "Subscrito com sucesso: " + topic);
            } catch (MqttException e) {
                Log.e(TAG, "Erro ao subscrever tópico: " + topic, e);
            }
        }
    }

    public void connect() {
        if (!SharedPrefManager.getInstance(context).isLoggedIn()) {
            return;
        }

        // 1. SE JÁ ESTIVER CONECTADO, NÃO FAZ NADA
        if (client != null && client.isConnected()) {
            // Log.d(TAG, "Cliente já conectado.");
            return;
        }

        // 2. SE JÁ ESTIVER A TENTAR CONECTAR, NÃO FAZ NADA (Evita o erro dos logs!)
        if (isConnecting) {
            Log.d(TAG, "Conexão em progresso... aguardando.");
            return;
        }

        isConnecting = true; // Marca que começou a tentar

        String serverIp = SharedPrefManager.getInstance(context).getServerBase();
        String cleanIp = serverIp.replace("http://", "").replace("https://", "").replace("/", "");

        if(cleanIp.contains(":")) {
            cleanIp = cleanIp.split(":")[0];
        }

        String brokerUrl = "tcp://" + cleanIp + ":1883";
        Log.d(TAG, "Conectando ao Broker: " + brokerUrl);

        try {
            // ID único com timestamp para garantir unicidade
            String clientId;
            if (SharedPrefManager.getInstance(context).getEnfermeiroBase() != null) {
                clientId = "Android_Enf_" + SharedPrefManager.getInstance(context).getEnfermeiroBase().getId()
                        + "_" + System.currentTimeMillis();
            } else if (SharedPrefManager.getInstance(context).getPacienteBase() != null) {
                clientId = "Android_Pac_" + SharedPrefManager.getInstance(context).getPacienteBase().getId()
                        + "_" + System.currentTimeMillis();
            } else {
                clientId = MqttClient.generateClientId() + "_" + System.currentTimeMillis();
            }

            client = new MqttClient(brokerUrl, clientId, new MemoryPersistence());

            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName("emergencysts");
            options.setPassword("i%POZsi02Kmc".toCharArray());
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);

            client.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    isConnecting = false; // SUCESSO: Liberta a flag
                    Log.d(TAG, "Conectado ao MQTT!");
                    subscribeToTopics();
                }

                @Override
                public void connectionLost(Throwable cause) {
                    isConnecting = false; // FALHA: Liberta a flag para tentar de novo
                    Log.e(TAG, "Conexão perdida.");
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    String payload = new String(message.getPayload());
                    Log.d(TAG, "Mensagem MQTT: " + topic + " -> " + payload);

                    Intent broadcastIntent = new Intent("MQTT_MESSAGE");
                    broadcastIntent.putExtra("topic", topic);
                    broadcastIntent.putExtra("payload", payload);
                    context.sendBroadcast(broadcastIntent);

                    try {
                        JSONObject jsonObject = new JSONObject(payload);

                        if (!jsonObject.has("titulo") || !jsonObject.has("mensagem")) {
                            return;
                        }

                        String titulo = jsonObject.getString("titulo");
                        String mensagem = jsonObject.getString("mensagem");

                        Intent intent;
                        if (mensagem.toLowerCase().contains("atualizada") || titulo.toLowerCase().contains("concluida")) {
                            intent = new Intent(context, HistoricoActivity.class);
                        } else if (titulo.toLowerCase().contains("nova") || mensagem.toLowerCase().contains("criada")) {
                            intent = new Intent(context, MostrarPulseirasActivity.class);
                        } else {
                            intent = new Intent(context, EnfermeiroActivity.class);
                        }

                        showNotification(titulo, mensagem, intent);

                    } catch (JSONException e) {
                        Log.e(TAG, "Erro JSON: " + e.getMessage());
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {}
            });

            // Inicia conexão em background
            new Thread(() -> {
                try {
                    client.connect(options);
                } catch (MqttException e) {
                    isConnecting = false; // ERRO: Liberta a flag
                    Log.e(TAG, "Erro ao conectar: " + e.getMessage());
                    e.printStackTrace();
                }
            }).start();

        } catch (MqttException e) {
            isConnecting = false;
            e.printStackTrace();
        }
    }

    private void subscribeToTopics() {
        if (client == null || !client.isConnected()) return;

        SharedPrefManager spm = SharedPrefManager.getInstance(context);

        if (spm.getPacienteBase() != null && spm.getPacienteBase().getId() != -1) {
            int pid = spm.getPacienteBase().getId();
            subscribe("pulseira/criada/" + pid);
            subscribe("pulseira/atualizada/" + pid);
            Log.d(TAG, "Subscrito como PACIENTE");
        }

        if (spm.getEnfermeiroBase() != null && spm.getEnfermeiroBase().getId() != -1) {
            subscribe("emergencysts/triagem");
            Log.d(TAG, "Subscrito como ENFERMEIRO em: emergencysts/triagem");
        }
    }

    private void showNotification(String title, String messageBody, Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        int color = 0xFF009E4D;
        try {
            color = androidx.core.content.ContextCompat.getColor(context, R.color.green_700);
        } catch (Exception e) {}

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)
                .setColor(color)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        } catch (SecurityException e) {
            Log.e(TAG, "Falta permissão POST_NOTIFICATIONS");
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Notificações Emergência";
            String description = "Alertas de triagem e pulseiras";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableVibration(true);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    public void disconnect() {
        new Thread(() -> {
            try {
                if (client != null && client.isConnected()) {
                    client.disconnect();
                    Log.d(TAG, "Desconectado.");
                }
            } catch (MqttException e) {
                Log.e(TAG, "Erro ao desconectar", e);
            }
        }).start();
    }
}