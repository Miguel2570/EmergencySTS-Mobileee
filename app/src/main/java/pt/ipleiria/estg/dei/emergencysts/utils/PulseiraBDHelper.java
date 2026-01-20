package pt.ipleiria.estg.dei.emergencysts.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import pt.ipleiria.estg.dei.emergencysts.modelo.Pulseira;

public class PulseiraBDHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "emergencysts.db";
    private static final int DB_VERSION = 3;
    private static final String TABLE_PULSEIRA = "pulseiras";

    // --- COLUNAS ---
    private static final String COL_ID = "id";
    private static final String COL_CODIGO = "codigo";
    private static final String COL_PRIORIDADE = "prioridade";
    private static final String COL_STATUS = "status";
    private static final String COL_HORA = "hora";
    private static final String COL_USER_ID = "user_id";
    private static final String COL_NOME = "nome_paciente";
    private static final String COL_SNS = "sns";
    private static final String COL_DATA_NASC = "data_nascimento";
    private static final String COL_TELEFONE = "telefone";
    private static final String COL_MOTIVO = "motivo";
    private static final String COL_QUEIXA = "queixa";
    private static final String COL_DESCRICAO = "descricao";
    private static final String COL_INICIO = "inicio_sintomas";
    private static final String COL_DOR = "dor";
    private static final String COL_ALERGIAS = "alergias";
    private static final String COL_MEDICACAO = "medicacao";

    private static PulseiraBDHelper instance;

    private PulseiraBDHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static synchronized PulseiraBDHelper getInstance(Context context) {
        if (instance == null) {
            instance = new PulseiraBDHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sqlCreate = "CREATE TABLE " + TABLE_PULSEIRA + " (" +
                COL_ID + " INTEGER PRIMARY KEY, " +
                COL_CODIGO + " TEXT, " +
                COL_PRIORIDADE + " TEXT, " +
                COL_STATUS + " TEXT, " +
                COL_HORA + " TEXT, " +
                COL_USER_ID + " INTEGER, " +
                COL_NOME + " TEXT, " +
                COL_SNS + " TEXT, " +
                COL_DATA_NASC + " TEXT, " +
                COL_TELEFONE + " TEXT, " +
                COL_MOTIVO + " TEXT, " +
                COL_QUEIXA + " TEXT, " +
                COL_DESCRICAO + " TEXT, " +
                COL_INICIO + " TEXT, " +
                COL_DOR + " TEXT, " +
                COL_ALERGIAS + " TEXT, " +
                COL_MEDICACAO + " TEXT);";
        db.execSQL(sqlCreate);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PULSEIRA);
        onCreate(db);
    }

    // --- MÉTODOS DE SINCRONIZAÇÃO (NOVO) ---

    public void sincronizarPulseiras(ArrayList<Pulseira> pulseiras) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_PULSEIRA, null, null);
            for (Pulseira p : pulseiras) {
                insertPulseira(db, p);
            }
            db.setTransactionSuccessful(); // Marca como sucesso
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public void adicionarPulseira(Pulseira p) {
        SQLiteDatabase db = this.getWritableDatabase();
        insertPulseira(db, p);
    }

    // Helper privado para inserir usando a base de dados já aberta
    private void insertPulseira(SQLiteDatabase db, Pulseira p) {
        ContentValues values = new ContentValues();
        values.put(COL_ID, p.getId());
        values.put(COL_CODIGO, p.getCodigo());
        values.put(COL_PRIORIDADE, p.getPrioridade());
        values.put(COL_STATUS, p.getStatus());
        values.put(COL_HORA, p.getDataEntrada());
        values.put(COL_USER_ID, p.getUserProfileId());
        values.put(COL_NOME, p.getNomePaciente());
        values.put(COL_SNS, p.getSns());
        values.put(COL_DATA_NASC, p.getDataNascimento());
        values.put(COL_TELEFONE, p.getTelefone());
        values.put(COL_MOTIVO, p.getMotivo());
        values.put(COL_QUEIXA, p.getQueixa());
        values.put(COL_DESCRICAO, p.getDescricao());
        values.put(COL_INICIO, p.getInicioSintomas());
        values.put(COL_DOR, p.getDor());
        values.put(COL_ALERGIAS, p.getAlergias());
        values.put(COL_MEDICACAO, p.getMedicacao());

        db.insertWithOnConflict(TABLE_PULSEIRA, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void removeAllPulseiras() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PULSEIRA, null, null);
    }

    public ArrayList<Pulseira> getAllPulseiras() {
        ArrayList<Pulseira> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PULSEIRA, null);

        if (cursor.moveToFirst()) {
            do {
                Pulseira p = new Pulseira();
                p.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)));
                p.setCodigo(cursor.getString(cursor.getColumnIndexOrThrow(COL_CODIGO)));
                p.setPrioridade(cursor.getString(cursor.getColumnIndexOrThrow(COL_PRIORIDADE)));
                p.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COL_STATUS)));
                p.setDataEntrada(cursor.getString(cursor.getColumnIndexOrThrow(COL_HORA)));
                p.setUserProfileId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID)));
                p.setNomePaciente(cursor.getString(cursor.getColumnIndexOrThrow(COL_NOME)));
                p.setSns(cursor.getString(cursor.getColumnIndexOrThrow(COL_SNS)));
                p.setDataNascimento(cursor.getString(cursor.getColumnIndexOrThrow(COL_DATA_NASC)));
                p.setTelefone(cursor.getString(cursor.getColumnIndexOrThrow(COL_TELEFONE)));
                p.setMotivo(cursor.getString(cursor.getColumnIndexOrThrow(COL_MOTIVO)));
                p.setQueixa(cursor.getString(cursor.getColumnIndexOrThrow(COL_QUEIXA)));
                p.setDescricao(cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRICAO)));
                p.setInicioSintomas(cursor.getString(cursor.getColumnIndexOrThrow(COL_INICIO)));
                p.setDor(cursor.getString(cursor.getColumnIndexOrThrow(COL_DOR)));
                p.setAlergias(cursor.getString(cursor.getColumnIndexOrThrow(COL_ALERGIAS)));
                p.setMedicacao(cursor.getString(cursor.getColumnIndexOrThrow(COL_MEDICACAO)));

                lista.add(p);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }
}