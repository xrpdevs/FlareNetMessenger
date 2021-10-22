package uk.co.xrpdevs.flarenetmessenger;

import static uk.co.xrpdevs.flarenetmessenger.Utils.myLog;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class ContextMenuRecyclerView extends RecyclerView {

    public RecyclerViewContextMenuInfo mContextMenuInfo;


    public ContextMenuRecyclerView(@NonNull Context context) {
        super(context, null);
    }


    public ContextMenuRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, R.attr.recyclerViewStyle);
    }

    public ContextMenuRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    // public ContextMenuRecyclerView(@NonNull Context context,  AttributeSet as) {
    //     super(context, as);
    // }


    @Override
    public ContextMenu.ContextMenuInfo getContextMenuInfo() {
        return mContextMenuInfo;
    }

    public ContextMenu.ContextMenuInfo _getContextMenuInfo() {
        return mContextMenuInfo;
    }

    @Override
    public boolean showContextMenuForChild(View originalView) {
        final int longPressPosition = getChildAdapterPosition(originalView);
        final int bPosition = getChildPosition(originalView);
        final int cPosition = getChildAdapterPosition(originalView);
        myLog("MENU:", "CHAD " + longPressPosition);
        TransactionsActivity.thepos = longPressPosition;

        if (longPressPosition >= 0) {
            final long longPressId = getAdapter().getItemId(bPosition);
            TransactionsActivity.theID = longPressId;
            myLog("MENU:", " posa: " + longPressPosition + " posb: " + bPosition + " posc: " + cPosition + " id: " + longPressId);
            mContextMenuInfo = new RecyclerViewContextMenuInfo(longPressPosition, longPressId);
            return super.showContextMenuForChild(originalView);
        }
        return false;
    }


    public static class RecyclerViewContextMenuInfo implements ContextMenu.ContextMenuInfo {

        public RecyclerViewContextMenuInfo(int position, long id) {
            this.position = position;
            this.id = id;
        }

        final public int position;
        final public long id;
    }
}