package pylapp.smoothclicker.android.database;

public class Script {
    public long id;
    public String name;
    public long createdAt;
    public long updatedAt;

    public Script() {
    }

    public Script(String name) {
        this.name = name;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public Script(long id, String name, long createdAt, long updatedAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}