package pylapp.smoothclicker.android.database;

public class Action {
    public long id;
    public long scriptId;
    public String name;
    public int type;
    public int x;
    public int y;
    public int endX;
    public int endY;
    public int duration;
    public int waitTime;
    public int repeat;
    public int orderNum;

    public static final int TYPE_CLICK = 0;
    public static final int TYPE_SWIPE = 1;
    public static final int TYPE_MULTI = 2;
    public static final int TYPE_DELAY = 3;

    public Action() {
    }

    public Action(long scriptId, String name, int type) {
        this.scriptId = scriptId;
        this.name = name;
        this.type = type;
        this.x = 0;
        this.y = 0;
        this.endX = -1;
        this.endY = -1;
        this.duration = 100;
        this.waitTime = 0;
        this.repeat = 1;
        this.orderNum = 0;
    }
}