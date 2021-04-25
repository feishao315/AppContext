package csds493.appcontext;

import java.util.ArrayList;
import java.util.List;

import soot.SootMethod;
import soot.Value;
import soot.jimple.Stmt;

public class PermissionInvocation {
	SootMethod src;
	SootMethod tgt;
	String permission;
	//List<Value> factors;	
	List<Context> contexts = new ArrayList<Context>();
	
	public PermissionInvocation(SootMethod src, SootMethod tgt,
			String permission) {
		super();
		this.src = src;
		this.tgt = tgt;
		this.permission = permission;
		
	}
	
	
	
	public PermissionInvocation(SootMethod src, SootMethod tgt) {
		super();
		this.src = src;
		this.tgt = tgt;
		
	}

	

	public List<Context> getContexts() {
		return contexts;
	}

	public void addContext(Context ctx){
		this.contexts.add(ctx);
	}

	public void setContexts(List<Context> contexts) {
		this.contexts = contexts;
	}



	public SootMethod getSrc() {
		return src;
	}
	public void setSrc(SootMethod src) {
		this.src = src;
	}
	public SootMethod getTgt() {
		return tgt;
	}
	public void setTgt(SootMethod tgt) {
		this.tgt = tgt;
	}
	public String getPermission() {
		return permission;
	}
	public void setPermission(String permission) {
		this.permission = permission;
	}
	
	
	
	@Override
	public boolean equals(Object obj){
		if(!(obj instanceof PermissionInvocation))
		return false;
		PermissionInvocation invoc = (PermissionInvocation)obj;
		if(this.getSrc().equals(invoc.getSrc()) && this.getTgt().equals(invoc.getTgt())) return true;
		return false;
		
	}

}
