package walden.com.recyclerviewitemderectiontest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView rv = (RecyclerView) findViewById(R.id.rv);
        final RvAdapter adapter = new RvAdapter();
        rv.setAdapter(adapter);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        rv.setLayoutManager(manager);
        rv.addItemDecoration(new TestDividerItemDecoration());


        ItemTouchHelper.Callback callback = new RecycleItemTouchHelper(this,adapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);

        //功能同上, 但上面的实现更优化
        ItemTouchHelper itemTouchHelper1 = new ItemTouchHelper(new ItemTouchHelper.Callback() {

            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {

                return 0;
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                adapter.onMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                adapter.onItemDelete(viewHolder.getAdapterPosition()); //执行滑动时预期的动作
            }
        });
        itemTouchHelper.attachToRecyclerView(rv);


    }

    private class RvAdapter extends RecyclerView.Adapter implements RecycleItemTouchHelper.ItemTouchHelperCallback {
        ArrayList<String> mData = new ArrayList<>();


        RvAdapter() {
            for (int i = 0; i < 100; i++) {
                mData.add("viewHolder" + i);
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = View.inflate(MainActivity.this, R.layout.rv_item, null);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof ViewHolder) {
                ViewHolder holder1 = (ViewHolder) holder;
                holder1.tv.setText(mData.get(position));
                View view = holder1.getView();
                if (position % 2 == 0 && position % 4 != 0) {
                    view.setTag(true);
                } else {
                    view.setTag(false);
                }
            }
        }

        @Override
        public int getItemCount() {
            return 100;
        }

        @Override
        public void onItemDelete(int positon) {
            mData.remove(positon);
            notifyDataSetChanged();
        }

        @Override
        public void onMove(int fromPosition, int toPosition) {
            Collections.swap(mData, fromPosition, toPosition);//拖动交换数据,更改item的位置
            notifyItemMoved(fromPosition, toPosition);
        }
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tv;
        private View view;

        public ViewHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.tv);
            view = itemView;
        }

        public View getView() {
            return view;
        }

    }

    private class TestDividerItemDecoration extends RecyclerView.ItemDecoration {
        private Paint paint;
        private float mOffsetLeft;
        private float mOffsetTop;
        private float mNodeRadius;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher, null);

        TestDividerItemDecoration() {
            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(Color.RED);
            mOffsetLeft = 120;
            mNodeRadius = 30;
        }

        @Override   //针对 item
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            if (parent.getChildAdapterPosition(view) != 0) {
                outRect.top = 1;
                mOffsetTop = 1;
            }
            outRect.left = (int) mOffsetLeft;
        }

        @Override   //针对 整个recycerView  , 遍历整个
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            super.onDraw(c, parent, state);
            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View view = parent.getChildAt(i);
                int index = parent.getChildAdapterPosition(view);

                float dividerTop = view.getTop() - mOffsetTop;
                if (index == 0) { //第一个item .
                    dividerTop = view.getTop();
                }

                float dividerleft = parent.getPaddingLeft();


                float dividerBottom = view.getBottom();  //todo
                float dividerRight = parent.getWidth() - parent.getPaddingRight();

                float centerX = dividerleft + mOffsetLeft / 2;

                float centerY = dividerTop + (dividerBottom - dividerTop) / 2;

                float uplineTopX = centerX;
                float uplineTopY = dividerTop;
                float upLineBottomX = centerX;
                float upLineBottomY = centerY - mNodeRadius;

                c.drawLine(uplineTopX, uplineTopY, upLineBottomX, upLineBottomY, paint);
                paint.setStyle(Paint.Style.STROKE);
                c.drawCircle(centerX, centerY, mNodeRadius, paint);
                paint.setStyle(Paint.Style.FILL);

                float downLineTopX = centerX;
                float downLineTopY = centerY + mNodeRadius;
                float downLineBottomX = centerX;
                float downLineBottomY = dividerBottom;

                c.drawLine(downLineTopX, downLineTopY, downLineBottomX, downLineBottomY, paint);
                //画间隔线
                c.drawRect(dividerleft + mOffsetLeft, dividerTop, dividerRight, view.getTop(), paint);
            }
        }

        @Override
        public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
            super.onDrawOver(c, parent, state);
            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {

                View view = parent.getChildAt(i);
                boolean flag = (boolean) view.getTag();
                int dividerBottom = view.getBottom();
                int dividerTop = view.getTop() - (int) mOffsetTop;

                int centerX = (int) mOffsetLeft / 2;
                int centerY = view.getTop() + (dividerBottom - dividerTop) / 2;
                int index = parent.getChildAdapterPosition(view);
                int top = view.getTop();
                int left = view.getLeft();
                int r = (int) mNodeRadius - 10;
                Rect rect = new Rect(centerX - r, centerY - r, centerX + r, centerY + r);
//                    c.drawBitmap(bitmap, 50, view.getTop(), paint);
                if (flag) {
                    c.drawBitmap(bitmap, null, rect, paint);
                }
            }
        }


    }
}
