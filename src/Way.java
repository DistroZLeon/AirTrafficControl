import java.util.concurrent.Semaphore;

public class Way {
    private final Semaphore permit = new Semaphore(1);
    private final int id;
    private int occupantId;
    public Way(int id){
        this.id = id;
    }
    public int getId() {
        return this.id;
    }

    public int getOccupantId() {
        return this.occupantId;
    }

    public int acquire(int id) {
        if(this.permit.tryAcquire()) {
            this.occupantId = id;
            return this.id;
        }
        return -1;
    }

    public void release(){
        this.permit.release();
        this.occupantId = 0;
    }
}
