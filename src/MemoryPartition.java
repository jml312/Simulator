import java.util.ArrayList;

public class MemoryPartition {

    private int pages; // number of pages

    private double req; // probability of a request being made

    private double free; // probability of this memory being free

    private boolean allocated; // status of the memory

    private final ArrayList<Integer> pageIndexes; // page indexes

    private double[] metoo; // the probability of this partition becoming allocated when another partition is allocated

    public MemoryPartition(int pages,  double req, double free, boolean allocated) {
        this.pages = pages;
        this.req = req;
        this.free = free;
        this.allocated = allocated;
        this.pageIndexes = new ArrayList<Integer>();
        this.metoo = new double[pages];
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public double getReq() {
        return req;
    }

    public void setReq(double req) {
        this.req = req;
    }

    public double getFree() {
        return free;
    }

    public void setFree(double free) {
        this.free = free;
    }

    public boolean isAllocated() {
        return allocated;
    }

    public void setAllocated(boolean allocated) {
        this.allocated = allocated;
    }

    public double[] getMetoo() {
        return metoo;
    }

    public void setMetoo(double[] metoo) {
        this.metoo = metoo;
    }

    public ArrayList<Integer> getPageIndexes() {
        return pageIndexes;
    }
}