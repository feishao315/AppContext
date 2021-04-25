package csds493.appcontext;		

import java.util.ArrayList;
import java.util.List;

import soot.SootMethod;
import soot.jimple.Ref;
import soot.jimple.Stmt;

public class Context {
	List<Stmt> conditionalStmt;
	List<SootMethod> factorMethod;
	SootMethod entrypoint;
	List<Ref> factorRef;//Sources in some information flows are parameters of entrypoint method, e.g., @parameter0: android.content.Context in method <com.android.providers.downloadsmanager.DownloadCompleteReceiver: void onReceive(android.content.Context,android.content.Intent)>
	
	
	public SootMethod getEntrypoint() {
		return entrypoint;
	}

	public void setEntrypoint(SootMethod entrypoint) {
		this.entrypoint = entrypoint;
	}

	public List<Stmt> getConditionalStmt() {
		return conditionalStmt;
	}

	public void setConditionalStmt(List<Stmt> conditionalStmt) {
		this.conditionalStmt = conditionalStmt;
	}
	
	public void addFactorMethod(SootMethod v){
		if(this.factorMethod == null)
			this.factorMethod = new ArrayList<SootMethod>();
		this.factorMethod.add(v);
	}
	
	public boolean hasFactorMethod(SootMethod v){
		if(this.factorMethod == null)
			this.factorMethod = new ArrayList<SootMethod>();
		return factorMethod.contains(v);
	}
	
	
	
	public List<SootMethod> getFactorMethod() {
		return factorMethod;
	}

	public void setFactorMethod(List<SootMethod> factorMethod) {
		this.factorMethod = factorMethod;
	}

	public List<Ref> getFactorRef() {
		return factorRef;
	}

	public void setFactorRef(List<Ref> factorRef) {
		this.factorRef = factorRef;
	}

	public void addFactorRef(Ref r){
		if(this.factorRef == null)
			this.factorRef = new ArrayList<Ref>();
		this.factorRef.add(r);
	}
	public boolean hasFactorRef(Ref r){
		if(this.factorRef == null)
			this.factorRef = new ArrayList<Ref>();
		return factorRef.contains(r);
	}
}
