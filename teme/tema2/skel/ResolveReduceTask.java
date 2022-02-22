import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;


public class ResolveReduceTask extends Thread {
	private String file_name;
	private HashMap<Integer, Long> dictionary;
	private ArrayList<String> max_length;
	static int[] fibbonacci = {0,1,1,2,3,5,8,13,21,34,55,89,144,233,377,610,987,1597,2584,4181,6765, 10946,17711,28657,46368,75025,121393,196418,317811,514229,832040};
	HashMap<String, ReduceTask> reduce_tasks;
	ArrayList<String> docs;
	int id;
	int nr_workers;
	CyclicBarrier barrier;
	
	private ArrayList<ReduceTaskResult> res;
	
	public ResolveReduceTask() {
		file_name = "";
		dictionary = new HashMap<Integer, Long>();
		max_length = new ArrayList<String>();
		this.barrier = barrier;
	}

	public ResolveReduceTask(HashMap<String, ReduceTask> reduce_tasks, int id, int nr_workers, CyclicBarrier barrier, String file_name, ArrayList<String> docs,
			ArrayList<ReduceTaskResult> res) {
		this.reduce_tasks = reduce_tasks;
		this.id = id;
		this.nr_workers = nr_workers;
		this.barrier = barrier;
		this.docs = docs;
		this.res = res;
	}

	@Override
	public void run(){
		 int start = id * (int) Math.ceil((double) reduce_tasks.size() / (double) nr_workers);
	     int end = Math.min((id + 1) * (int) Math.ceil((double) reduce_tasks.size() / (double) nr_workers), reduce_tasks.size());
		
	     try {
	            barrier.await();
	        } catch (InterruptedException | BrokenBarrierException e) {
	            e.printStackTrace();
	        }
				
				
		for(int i = start; i < end; i++) {
			file_name = docs.get(i);
		     ReduceTask task = reduce_tasks.get(file_name);
			max_length = task.max_length;
			dictionary = task.dictionary;
			file_name = task.file_name;
	
			//first, compute the final max_length list
			int MAX = 0;
			ArrayList<String> final_max_length = new ArrayList<String>();
			
			for(String s : max_length) {
				if(s.length() > MAX) {
					MAX = s.length();
					
					if(!final_max_length.contains(s)) {
						final_max_length.clear();
						final_max_length.add(s);
					}
				}  else if(s.length() == MAX) {
					final_max_length.add(s);
			} 
			}
			//now, compute rank
			
			Double rank =new Double(0);
			int total_nr_words = 0;
			for(Integer key : dictionary.keySet()) {
				rank += fibbonacci[key + 1] * dictionary.get(key);
				total_nr_words += dictionary.get(key);
			}
			rank /= total_nr_words;
			//String trunked = rank.toString().substring(0,5);
			ReduceTaskResult r = new ReduceTaskResult(file_name, final_max_length, rank, MAX);
			res.add(r);
			
		}
				
	}
	
	
}