package com.example.notepad2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;

public class MainAdapter extends BaseAdapter implements Filterable {


    //원본데이터리스트
    private ArrayList<Note> noteArrayList;
    //필터 데이터 리스트
    private ArrayList<Note> filteredItemList;
    private Filter listFilter;
    private LayoutInflater mInflater;


    public ArrayList<Note> getFilteredItemList() {
        return filteredItemList;
    }


    public MainAdapter(ArrayList<Note> noteList, Context context) {
        noteArrayList = noteList;
        filteredItemList = noteArrayList;
        mInflater = LayoutInflater.from(context);

    }

    @Override
    public Filter getFilter() {
        if (listFilter == null) {
            listFilter = new ListFilter();
        }
        return listFilter;
    }

    public class ViewHolder {
        TextView tv_contents;
        TextView tv_date;
        CheckBox cb_checkbox;
    }

    @Override
    public int getCount() {
        return filteredItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return filteredItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {

        final ViewHolder holder;
        final Note note = filteredItemList.get(position);
        final int checkBoxPosition = position;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_layout, viewGroup, false);
            holder.cb_checkbox = convertView.findViewById(R.id.chk_main_item);
            holder.tv_contents = convertView.findViewById(R.id.contents);
            holder.tv_date = convertView.findViewById(R.id.date);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tv_contents.setText(note.getContents());
        holder.tv_date.setText(note.getDate()); // 데이터 파싱
        holder.cb_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    note.isChecked()
                } else{
                    holder.cb_checkbox.setChecked(false);
                }
            }
        });
        return convertView;
}

private class ListFilter extends Filter {
    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();

        if (constraint == null || constraint == "" || constraint.length() == 0) {
            results.values = noteArrayList;
            results.count = noteArrayList.size();
        } else {
            ArrayList<Note> itemList = new ArrayList<Note>();

            for (Note item : noteArrayList) {
                if (item.getContents().toUpperCase().contains(constraint.toString().toUpperCase())) {
                    itemList.add(item);// 아이템 추가
                }
            }

            results.values = itemList;
            results.count = itemList.size();
        }
        return results;
    }
    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {

        // update listview by filtered data list.
        filteredItemList = (ArrayList<Note>) results.values;

        // notify
        if (results.count > 0) {
            notifyDataSetChanged();
        } else {
            notifyDataSetInvalidated();
        }
    }
}
}

