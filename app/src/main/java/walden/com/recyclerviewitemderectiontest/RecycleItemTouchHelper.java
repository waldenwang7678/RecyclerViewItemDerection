package walden.com.recyclerviewitemderectiontest;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

/**
 * Created by Administrator on 2017/5/27 0027.
 */

public class RecycleItemTouchHelper extends ItemTouchHelper.Callback {

    private static final String TAG = "RecycleItemTouchHelper";
    private final ItemTouchHelperCallback helperCallback;
    private Context mContext;

    public RecycleItemTouchHelper(Context context, ItemTouchHelperCallback helperCallback) {
        this.helperCallback = helperCallback;
        mContext = context;
    }

    @Override           //用来指定拖拽和滑动在哪个方向是被允许的
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        //return 0;

//        ItemTouchHelper.UP  //滑动拖拽向上方向
//        ItemTouchHelper.DOWN//向下
//        ItemTouchHelper.LEFT//向左
//        ItemTouchHelper.RIGHT//向右
//        ItemTouchHelper.START//依赖布局方向的水平开始方向
//        ItemTouchHelper.END//依赖布局方向的水平结束方向
        return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.END);
    }

    /**
     * @param recyclerView
     * @param viewHolder   拖拽的item
     * @param target       目标位置的item
     * @return
     */
    @Override  //拖拽
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        helperCallback.onMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());  //拖拽时执行的动作
        return false;
    }

    @Override   //滑动
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        helperCallback.onItemDelete(viewHolder.getAdapterPosition()); //执行滑动时预期的动作
    }

    @Override
    public boolean isLongPressDragEnabled() {   //长按拖动
        return super.isLongPressDragEnabled();   //true

    }

    @Override    //是够支持滑动
    public boolean isItemViewSwipeEnabled() {
        return super.isItemViewSwipeEnabled();  //true
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        if (actionState==ItemTouchHelper.ACTION_STATE_SWIPE){

            //dX大于0时向右滑动，小于0向左滑动
            View itemView=viewHolder.itemView;//获取滑动的view
            Resources resources= mContext.getResources();
            Bitmap bitmap= BitmapFactory.decodeResource(resources, R.mipmap.delete);//获取删除指示的背景图片
            int padding =10;//图片绘制的padding
            int maxDrawWidth=2*padding+bitmap.getWidth();//最大的绘制宽度
            Paint paint=new Paint();
            paint.setColor(resources.getColor(R.color.colorAccent));
            int x=Math.round(Math.abs(dX));
            int drawWidth=Math.min(x,maxDrawWidth);//实际的绘制宽度，取实时滑动距离x和最大绘制距离maxDrawWidth最小值
            int itemTop=itemView.getBottom()-itemView.getHeight();//绘制的top位置
            //向右滑动
            if(dX>0){
                //根据滑动实时绘制一个背景
                c.drawRect(itemView.getLeft(),itemTop,drawWidth,itemView.getBottom(),paint);
                //在背景上面绘制图片
                if (x>padding){//滑动距离大于padding时开始绘制图片
                    //指定图片绘制的位置
                    Rect rect=new Rect();//画图的位置
                    rect.left=itemView.getLeft()+padding;
                    rect.top=itemTop+(itemView.getBottom()-itemTop-bitmap.getHeight())/2;//图片居中
                    int maxRight=rect.left+bitmap.getWidth();
                    rect.right=Math.min(x,maxRight);
                    rect.bottom=rect.top+bitmap.getHeight();
                    //指定图片的绘制区域
                    Rect rect1=null;
                    if (x<maxRight){
                        rect1=new Rect();//不能再外面初始化，否则dx大于画图区域时，删除图片不显示
                        rect1.left=0;
                        rect1.top = 0;
                        rect1.bottom=bitmap.getHeight();
                        rect1.right=x-padding;
                    }
                    c.drawBitmap(bitmap,rect1,rect,paint);
                }
                float alpha = 1.0f - Math.abs(dX) / (float) itemView.getWidth();
                itemView.setAlpha(alpha);
                //绘制时需调用平移动画，否则滑动看不到反馈
                itemView.setTranslationX(dX);
            }else {
                //如果在getMovementFlags指定了向左滑动（ItemTouchHelper。START）时则绘制工作可参考向右的滑动绘制，也可直接使用下面语句交友系统自己处理
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }else {
            //拖动时有系统自己完成
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }


    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        super.onSelectedChanged(viewHolder, actionState);

    }

    public interface ItemTouchHelperCallback {
        void onItemDelete(int positon);

        void onMove(int fromPosition, int toPosition);
    }
}
