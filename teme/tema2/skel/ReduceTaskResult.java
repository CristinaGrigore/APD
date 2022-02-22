import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class ReduceTaskResult {
	String file_name;
	double rank;
	ArrayList<String> max_length;
	private static final DecimalFormat df = new DecimalFormat("0.00");
	int MAX;
	ArrayList<String> final_max_length;
	
	public ReduceTaskResult(String file_name, ArrayList<String> final_max_length,
		double rank, int MAX) {
		this.file_name = file_name;
		this.MAX = MAX;
		this.final_max_length = final_max_length;
		this.rank = rank;
	}
	
	public String toString() {
		String[] arr = file_name.split("/");
		
		return (arr[arr.length - 1] + "," + df.format(rank) + "," + MAX + "," + final_max_length.size() + "\n");
	}
}