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

// IMPORTANTE: Troca para as bibliotecas do Android Service
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
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

    // ALTERAÇÃO: Usar MqttAndroidClient em vez de MqttClient
    private MqttAndroidClient client;
    private Context context;

    private boolean isConnecting = false;

    private MqttClientManager(Context context) {
        // Usar getApplicationContext evita memory leaks
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
                // A subscrição no AndroidClient também é assíncrona, mas o QoS 1 garante a entrega
                client.subscribe(topic, 1, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.d(TAG, "Subscrito com sucesso: " + topic);
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.e(TAG, "Falha ao subscrever: " + topic);
                    }
                });
            } catch (MqttException e) {
                Log.e(TAG, "Erro ao subscrever tópico: " + topic, e);
            }
        }
    }

    public void connect() {
        if (!SharedPrefManager.getInstance(context).isLoggedIn()) {
            return;
        }

        if (client != null && client.isConnected()) {
            return;
        }

        if (isConnecting) {
            Log.d(TAG, "Conexão em progresso... aguardando.");
            return;
        }

        isConnecting = true;

        String serverIp = SharedPrefManager.getInstance(context).getServerBase();
        String cleanIp = serverIp.replace("http://", "").replace("https://", "").replace("/", "");

        if(cleanIp.contains(":")) {
            cleanIp = cleanIp.split(":")[0];
        }

        String brokerUrl = "tcp://" + cleanIp + ":1883";
        Log.d(TAG, "Conectando ao Broker: " + brokerUrl);

        try {
            String clientId;
            if (SharedPrefManager.getInstance(context).getEnfermeiroBase() != null) {
                clientId = "Android_Enf_" + SharedPrefManager.getInstance(context).getEnfermeiroBase().getId()
                        + "_" + System.currentTimeMillis();
            } else if (SharedPrefManager.getInstance(context).getPacienteBase() != null) {
                clientId = "Android_Pac_" + SharedPrefManager.getInstance(context).getPacienteBase().getId()
                        + "_" + System.currentTimeMillis();
            } else {
                clientId = "Android_Guest_" + System.currentTimeMillis();
            }

            // ALTERAÇÃO: Instanciar o MqttAndroidClient
            client = new MqttAndroidClient(context, brokerUrl, clientId);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName("emergencysts");
            options.setPassword("i%POZsi02Kmc".toCharArray());
            options.setAutomaticReconnect(true);
            options.setCleanSession(true); // Se quiseres receber msgs perdidas, muda para false
            options.setConnectionTimeout(10);

            // Callback para mensagens recebidas (Funciona igual ao anterior)
            client.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    // Nota: No MqttAndroidClient, a lógica de sucesso inicial deve ir no IMqttActionListener abaixo,
                    // mas este método é ótimo para quando o 'AutomaticReconnect' recupera a rede sozinho.
                    if (reconnect) {
                        Log.d(TAG, "Reconectado automaticamente ao MQTT!");
                        subscribeToTopics();
                    }
                }

                @Override
                public void connectionLost(Throwable cause) {
                    Log.e(TAG, "Conexão perdida.");
                    // O AutomaticReconnect tentará reconectar sozinho
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    // Lógica de receção de mensagem mantém-se igual
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

            // ALTERAÇÃO: O connect agora recebe um Listener para sucesso/falha
            client.connect(options, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    isConnecting = false;
                    Log.d(TAG, "Conectado com sucesso (Callback)!");
                    subscribeToTopics();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    isConnecting = false;
                    Log.e(TAG, "Falha ao conectar: " + exception.getMessage());
                }
            });

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
        try {
            if (client != null && client.isConnected()) {
                client.disconnect();
                Log.d(TAG, "Desconectado.");
            }
        } catch (MqttException e) {
            Log.e(TAG, "Erro ao desconectar", e);
        }
    }
}