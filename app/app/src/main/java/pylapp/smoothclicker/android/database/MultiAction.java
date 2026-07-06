package pylapp.smoothclicker.android.database;

public class MultiAction {
    public long id;
    public long parentId;
    public int type;
    public int x;
    public int y;
    public int endX;
    public int endY;
    public int duration;
    public int waitTime;
    public int repeat;
    public int delay;
    public int orderNum;

    public static final int TYPE_CLICK = 0;
    public static final int TYPE_SWIPE = 1;

    public MultiAction() {
    }

    public MultiAction(long parentId, int type) {
        this.parentId = parentId;
        this.type = type;
        this.x = 0;
        this.y = 0;
        this.endX = -1;
        this.endY = -1;
        this.duration = 100;
        this.waitTime = 0;
        this.repeat = 1;
        this.delay = 0;
        this.orderNum = 0;
    }
}