package pylapp.smoothclicker.android.database;

public class Script {
    public long id;
    public String name;
    public String category;
    public long createdAt;
    public long updatedAt;

    public Script() {
        this.category = "";
    }

    public Script(String name) {
        this.name = name;
        this.category = "";
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public Script(String name, String category) {
        this.name = name;
        this.category = category;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public Script(long id, String name, String category, long createdAt, long updatedAt) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}