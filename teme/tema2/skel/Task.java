import java.io.*;
import java.util.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;


public class Task extends Thread {
	private final int id;
	private ArrayList<MapTask> mp_list;
	private String regex;
	static String s = ";a:a/a?a~a\\a.a,a>a<a~a`a[a]a{a}a(a)a!a@a#a$a%a^a&a-a_a+a'a=a*a\"a|a a\ta\na\sa";
	static String ss = ";:/?~\\.,><~`[]{}()!@#$%^&-_+'=*\"| \t\n";
	static ArrayList<String> delims = new ArrayList<>(Arrays.asList(s.split("a")));
	private Semaphore sem;
	private final int nr_workers;
	private final CyclicBarrier barrier;
	//the dictionary: key = length of word, value = nr. of words with that length
	private HashMap<String, HashMap<Integer, Long>> map_result;
	private HashMap<String, ArrayList<String>> max_len_map;
	private ArrayList<MapTaskResult> res;
	
	public Task(ArrayList<MapTask> mp_list, int id, int nr_workers, CyclicBarrier barrier, ArrayList<MapTaskResult> res) {
		this.mp_list = mp_list;
		this.id = id;
		this.regex = "[;:/?˜\\.,><‘{}()!@#$%ˆ&-+’=*\s\\[\\]]+";
		//separators = String.join("", delims);
		sem = new Semaphore(nr_workers);
		this.nr_workers = nr_workers;
		this.barrier = barrier;
		this.res = res;
	
	}

	@Override
	public void run(){
		
		 int start = id * (int) Math.ceil((double) mp_list.size() / (double) nr_workers);
	     int end = Math.min((id + 1) * (int) Math.ceil((double) mp_list.size() / (double) nr_workers), mp_list.size());
		
		try {
            barrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
			//System.out.println("THREAD " + id + "got tasks: ");
			
			for(int i = start; i < end; i++) {
			
				MapTask mp = mp_list.get(i);
				
				File file = new File(mp.getName());
				
				Boolean eof = false;
				
				try (RandomAccessFile data = new RandomAccessFile(file, "r")) {
			      byte[] size = new byte[(int)mp.getFrgSize()];
			      //move offset_start bytes to read from file
			      data.seek(mp.getOffsetStart());
			      data.read(size);
			      String str = new String(size);
   
		    	  if(mp.getOffsetStart() != 0) {
		    		  //if fragment starts in the middle of a word, skip that word
		    		  int k = 0;
		    		  data.seek(mp.getOffsetStart() - 1);
				      char c =  (char) data.readByte();
				      String one_char_str = c + "";
	
				      while(!delims.contains(one_char_str) && k < str.length() - 1) {

					      c = str.charAt(k);
					      k++;
					     one_char_str = c + "";
		
				      }
				      str = str.substring(k);
				      //I can't skip the last letter of a word, so I'm sometimes left with just one letter as a full word
				      //in that case just make the str null
				      if(str.length() == 1)
				    	  str = "";
				      
				   //   System.out.println("Now string is " + str);
		    	  }
		    	  
		    	  if(eof == false && str != "") {
				      char c = str.charAt(str.length() - 1);
				      String new_str = c + "";
			    	  //read until first delimiter, even if it is more than "size" (if fragment ends in the middle
			    	  //of a word, it reads that word
				      data.seek(mp.getOffsetEnd());
				    	  while(!delims.contains(new_str)) {
				    		  
					    	  try {
					    		  
					    		  c = (char) data.readByte();
					    		  new_str = c + "";
					    		  str += new_str;
					    	  }
					    	  catch(EOFException e){
					    		  break;
					    	  }
					      }

				   split_string(str, mp.getName());
				  // data.seek(mp.getOffsetStart());
		    	  } else {
		    		  MapTaskResult new_res = new MapTaskResult(mp.getName());
		    	      res.add(new_res);
		    	  }
 
				} catch(IOException ex){
			        System.out.println (ex.toString());
			        System.out.println("Could not find file " + mp.getName());
			    }
				
			}
			
			
			try {
	            barrier.await();
	        } catch (InterruptedException | BrokenBarrierException e) {
	            e.printStackTrace();
	        }
			
	}
	void split_string(String str, String file_name) {
		//ArrayList<String> max_length =max_len_map.get(file_name);
		//HashMap<Integer, Long> dictionary = map_result.get(file_name);
		Integer MAX;

		ArrayList<String> max_length = new ArrayList<String>();
		HashMap<Integer, Long> dictionary = new HashMap<Integer, Long>();

			MAX = 0;
		
		for (Character oldChar : ss.toCharArray()) {
			str = str.replace(oldChar, ' ');
		}

		 String[] arrSplit = str.split("\\s+");
	      for (int j=0; j < arrSplit.length; j++)
	      {
	    	if(arrSplit[j].length() != 0) {
	    	//	System.out.println("small: %" + arrSplit[j] + "%" + "(" + arrSplit[j].length()+")");
	    	//	System.out.println("dictionary: " + dictionary.toString());
		    	if( dictionary.containsKey(arrSplit[j].length())) {
		    		 
		    		dictionary.put(arrSplit[j].length(), dictionary.get(arrSplit[j].length()) + 1);
		    	} else {
		    		Long length = new Long(1);
		    		dictionary.put(arrSplit[j].length(), length);
		    	}
		    	//System.out.println("MAP: " + map_result.get(file_name));
	    	}
	    	
	    	if(arrSplit[j].length() > MAX) {
	    		
	    		//only store words of maximum length
	    		if(max_length != null) {
	    			max_length.clear();
	    		}
	    		else max_length = new ArrayList<String>();
	    		MAX = arrSplit[j].length();
	    		max_length.add(arrSplit[j]);
	    	} else {
	    		if(arrSplit[j].length() == MAX) {
	    			
	    			if(!max_length.contains(arrSplit[j])) {
	    				max_length.add(arrSplit[j]);
	    			}
	    		}
	    	}

	    	 
	      }

	      MapTaskResult new_res = new MapTaskResult(file_name, dictionary, max_length);
	     
	      res.add(new_res);
	      
	    
	}
	
}