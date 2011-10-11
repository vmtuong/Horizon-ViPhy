package aethers.notebook.core.ui.filechooser;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;

import aethers.notebook.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

public class DirectoryChooser
extends Activity
{
    public static final String EXTRA_RESULT = "EXTRA_RESULT";
    
    private static final String EXTRA_BASE_DIRECTORY = "EXTRA_BASE_DIRECTORY";
    
    private static final FileFilter dirFilter = new FileFilter()
    {
        @Override
        public boolean accept(File pathname) 
        {
            return pathname.isDirectory();
        }
    };
    
    public static Intent createStartIntent(
            Context context,
            String baseDirectory,
            String extraResult)
    {
        Intent i = new Intent(context, DirectoryChooser.class);
        i.putExtra(EXTRA_BASE_DIRECTORY, baseDirectory);
        i.putExtra(EXTRA_RESULT, extraResult);
        return i;
    }
    
    private File baseDirectory;
    
    private volatile File currentDirectory;
    
    private ListView listView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.directorychooser);
        setTitle("Select directory");
        listView = (ListView)findViewById(R.id.dirchooser_listview);
        Intent i = getIntent();
        baseDirectory = Environment.getExternalStorageDirectory();
        currentDirectory = i.hasExtra(EXTRA_BASE_DIRECTORY)
                ? new File(i.getStringExtra(EXTRA_BASE_DIRECTORY))
                : baseDirectory;
        
        listView.setAdapter(new FileListAdapter(this, Arrays.asList(currentDirectory.listFiles(dirFilter))));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(
                    AdapterView<?> parent,
                    View view,
                    int position,
                    long id) 
            {
                File f = (File)listView.getItemAtPosition(position);
                if(f.isDirectory())
                    changeDirectory(f);
                else
                    returnResult(f);
            }
        });
    }

    @Override
    public void onBackPressed()
    {
        if(currentDirectory.equals(baseDirectory))
        {
            setResult(RESULT_CANCELED);
            finish();
        }
        else
            changeDirectory(currentDirectory.getParentFile());
    }
    
    private void changeDirectory(File f)
    {
        currentDirectory = f;
        listView.setAdapter(new FileListAdapter(
                DirectoryChooser.this, Arrays.asList(f.listFiles(dirFilter))));
    }
    
    private void returnResult(File f)
    {
        Intent s = getIntent();
        String rn = s.hasExtra(EXTRA_RESULT) 
                ? s.getStringExtra(EXTRA_RESULT) 
                : EXTRA_RESULT;
        Intent e = new Intent();
        e.putExtra(rn, f.getAbsolutePath());
        setResult(RESULT_OK, e);
        finish();
    }
    
    public void selectDirectory(View v)
    {
        returnResult(currentDirectory);
    }
    
    public void newDirectory(View v)
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Create new directory");
        alert.setMessage("Directory name");
        final EditText input = new EditText(this);
        alert.setView(input);
        alert.setPositiveButton("Create", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which) 
            {
                File d = new File(currentDirectory, input.getText().toString());
                d.mkdir();
                changeDirectory(d);
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which) { }
        });
        alert.show();
    }
}
