package com.example.ruleta;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ruleta.DB.DBmanager;

public class MainActivity extends AppCompatActivity {

    private DBmanager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbManager = new DBmanager(this);

        Button btnInicioSesion = findViewById(R.id.btnInicioSesion);
        btnInicioSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText input = new EditText(MainActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setHint("Nombre de Usuario");

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Iniciar Sesión")
                        .setMessage("Ingresa tu nombre de usuario:")
                        .setView(input)
                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String nombreUsuario = input.getText().toString();
                                if (!nombreUsuario.trim().isEmpty()) {
                                    // Consultar las monedas totales del usuario
                                    int monedasTotales = obtenerMonedasTotalesDelUsuario(nombreUsuario);

                                    // Llamar a verificarEInsertarUsuario con las monedas totales obtenidas
                                    long usuarioId = dbManager.verificarEInsertarUsuario(nombreUsuario, monedasTotales);

                                    // Guarda el nombre en SharedPreferences
                                    guardarNombreUsuarioEnPrefs(nombreUsuario);
                                    // Guarda el ID del usuario en SharedPreferences
                                    guardarIdUsuarioEnPrefs(usuarioId);
                                    // Actualiza las monedas totales en SharedPreferences
                                    guardarMonedasTotalesEnPrefs(monedasTotales);

                                    // Lanza la actividad Menu
                                    Intent intent = new Intent(MainActivity.this, Menu.class);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(MainActivity.this, "Por favor, ingresa un nombre de usuario.", Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            }
        });
    }

    private int obtenerMonedasTotalesDelUsuario(String nombreUsuario) {
        SQLiteDatabase db = dbManager.dbConexion.obtenerDatabase();

        int monedasTotales = 0;

        // Realiza una consulta a la base de datos para obtener las monedas totales del usuario
        Cursor cursor = db.query("Usuario", new String[]{"monedasTotales"}, "nombreUsuario = ?", new String[]{nombreUsuario}, null, null, null);
        if (cursor.moveToFirst()) {
            int monedasTotalesIndex = cursor.getColumnIndex("monedasTotales");
            if (monedasTotalesIndex != -1) {
                monedasTotales = cursor.getInt(monedasTotalesIndex);
            } else {
                // Manejar el caso en que la columna "monedasTotales" no exista
                Log.e("DBconexion", "La columna 'monedasTotales' no fue encontrada en el resultado de la consulta.");
            }
        } else {
            // Manejar el caso en que no se encuentre el usuario
            Log.e("DBconexion", "Usuario no encontrado: " + nombreUsuario);
        }

        cursor.close();
        db.close(); // Cierra la base de datos para liberar recursos

        return monedasTotales;
    }

    private void guardarNombreUsuarioEnPrefs(String nombreUsuario) {
        SharedPreferences prefs = getSharedPreferences("prefsRuleta", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("nombreUsuario", nombreUsuario);
        editor.apply();
    }

    private void guardarIdUsuarioEnPrefs(long usuarioId) {
        SharedPreferences prefs = getSharedPreferences("prefsRuleta", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("usuarioId", usuarioId);
        editor.apply();
    }

    private void guardarMonedasTotalesEnPrefs(int monedasTotales) {
        SharedPreferences prefs = getSharedPreferences("prefsRuleta", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("monedasTotales", monedasTotales);
        editor.apply();
    }
}
