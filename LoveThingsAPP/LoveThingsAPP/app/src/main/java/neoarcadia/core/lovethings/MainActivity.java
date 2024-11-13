package neoarcadia.core.lovethings;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView roleMessageTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        roleMessageTextView = findViewById(R.id.roleMessageTextView);

        // Obtiene el mensaje del rol desde el intent
        String roleMessage = getIntent().getStringExtra("ROLE_MESSAGE");

        // Muestra el mensaje del rol
        if (roleMessage != null) {
            roleMessageTextView.setText(roleMessage);
        } else {
            roleMessageTextView.setText("No se pudo obtener el rol del usuario.");
        }
    }
}
