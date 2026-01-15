package pt.ipleiria.estg.dei.emergencysts.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import pt.ipleiria.estg.dei.emergencysts.activities.auth.ConfigActivity;
import pt.ipleiria.estg.dei.emergencysts.activities.enfermeiro.EnfermeiroActivity;
import pt.ipleiria.estg.dei.emergencysts.activities.auth.LoginActivity;
import pt.ipleiria.estg.dei.emergencysts.activities.paciente.PacienteActivity;
import pt.ipleiria.estg.dei.emergencysts.modelo.Enfermeiro;
import pt.ipleiria.estg.dei.emergencysts.modelo.Paciente;

public class SharedPrefManager {

    private static final String PREF_NAME = "emergencysts_pref";

    // LOGIN BASE
    private static final String KEY_ACCESS_TOKEN = "key_access_token";
    private static final String KEY_ID = "key_id";
    private static final String KEY_USERNAME = "key_username";
    private static final String KEY_EMAIL = "key_email";
    private static final String KEY_ROLE = "key_role";

    // PERFIL PACIENTE
    private static final String KEY_PAC_ID = "pac_id";
    private static final String KEY_PAC_NOME = "pac_nome";
    private static final String KEY_PAC_EMAIL = "pac_email";
    private static final String KEY_PAC_NASC = "pac_nasc";
    private static final String KEY_PAC_TEL = "pac_tel";
    private static final String KEY_PAC_SNS = "pac_sns";
    private static final String KEY_PAC_GENERO = "pac_genero";
    private static final String KEY_PAC_NIF = "pac_nif";
    private static final String KEY_PAC_MORADA = "pac_morada";

    // PERFIL ENFERMEIRO
    private static final String KEY_ENF_NOME = "enf_nome";
    private static final String KEY_ENF_EMAIL = "enf_email";
    private static final String KEY_ENF_NASC = "enf_nasc";
    private static final String KEY_ENF_TEL = "enf_tel";
    private static final String KEY_ENF_SNS = "enf_sns";
    private static final String KEY_ENF_NIF = "enf_nif";
    private static final String KEY_ENF_MORADA = "enf_morada";

    // CONFIG SERVIDOR
    private static final String KEY_SERVER_BASE = "key_server_base";
    private static final String KEY_API_PATH = "key_api_path";

    private static SharedPrefManager instance;
    private static Context ctx;

    private SharedPrefManager(Context context) {
        ctx = context;
    }

    public static synchronized SharedPrefManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefManager(context.getApplicationContext());
        }
        return instance;
    }

    // LOGIN
    public void userLogin(Enfermeiro user, String accessToken) {
        SharedPreferences.Editor e = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();

        e.putInt(KEY_ID, user.getId());
        e.putString(KEY_USERNAME, user.getUsername());
        e.putString(KEY_EMAIL, user.getEmail());
        e.putString(KEY_ROLE, user.getRole());
        e.putString(KEY_ACCESS_TOKEN, accessToken);

        e.apply();
    }

    public boolean isLoggedIn() {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_ACCESS_TOKEN, null) != null;
    }

    // Enfermeiro BASE (dados do login)
    public Enfermeiro getEnfermeiroBase() {
        SharedPreferences sp = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        return new Enfermeiro(
                sp.getInt(KEY_ID, -1),
                sp.getString(KEY_USERNAME, null),
                sp.getString(KEY_EMAIL, null),
                sp.getString(KEY_ROLE, null)
        );
    }

    // Enfermeiro COMPLETO (login + perfil)
    public Enfermeiro getEnfermeiro() {
        SharedPreferences sp = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Enfermeiro base = getEnfermeiroBase();

        return new Enfermeiro(
                base.getId(),
                base.getUsername(),
                sp.getString(KEY_ENF_EMAIL, base.getEmail()),
                base.getRole(),
                sp.getString(KEY_ENF_NOME, "---"),
                sp.getString(KEY_ENF_NASC, "---"),
                sp.getString(KEY_ENF_TEL, "---"),
                sp.getString(KEY_ENF_SNS, "---"),
                sp.getString(KEY_ENF_NIF, "---"),
                sp.getString(KEY_ENF_MORADA, "---")
        );
    }

    public void saveEnfermeiro(Enfermeiro e) {
        SharedPreferences.Editor ed = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();

        ed.putString(KEY_ENF_NOME, e.getNome());
        ed.putString(KEY_ENF_EMAIL, e.getEmail());
        ed.putString(KEY_ENF_NASC, e.getDataNascimento());
        ed.putString(KEY_ENF_TEL, e.getTelefone());
        ed.putString(KEY_ENF_SNS, e.getSns());
        ed.putString(KEY_ENF_NIF, e.getNif());
        ed.putString(KEY_ENF_MORADA, e.getMorada());

        ed.apply();
    }

    // --- PACIENTE BASE (login) ---
    public Paciente getPacienteBase() {
        SharedPreferences sp = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int id = sp.getInt(KEY_ID, -1);

        // Se não houver ID de login, não há paciente logado
        if (id == -1) return null;

        return new Paciente(
                id,
                sp.getString(KEY_USERNAME, null),
                sp.getString(KEY_EMAIL, null),
                sp.getString(KEY_ROLE, null)
        );
    }

    // --- PACIENTE COMPLETO (Perfil) ---
    public Paciente getPaciente() {
        SharedPreferences sp = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Paciente base = getPacienteBase();

        if (base == null) return null;

        return new Paciente(
                base.getId(),
                base.getUsername(),
                sp.getString(KEY_PAC_EMAIL, base.getEmail()),
                base.getRole(),
                sp.getString(KEY_PAC_NOME, "---"),
                sp.getString(KEY_PAC_NASC, "---"),
                sp.getString(KEY_PAC_GENERO, "M"),
                sp.getString(KEY_PAC_TEL, "---"),
                sp.getString(KEY_PAC_SNS, "---"),
                sp.getString(KEY_PAC_NIF, "---"),
                sp.getString(KEY_PAC_MORADA, "---")
        );
    }

    public void savePaciente(Paciente p) {
        SharedPreferences.Editor ed = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();

        // Guardamos os dados de perfil
        ed.putString(KEY_PAC_NOME, p.getNome());
        ed.putString(KEY_PAC_EMAIL, p.getEmail());
        ed.putString(KEY_PAC_NASC, p.getDataNascimento());
        ed.putString(KEY_PAC_GENERO, p.getGenero());
        ed.putString(KEY_PAC_TEL, p.getTelefone());
        ed.putString(KEY_PAC_SNS, p.getSns());
        ed.putString(KEY_PAC_NIF, p.getNif());
        ed.putString(KEY_PAC_MORADA, p.getMorada());

        ed.apply();
    }

    // TOKEN
    public String getKeyAccessToken() {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_ACCESS_TOKEN, null);
    }

    // LOGOUT
    public void logout() {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();

        Intent intent = new Intent(ctx, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        ctx.startActivity(intent);
    }

    // CONFIG DO SERVIDOR
    public void setServerBase(String baseUrl) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_SERVER_BASE, baseUrl)
                .apply();
    }

    public void setApiPath(String path) {
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_API_PATH, path)
                .apply();
    }

    public String getServerBase() {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_SERVER_BASE, "http://172.22.21.215");
    }

    public String getApiPath() {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_API_PATH, "/EmergencySTS/advanced/backend/web/");
    }

    public String getServerUrl() {
        return getServerBase() + getApiPath();
    }

    public boolean hasServerConfigured() {
        return getServerBase() != null && !getServerBase().isEmpty();
    }

    // NAVEGAÇÃO INICIAL
    public void navigateOnStart(Context context) {

        // Verifica configurações
        if (!hasServerConfigured()) {
            Intent i = new Intent(context, ConfigActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(i);
            return;
        }

        //  Verifica se tem login
        if (!isLoggedIn()) {
            Intent i = new Intent(context, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(i);
            return;
        }

        //  Verifica a Role para encaminhar
        Enfermeiro u = getEnfermeiro();
        String role = u.getRole();

        if (role == null) role = "";

        Intent next;

        switch (role.toLowerCase()) {
            case "paciente":
            case "utente":
                next = new Intent(context, PacienteActivity.class);
                break;

            case "enfermeiro":
            case "medico":
            case "admin":
            case "administrador":
                next = new Intent(context, EnfermeiroActivity.class);
                break;

            default:
                next = new Intent(context, EnfermeiroActivity.class);
                break;
        }

        next.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(next);
    }

    public String getKeyRole() {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_ROLE, null);
    }


    //Tema escuro
    private static final String KEY_THEME = "key_theme";

    public void saveTheme(int mode) {
        SharedPreferences.Editor ed = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
        ed.putInt(KEY_THEME, mode);
        ed.apply();
    }

    public int getTheme() {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getInt(KEY_THEME, androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

}