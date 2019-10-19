package kz.mobile.todoapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class CreateToDoActivity extends AppCompatActivity implements OnTaskCompleted {

    private EditText inputName;
    private EditText inputDescription;
    private ImageButton buttonCreate;
    private ImageButton buttonSave;
    private ImageButton buttonShare;
    private ToDo toDo;

    private String text;

    private CreateToDoAsync createToDoAsync;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_to_do);
        initViews();
        setToDoData();
    }

    private void initViews() {
        inputName = findViewById(R.id.inputName);
        inputDescription = findViewById(R.id.inputDescription);
        buttonCreate = findViewById(R.id.buttonCreate);
        buttonSave = findViewById(R.id.buttonSave);
        buttonShare = findViewById(R.id.buttonShare);

        buttonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = inputName.getText().toString();
                String description = inputDescription.getText().toString();
                createToDo(name, description);
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createToDoAsync = new CreateToDoAsync();
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(writeToExternalFileGranted()){
                        createToDoAsync.execute(toDo);
                    } else {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
                    }
                } else {
                    createToDoAsync.execute(toDo);
                }
            }
        });

        buttonShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File iconsStoragePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/external file/" + toDo.getName() +  ".txt");
                final String selpath = iconsStoragePath.getAbsolutePath();
                Intent intent = new Intent(Intent.ACTION_SEND);
                Uri selectedUri = Uri.parse(selpath);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_STREAM, selectedUri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(intent, "Share File"));
            }
        });
    }

    private boolean writeToExternalFileGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                createToDoAsync.execute(toDo);
            }
        }
    }

    private void setToDoData() {
        if (getIntent().hasExtra(Constants.TODO)) {
            //Todo check ToDo entity for null
            toDo = (ToDo)getIntent().getSerializableExtra(Constants.TODO);
            inputName.setText(toDo.getName());
            inputDescription.setText(toDo.getDescription());
        }
    }

    private void createToDo(String name, String description) {
        //TODO check strings for null and empty values
        if (toDo == null) {
            toDo = new ToDo(name, description);
        } else {
            toDo.setName(name);
            toDo.setDescription(description);
        }
        new CreateToDoAsync(this).execute(toDo);
    }

    @Override
    public void itemCreated(ToDo toDo) {
        Intent intent = new Intent();
        intent.putExtra(Constants.TODO, toDo);
        intent.putExtra("position", toDo.getId());
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private class CreateToDoAsync extends AsyncTask<ToDo, Void, ToDo> {

        private OnTaskCompleted listener;

        CreateToDoAsync(OnTaskCompleted listener) {
            this.listener = listener;
        }

        public CreateToDoAsync() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ToDo doInBackground(ToDo... voids) {
            //Todo save object to file
            if(voids[0] != null){
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "external file");
                if(!file.exists()){
                    file.mkdir();
                }
                FileUtils.saveToFile(file, CreateToDoActivity.this, voids[0].getName() + "\n" +voids[0].getDescription() + "\n" + voids[0].getDate(), voids[0].getName()+".txt");
            }
            return voids[0];
        }

        @Override
        protected void onPostExecute(ToDo item) {
            super.onPostExecute(item);
            if (listener != null) {
                listener.itemCreated(item);
            }
        }
    }
}
