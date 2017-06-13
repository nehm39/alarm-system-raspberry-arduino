package pl.szymongajewski.system.alarmowy.other;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import pl.szymongajewski.system.alarmowy.R;
import pl.szymongajewski.system.alarmowy.fragment.AlarmsFragment;

/**
 * Created by Szymon Gajewski on 30.12.2015.
 */
public class AlarmHolder extends RecyclerView.ViewHolder {

    protected TextView txtStartDate;
    protected TextView txtEndDate;
    protected ImageButton btnDelete;
    protected int id;
    protected AlarmsFragment alarmsFragment;

    public AlarmHolder(View view, final AlarmsFragment alarmsFragment) {
        super(view);
        this.alarmsFragment = alarmsFragment;
        this.txtStartDate = (TextView) view.findViewById(R.id.txtStartValue);
        this.txtEndDate = (TextView) view.findViewById(R.id.txtEndValue);
        this.btnDelete = (ImageButton) view.findViewById(R.id.btnDeleteAlarm);

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = view.getContext();
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(context.getString(R.string.delete_alarm_dialog_title));
                builder.setMessage(context.getString(R.string.delete_alarm_dialog_msg));
                builder.setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        alarmsFragment.deleteAlarm(id);
                    }
                });

                builder.setNegativeButton(context.getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }
}
