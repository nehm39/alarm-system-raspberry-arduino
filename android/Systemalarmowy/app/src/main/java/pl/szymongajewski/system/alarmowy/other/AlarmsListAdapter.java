package pl.szymongajewski.system.alarmowy.other;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import pl.szymongajewski.system.alarmowy.R;
import pl.szymongajewski.system.alarmowy.fragment.AlarmsFragment;
import pl.szymongajewski.system.alarmowy.fragment.AlarmsFragment_;
import pl.szymongajewski.system.alarmowy.model.Alarm;

/**
 * Created by Szymon Gajewski on 30.12.2015.
 */
public class AlarmsListAdapter extends RecyclerView.Adapter<AlarmHolder> {
    private List<Alarm> alarmsList;
    private AlarmsFragment alarmsFragment;

    public AlarmsListAdapter(List<Alarm> alarmsList, AlarmsFragment alarmsFragment) {
        this.alarmsList = alarmsList;
        this.alarmsFragment = alarmsFragment;
    }

    @Override
    public AlarmHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.alarms_row, parent, false);
        return new AlarmHolder(view, alarmsFragment);
    }

    @Override
    public void onBindViewHolder(AlarmHolder holder, int position) {
        Alarm alarm = alarmsList.get(position);
        holder.id = alarm.getId();
        holder.txtStartDate.setText(alarm.getStartDate());
        holder.txtEndDate.setText(alarm.getEndDate());
    }

    @Override
    public int getItemCount() {
        return alarmsList.size();
    }
}
