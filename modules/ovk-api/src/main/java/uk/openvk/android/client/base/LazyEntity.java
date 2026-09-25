package uk.openvk.android.client.base;

public class LazyEntity {
    public static final int SLEEPING_ENTITY = 0;
    public static final int LAYOUT_HEADER   = 1;
    public static final int REAL_ENTITY     = 2;

    protected int entityType;
    public long id;

    public LazyEntity() { }

    public LazyEntity(int type) {
        this.entityType = type;
    }

    public int getEntityType() {
        return entityType;
    }
}
