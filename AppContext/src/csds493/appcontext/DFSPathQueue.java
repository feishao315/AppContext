package csds493.appcontext;

import java.util.ArrayList;



/**
 * @author fxs128
 * This class is used for keep track of the path from current node to start node. 
 */
public class DFSPathQueue<T> {
	ArrayList<T> queue;
	ArrayList<Boolean> existed;
	
	public DFSPathQueue(){
		queue = new ArrayList<T>();
		existed = new ArrayList<Boolean>();
	}
	
	public void push(T element){
		queue.add(element);
		existed.add(new Boolean(true));
	}
	
	public T pop(){
		int length = queue.size()-1;
		for(int i = length; i >= 0; i--){
			if(existed.get(i).booleanValue() == true){
				T element = queue.get(i);
				existed.set(i, new Boolean(false));
				return element;
			}
		}
		return null;
	}
	
	public T lastRemoved(){
		for(int i = queue.size()-1; i >= 0; i--){
			if(existed.get(i).booleanValue() == true){
				if(i == queue.size()-1) return null;
				else return queue.get(i+1);
			}
		}
		return null;
	}
	
	public boolean isEmpty(){
		for(int i = queue.size()-1; i >= 0; i--){
			if(existed.get(i).booleanValue() == true){
				return false;
			}
		}
		return true;
	}
	
}
