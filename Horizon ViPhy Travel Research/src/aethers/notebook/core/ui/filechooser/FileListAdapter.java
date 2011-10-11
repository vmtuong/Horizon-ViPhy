package aethers.notebook.core.ui.filechooser;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import aethers.notebook.R;
import android.app.Activity;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

public class FileListAdapter
implements ListAdapter
{
    private final Activity activity;
 
    private final List<File> files;
    
    public FileListAdapter(Activity activity, List<File> files)
    {
        this.activity = activity;
        ArrayList<File> ordered = new ArrayList<File>();
        ordered.addAll(files);
        Collections.sort(ordered, new Comparator<File>()
        {
            @Override
            public int compare(File a, File b) 
            {
                if(a.isDirectory() && b.isDirectory())
                    return a.getName().compareTo(b.getName());
                if(a.isFile() && b.isFile())
                    return a.getName().compareTo(b.getName());
                if(a.isDirectory() && b.isFile())
                    return -1;
                return 1;
            }
        });
        this.files = ordered;        
    }
    
    @Override
    public int getCount() 
    {
        return files.size();
    }

    @Override
    public Object getItem(int position) 
    {
        return files.get(position);
    }

    @Override
    public long getItemId(int position) 
    {
        return files.get(position).hashCode();
    }

    @Override
    public int getItemViewType(int position) 
    {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) 
    {
        File f = files.get(position);
        LinearLayout l = (LinearLayout)activity.getLayoutInflater().inflate(
                R.layout.filechooseritem, null);
        ImageView i = (ImageView)l.findViewById(R.id.filechooseritem_icon);
        i.setImageResource(f.isDirectory() ? R.drawable.folder : R.drawable.file);
        TextView v = (TextView)l.findViewById(R.id.filechooseritem_text);
        v.setText(f.getName());
        return l;
    }

    @Override
    public int getViewTypeCount() 
    {
        return 1;
    }

    @Override
    public boolean hasStableIds() 
    {
        return true;
    }

    @Override
    public boolean isEmpty()
    {
        return files.isEmpty();
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) { }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) { }

    @Override
    public boolean areAllItemsEnabled() 
    {
        return true;
    }

    @Override
    public boolean isEnabled(int position) 
    {
        return true;
    }
}