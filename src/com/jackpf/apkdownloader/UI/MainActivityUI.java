package com.jackpf.apkdownloader.UI;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.MenuItem;
import com.jackpf.apkdownloader.Downloader;
import com.jackpf.apkdownloader.Helpers;
import com.jackpf.apkdownloader.R;
import com.jackpf.apkdownloader.Entity.ApkFile;
import com.jackpf.apkdownloader.Exception.AuthenticationException;
import com.jackpf.apkdownloader.Exception.PlayApiException;
import com.jackpf.apkdownloader.Model.UIInterface;

public class MainActivityUI extends UIInterface
{
    private SherlockActivity activity;
    
    private ArrayAdapter<List<ApkFile>> adapter;
    
    private List<ApkFile> downloads = new ArrayList<ApkFile>();
    
    public MainActivityUI(Context context)
    {
        super(context);
        
        activity = (SherlockActivity) context;
    }
    
    public void initialise(Object ...params)
    {
        if (params.length > 0 && params[0] instanceof String) {
            ((EditText) activity.findViewById(R.id.app_id)).setText((String) params[0]);
        }
        
        final ListView downloadsList = (ListView) activity.findViewById(R.id.downloads);
        
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Downloader.DOWNLOAD_DIR);
        if (dir.listFiles() != null) {
            for (File file : dir.listFiles()) {
                ApkFile apk = new ApkFile(context, file);
                if (!downloads.contains(apk)) {
                    downloads.add(apk);
                }
            }
        }
        
        Collections.sort(downloads, new Comparator<ApkFile>() {
            @Override
            public int compare(ApkFile f1, ApkFile f2) {
                return f1.getFile().lastModified() < f2.getFile().lastModified() ? 1 : -1;
            }
        });
        
        if (adapter == null) {
            adapter = new ArrayAdapter<List<ApkFile>>(context, downloads);
            downloadsList.setAdapter(adapter);
            
            downloadsList.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Helpers.installFile(context, ((ApkFile) adapter.getItem(position)).getFile());
                }
            });
            
            /*Helpers.addContextMenu(downloadsList, R.menu._downloads_context_menu, new Helpers.ContextMenuCallback() {
                @Override
                public ActionMode startActionMode(ActionMode.Callback callback) {
                    return activity.startActionMode(callback);
                }
                
                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    File file = (File) adapter.getItem(downloadsList.getCheckedItemPosition());
                    switch (item.getItemId()) {
                        case R.id.action_edit:
                            return true;
                        case R.id.action_delete:
                            return true;
                    }
                    
                    return false;
                }
            });*/
        } else {
            adapter.notifyDataSetChanged();
        }
    }
    
    public void preUpdate()
    {
        
    }
    
    public void update()
    {
        
    }
    
    public void error(Exception e)
    {
        if (e instanceof AuthenticationException) {
            Toast.makeText(context.getApplicationContext(), context.getString(R.string.error_unrecognized_login, e.getMessage()), Toast.LENGTH_LONG).show();
        } else if (e instanceof PlayApiException) {
            Toast.makeText(context.getApplicationContext(), context.getString(R.string.error_api_exception, e.getMessage()), Toast.LENGTH_LONG).show();
        } else {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    private class ArrayAdapter<T extends List<?>> extends BaseAdapter
    {
        private final Context context;
        private final T objects;
        private final LayoutInflater inflater;

        public ArrayAdapter(Context context, T objects) {
            this.context = context;
            this.objects = objects;

            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public Object getItem(int position) {
            return objects.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return objects.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row;

            if (convertView == null) {
                row = inflater.inflate(R.layout._download_item, parent, false);
            } else {
                row = convertView;
            }
            
            ApkFile apk = (ApkFile) getItem(position);
            
            ((TextView) row.findViewById(R.id.package_name)).setText(apk.getFile().getName());
            ((ImageView) row.findViewById(R.id.package_icon)).setImageBitmap(apk.getIcon());
            
            return row;
        }
    }
}
