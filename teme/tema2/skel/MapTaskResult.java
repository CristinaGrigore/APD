import java.util.ArrayList;
import java.util.HashMap;

public class MapTaskResult {
	String file_name;
	HashMap<Integer, Long> dictionary;
	ArrayList<String> max_length;
	
	public MapTaskResult(String file_name, HashMap<Integer, Long> dictionary, ArrayList<String> max_length) {
		this.file_name = file_name;
		this.dictionary = dictionary;
		this.max_length = max_length;
	}
	public MapTaskResult(String name) {
		this.file_name = name;
		this.dictionary = new HashMap<Integer, Long>();
		this.max_length = new ArrayList<String>();
	}
	public void printResult() {
		System.out.println(file_name + ":\n" + "Dictionary: " + dictionary.toString() + "\nMax length: " + max_length.toString() + "\n");
	}
}
