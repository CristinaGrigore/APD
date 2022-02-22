import java.util.Comparator;

public class CustomComparator implements Comparator<ReduceTaskResult> {
    @Override
    public int compare(ReduceTaskResult o1, ReduceTaskResult o2) {
        return Double.compare(o2.rank, o1.rank);
    }
}