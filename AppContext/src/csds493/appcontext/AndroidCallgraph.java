package csds493.appcontext;

import org.apache.commons.io.FileUtils;
import soot.util.queue.QueueReader;

import soot.MethodOrMethodContext;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.infoflow.InfoflowConfiguration;
import soot.jimple.infoflow.android.InfoflowAndroidConfiguration;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import au.com.bytecode.opencsv.CSVWriter;
import java.io.FileWriter;

import soot.jimple.IfStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.Ref;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;
import soot.jimple.Stmt;
import soot.jimple.TableSwitchStmt;

import java.io.PrintWriter;
import java.util.Map.Entry;

public class AndroidCallgraph {
	private final static String USER_HOME = System.getProperty("user.home");
	private static String androidJar = USER_HOME + "/Library/Android/sdk/platforms";
	static String androidDemoPath = System.getProperty("user.dir") + File.separator + "demo" + File.separator
			+ "Android";

	static List<String> PscoutMethod;
	static String folder = "C:\\Users\\anity\\Desktop\\PhD\\CSDS493\\Project\\Dowgin";
	static String csv = "./out.csv";
	static CSVWriter writer;
	static List<String[]> data = new ArrayList<String[]>();

	public static void main(String[] args) throws IOException {

		androidJar = "C:\\Users\\anity\\AppData\\Local\\Android\\Sdk\\platforms\\android-30\\android.jar";
		try {
			PscoutMethod = FileUtils.readLines(new File("./jellybean_publishedapimapping_parsed.txt"));
			// for(String m: PscoutMethod) {
			// System.out.println("-- "+m);
			// }
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (System.getenv().containsKey("ANDROID_HOME"))
			androidJar = System.getenv("ANDROID_HOME") + File.separator + "platforms";
		// Parse arguments
		InfoflowConfiguration.CallgraphAlgorithm cgAlgorithm = InfoflowConfiguration.CallgraphAlgorithm.SPARK;
		if (args.length > 0 && args[0].equals("CHA"))
			cgAlgorithm = InfoflowConfiguration.CallgraphAlgorithm.CHA;
		boolean drawGraph = false;
		if (args.length > 1 && args[1].equals("draw"))
			drawGraph = true;
		
		File directoryPath = new File(folder);
//		String apks[] = directoryPath.list();
		String apks[] = {"0b3d218bfd3f480657d6f5370fe43989.apk"};
		writer = new CSVWriter(new FileWriter(csv, true));
		for (int i = 0; i < apks.length; i++) {
			System.out.println(apks[i]);
			// Setup FlowDroid
			InfoflowAndroidConfiguration config = AndroidUtil
					.getFlowDroidConfig(folder + File.separator + apks[i], androidJar, cgAlgorithm);
			SetupApplication app = new SetupApplication(config);

			app.getConfig().getAnalysisFileConfig().setSourceSinkFile("./SourcesAndSinks.txt");
//			app.setCallbackFile("./AndroidCallbacks.txt");

			// Create the Callgraph without executing taint analysis
			app.constructCallgraph();
			CallGraph cg = Scene.v().getCallGraph();

			List<PermissionInvocation> perInvocs = analyzeCG(apks[i], cg);

		}
		writer.writeAll(data);
		writer.close();

	}

	public static List<PermissionInvocation> analyzeCG(String apk, CallGraph cg) {
		JimpleBasedInterproceduralCFG icfg = new JimpleBasedInterproceduralCFG();

		List<PermissionInvocation> perInvocs = new ArrayList<PermissionInvocation>();
		QueueReader<Edge> edges = cg.listener();
		while (edges.hasNext()) {
			Edge edge = edges.next();
			SootMethod target = (SootMethod) edge.getTgt();
			MethodOrMethodContext src = edge.getSrc();

			if (PscoutMethod.contains(target.toString())) {
				PermissionInvocation perInvoc = new PermissionInvocation((SootMethod) src, target);
				// System.out.println("** src:"+src.toString()+" target:" + target.toString());
				Unit u;

				if (!perInvocs.contains(perInvoc)) {
					String permission = getPermissionForInvoc(target.toString(), PscoutMethod);
					perInvoc.setPermission(permission);
					System.out.println("++"+target.toString());
//					System.out.println("++" + target.getDeclaringClass());
					printCFGpath(apk, (SootMethod) src, target, permission, icfg, cg, perInvoc);
					perInvocs.add(perInvoc);
				}
			}
		}

		System.out.println("size: " + perInvocs.size() + "  cg.size: " + cg.size());
		return perInvocs;
	}

	public static void printCFGpath(String apk, SootMethod src, SootMethod tgt, String permission, BiDiInterproceduralCFG<Unit, SootMethod> icfg,
			CallGraph cg, PermissionInvocation permInvoc) {
		/*
		 * Body srcBody = ((SootMethod)src).getActiveBody(); Iterator<Unit> unitItr =
		 * srcBody.getUnits().snapshotIterator();
		 */
		Set<Unit> calls = icfg.getCallsFromWithin(src);
		Iterator<Unit> unitItr = calls.iterator();
		Stmt tgtStmt = null;
		while (unitItr.hasNext()) {
			Unit u = unitItr.next();
			if (u instanceof Stmt) {
				Stmt stmt = (Stmt) u;
				// System.out.println("src: "+u);
				// not sure why com.android.internal.telephony.PhoneSubInfoProxy become
				// com.android.internal.telephony.PhoneSubInfoProxy$stub
				if (stmt.containsInvokeExpr() && stmt.getInvokeExpr().getMethod().equals(tgt))
					tgtStmt = stmt;
			}
		}
		System.out.println("=========CFG=======");
		// this.icfg = icfg;
		printCFGpath(apk, src, tgt, permission, tgtStmt, icfg, cg, permInvoc);
	}

	// Iterative version with DFSPathQueue
	public static void printCFGpath(String apk, SootMethod src, SootMethod tgt, String permission, Unit u, BiDiInterproceduralCFG<Unit, SootMethod> icfg, CallGraph cg,
			PermissionInvocation permInvoc) {
		/* File resultFile = new File("CFG1.log"); */
		Set<Unit> callers = new HashSet<Unit>();
		Unit last = null;
		DFSPathQueue<Unit> unitStack = new DFSPathQueue<Unit>();
		DFSPathQueue<SootMethod> callerStack = new DFSPathQueue<SootMethod>();
		Map<Unit, SootMethod> contexts = new HashMap<Unit, SootMethod>();
		ArrayList<SootMethod> path = new ArrayList<SootMethod>();
		path.add(src);
		ArrayList<Set<SootMethod>> methodByEntries = new ArrayList<Set<SootMethod>>();
		ArrayList<SootMethod> entries = new ArrayList<SootMethod>();
		unitStack.push(u);
		callerStack.push(src);
		int signal = 0;// when branch point is more than merge point, then u is in a branch.
		int infiniteLoopSignal = 0;
		
		Stack<SootMethod> myCallerStack = new Stack<SootMethod>();
		while (!unitStack.isEmpty()) {
			// if(unitStack.size() > 20) break;
			if(infiniteLoopSignal > 1000) break;
			boolean isStartpoint = true;

			try {
				isStartpoint = icfg.isStartPoint(u);
			} catch (java.lang.NullPointerException e) {
				System.err.println("DirectedGraph cannot be constructed: " + u);

				try {
					u = (icfg.getPredsOf(u).iterator().next()); // null pointer
				} catch (java.lang.NullPointerException e1) {
					isStartpoint = true;
				}
				last = u;
			}
			if (!isStartpoint) {
				// if u is a branch stmt, if signal <= 0, add pred stmt with method
				// name "invokeIfStmt" to the conditionalStmt; signal minus 1

				// if (last != null && icfg.isBranchTarget(u, last)) {
				if (u instanceof IfStmt || u instanceof TableSwitchStmt || u instanceof LookupSwitchStmt) {					
					infiniteLoopSignal ++;
//					System.out.println("branch: "+u+" infiniteLoopSignal:"+infiniteLoopSignal);
					if (signal <= 0) {
						Unit predUnit = u;

						while (u.equals(predUnit)) {
							predUnit = icfg.getPredsOf(u).iterator().next();
							if (icfg.getPredsOf(u).size() == 1) {
								if (predUnit instanceof InvokeStmt) {
									InvokeStmt condStmt = (InvokeStmt) predUnit;
									if (condStmt.getInvokeExpr().getMethod().getName().contains("invokeIfStmt")) {
										u = predUnit;
										System.out.println("condStmt: "+condStmt);
										contexts.put(condStmt, src);
									}
								}
							}
						}
					} else {
						signal--;
					}
				}
				if (icfg.getPredsOf(u).size() == 0) {

				}
				// assumption icfg.getPredsOf(u).size() >=1
				if (icfg.getPredsOf(u).size() > 1) {
					signal++;
//					System.out.println("Merge: "+u);
				}
//				System.out.println("u:" +u);
				last = u;
				u = icfg.getPredsOf(u).iterator().next();
			} else {
				// if start point, then add callsite for this method to stmts
				System.out.println("Starting Point: "+u);

				Iterator<Edge> iter = cg.edgesInto(src);// icfg.getMethodOf(u)
				while (iter.hasNext()) {
					Edge edge = iter.next();
					SootMethod srcCallerMethod = edge.src();// The method where src was called
					if (srcCallerMethod.toString().contains("dummyMainClass: void dummyMainMethod(java.lang.String[])")) {
						// add methods in path into the set
						if (!entries.contains(src)) {
							entries.add(src);
							methodByEntries.add(new HashSet<SootMethod>(path));
						} else {
							int i = entries.indexOf(src);
							Set<SootMethod> s = methodByEntries.get(i);
							s.addAll(new HashSet<SootMethod>(path));
							methodByEntries.set(i, s);
						}

						// cut path to the braching point in dps
						if (callerStack.lastRemoved() != null) {
							path = cutPath(path, callerStack.lastRemoved());
						}
						break;
					}
					Unit caller = edge.srcUnit(); // The stmt where call to src happen
					if (caller == null)
						continue;
					if (!callers.contains(caller)) {
						callers.add(caller);
						System.out.println("Caller: "+caller);
						try {
							System.out.println("Caller Method: "+ icfg.getMethodOf(caller));
							if(!icfg.getMethodOf(caller).toString().contains("dummyMainClass:"))
								myCallerStack.push(icfg.getMethodOf(caller));
						} catch (java.lang.NullPointerException e) {
							System.out.println("no corresponding method for" + caller);
							continue;
						}
						if (!caller.toString().contains("dummyMainClass: void dummyMainMethod(java.lang.String[])")) {

							unitStack.push(caller);
							callerStack.push(srcCallerMethod);
						}
					} else {// if caller has already been visited
							// for each set where srcCallerMethod is in, add all methods in path into that
							// set
						for (int i = 0; i < methodByEntries.size(); i++) {
							Set<SootMethod> s = methodByEntries.get(i);
							if (s.contains(srcCallerMethod)) {
								s.addAll(new HashSet<SootMethod>(path));
								methodByEntries.set(i, s);
							}
						}
						if (callerStack.lastRemoved() != null) {
							path = cutPath(path, callerStack.lastRemoved());
						}
					}
				}
				u = unitStack.pop();

				src = callerStack.pop();
				path.add(src);
				
			}
			
		}
		
		if(myCallerStack.size()>0) {
			Context myContext = new Context();
			myContext.setEntrypoint(myCallerStack.pop());
			System.out.println("******"+myContext.getEntrypoint().toString());
			permInvoc.addContext(myContext);
			
			ArrayList<String> result = new ArrayList<String>();
			result.add(apk);
			result.add(permission);
			result.add(tgt.toString());
			result.add(myContext.getEntrypoint().toString());
			String[] resultArray = result.toArray(new String[result.size()]);
			data.add(resultArray);
		}		
		
	}
	
	public static ArrayList<SootMethod> cutPath(ArrayList<SootMethod> path, SootMethod node){
		int length = path.size()-1;
		for(int i = length; i>=0; i--){
			if(!path.get(i).equals(node)) path.remove(i);
		}
		return path;
	}

	public static String getPermissionForInvoc(String signature, List<String> file) {
		String permission = "";
		// assumption: the List<String> is in the same order in the original file.
		for (String s : file) {
			if (s.startsWith("Permission:"))
				permission = s.substring(11, s.length());
			else if (s.contains(signature))
				break;
		}
		// System.out.println("** Permission: " + permission);
		return permission;
	}

}
