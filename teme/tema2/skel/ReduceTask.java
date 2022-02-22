import java.util.ArrayList;
import java.util.HashMap;

public class ReduceTask {
	String file_name;
	HashMap<Integer, Long> dictionary;
	ArrayList<String> max_length;
	
	public ReduceTask() {
		this.file_name = "";
		dictionary = new HashMap<Integer, Long>();
		max_length = new ArrayList<String>();
	}
	
	public ReduceTask(String file_name) {
		this.file_name = file_name;
		dictionary = new HashMap<Integer, Long>();
		max_length = new ArrayList<String>();
	}
	public void addToDictionary(HashMap<Integer, Long> d) {
		for(Integer key : d.keySet()) {
			if(dictionary.containsKey(key)) 
				dictionary.put(key, d.get(key) + dictionary.get(key));
			 else 
				dictionary.put(key, d.get(key));				
		}
		
	}
	public void addMaxLength(ArrayList<String> len) {
		
			max_length.addAll(len);
		
	}

	public void create(MapTaskResult res) {
		if(file_name == "")
			file_name = res.file_name;
		addToDictionary(res.dictionary);
		addMaxLength(res.max_length);
	}
	public void printReduceTaskTask() {
		System.out.println("REDUCE: " + file_name + ":\n" + "Dictionary: " + dictionary.toString() + "\nMax length: " + max_length.toString() + "\n");
	}
	
	
}
