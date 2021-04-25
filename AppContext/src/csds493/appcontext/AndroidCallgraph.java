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
import java.io.IOException;
import java.util.*;

public class AndroidCallgraph {
    private final static String USER_HOME = System.getProperty("user.home");
    private static String androidJar = USER_HOME + "/Library/Android/sdk/platforms";
    static String androidDemoPath = System.getProperty("user.dir") + File.separator + "demo" + File.separator + "Android";
//    static String apkPath = androidDemoPath + File.separator + "/st_demo.apk";

	//String apk = args[0].substring(args[0].lastIndexOf("/"), args[0].indexOf(".apk"));
	//String appPath = args[1];
	static String apkPath = "C:\\Users\\anity\\Downloads\\Temp\\MyFlowdroid\\sampleAPK\\09b143b430e836c513279c0209b7229a4d29a18c.apk";
//    static String childMethodSignature = "<dev.navids.multicomp1.ClassChild: void childMethod()>";
//    static String childBaseMethodSignature = "<dev.navids.multicomp1.ClassChild: void baseMethod()>";
//    static String parentMethodSignature = "<dev.navids.multicomp1.ClassParent: void baseMethod()>";
//    static String unreachableMethodSignature = "<dev.navids.multicomp1.ClassParent: void unreachableMethod()>";
//    static String mainActivityEntryPointSignature = "<dummyMainClass: dev.navids.multicomp1.MainActivity dummyMainMethod_dev_navids_multicomp1_MainActivity(android.content.Intent)>";
//    static String mainActivityClassName = "dev.navids.multicomp1.MainActivity";
	
	static List<PermissionInvocation> perInvocs  = new ArrayList<PermissionInvocation>();
	static List<String> PscoutMethod;
	static CallGraph cg;
	

    public static void main(String[] args){
    	
    	androidJar = "C:\\Users\\anity\\AppData\\Local\\Android\\Sdk\\platforms\\android-30\\android.jar";
    	try {
			PscoutMethod = FileUtils.readLines(new File ("./jellybean_publishedapimapping_parsed.txt"));
//			for(String m: PscoutMethod) {
//				System.out.println("-- "+m);
//			}
		} catch (IOException e) {
			e.printStackTrace();
		}

        if(System.getenv().containsKey("ANDROID_HOME"))
            androidJar = System.getenv("ANDROID_HOME")+ File.separator+"platforms";
        // Parse arguments
        InfoflowConfiguration.CallgraphAlgorithm cgAlgorithm = InfoflowConfiguration.CallgraphAlgorithm.SPARK;
        if (args.length > 0 && args[0].equals("CHA"))
            cgAlgorithm = InfoflowConfiguration.CallgraphAlgorithm.CHA;
        boolean drawGraph = false;
        if (args.length > 1 && args[1].equals("draw"))
            drawGraph = true;
        // Setup FlowDroid
        final InfoflowAndroidConfiguration config = AndroidUtil.getFlowDroidConfig(apkPath, androidJar, cgAlgorithm);
        SetupApplication app = new SetupApplication(config);
        // Create the Callgraph without executing taint analysis
        app.constructCallgraph();
        cg = Scene.v().getCallGraph();
        analyzeCG();
        for(PermissionInvocation perInvoc:perInvocs){
			System.out.println("tgt: "+perInvoc.getTgt());
			System.out.println("src: "+perInvoc.getSrc());
			System.out.println("per: "+perInvoc.getPermission());
        }
//        int classIndex = 0;
//        // Print some general information of the generated callgraph. Note that although usually the nodes in callgraph
//        // are assumed to be methods, the edges in Soot's callgraph is from Unit to SootMethod.
//        AndroidCallGraphFilter androidCallGraphFilter = new AndroidCallGraphFilter(AndroidUtil.getPackageName(apkPath));
//        for(SootClass sootClass: androidCallGraphFilter.getValidClasses()){
//            System.out.println(String.format("Class %d: %s", ++classIndex, sootClass.getName()));
//            for(SootMethod sootMethod : sootClass.getMethods()){
//                int incomingEdge = 0;
//                for(Iterator<Edge> it = cg.edgesInto(sootMethod); it.hasNext();incomingEdge++,it.next());
//                int outgoingEdge = 0;
//                for(Iterator<Edge> it = cg.edgesOutOf(sootMethod); it.hasNext();outgoingEdge++,it.next());
//                System.out.println(String.format("\tMethod %s, #IncomeEdges: %d, #OutgoingEdges: %d", sootMethod.getName(), incomingEdge, outgoingEdge));
//            }
//        }
//        System.out.println("-----------");
//        // Retrieve some methods to demonstrate reachability in callgraph
//        SootMethod childMethod = Scene.v().getMethod(childMethodSignature);
//        SootMethod parentMethod = Scene.v().getMethod(parentMethodSignature);
//        SootMethod unreachableMehthod = Scene.v().getMethod(unreachableMethodSignature);
//        SootMethod mainActivityEntryMethod = Scene.v().getMethod(mainActivityEntryPointSignature);
//        // A better way to find MainActivity's entry method (generated by FlowDroid)
//        for(SootMethod sootMethod : app.getDummyMainMethod().getDeclaringClass().getMethods()) {
//            if (sootMethod.getReturnType().toString().equals(mainActivityClassName)) {
//                System.out.println("MainActivity's entrypoint is " + sootMethod.getName()
//                        + " and it's equal to mainActivityEntryMethod: " + sootMethod.equals(mainActivityEntryMethod));
//            }
//        }
        // Perform BFS from the main entrypoint to see if "unreachableMehthod" is reachable at all or not
//        Map<SootMethod, SootMethod> reachableParentMapFromEntryPoint = getAllReachableMethods(app.getDummyMainMethod());
//        if(reachableParentMapFromEntryPoint.containsKey(unreachableMehthod))
//            System.out.println("unreachableMehthod is reachable, a possible path from the entry point: " + getPossiblePath(reachableParentMapFromEntryPoint, unreachableMehthod));
//        else
//            System.out.println("unreachableMehthod is not reachable from the entrypoint.");
//        // Perform BFS to get all reachable methods from MainActivity's entry point
//        Map<SootMethod, SootMethod> reachableParentMapFromMainActivity = getAllReachableMethods(mainActivityEntryMethod);
//        if(reachableParentMapFromMainActivity.containsKey(childMethod))
//            System.out.println("childMethod is reachable from MainActivity, a possible path: " + getPossiblePath(reachableParentMapFromMainActivity, childMethod));
//        else
//            System.out.println("childMethod is not reachable from MainActivity.");
//        if(reachableParentMapFromMainActivity.containsKey(parentMethod))
//            System.out.println("parentMethod is reachable from MainActivity, a possible path: " + getPossiblePath(reachableParentMapFromMainActivity, parentMethod));
//        else
//            System.out.println("parentMethod is not reachable from MainActivity.");


        // Draw a subset of call graph
//        if (drawGraph) {
//            Visualizer.v().addCallGraph(cg,
//                    androidCallGraphFilter,
//                    new Visualizer.AndroidNodeAttributeConfig(true));
//            Visualizer.v().draw();
//        }
    }
    
	public static void analyzeCG(){
	 QueueReader<Edge> edges = cg.listener();
		while (edges.hasNext()) {
			Edge edge = edges.next();
			SootMethod target = (SootMethod) edge.getTgt();
			MethodOrMethodContext src = edge.getSrc();
			System.out.println("** "+target.toString());
			if (PscoutMethod.contains(target.toString())) {
				PermissionInvocation perInvoc = new PermissionInvocation((SootMethod)src,target);
				Unit u;
				/*try{
				u = verifyCall((SootMethod)src,target,cg);
				}
				catch(Exception e){
					System.err.println("Method "+target+" in "+ src + "throws "+e);
					continue;
				}
				if(u == null) {
					System.err.println("Method "+target+" is not in "+ src );
					continue;
				}*/
				if (!perInvocs.contains(perInvoc)) {
					//if(target.toString().contains("android.telephony.TelephonyManager: java.lang.String getDeviceId()")){
						perInvoc.setPermission(getPermissionForInvoc(target.toString(), PscoutMethod));
//						printCFGpath((SootMethod)src,target,icfg,cg,perInvoc);
						perInvocs.add(perInvoc);
					//}
				}
			}
		}
	
	System.out.println("size: "+perInvocs.size()+ "  cg.size: "+cg.size());
}

public static String getPermissionForInvoc(String signature, List<String> file){
	String permission = "";
	//assumption: the List<String> is in the same order in the original file.
	for(String s: file){
		if(s.startsWith("Permission:")) permission = s.substring(11, s.length());
		else if (s.contains(signature)) break;
	}
	System.out.println("** Permission: "+permission);
	return permission;
}

    // A Breadth-First Search algorithm to get all reachable methods from initialMethod in the callgraph
    // The output is a map from reachable methods to their parents
    public static Map<SootMethod, SootMethod> getAllReachableMethods(SootMethod initialMethod){
        CallGraph callgraph = Scene.v().getCallGraph();
        List<SootMethod> queue = new ArrayList<>();
        queue.add(initialMethod);
        Map<SootMethod, SootMethod> parentMap = new HashMap<>();
        parentMap.put(initialMethod, null);
        for(int i=0; i< queue.size(); i++){
            SootMethod method = queue.get(i);
            for (Iterator<Edge> it = callgraph.edgesOutOf(method); it.hasNext(); ) {
                Edge edge = it.next();
                SootMethod childMethod = edge.tgt();
                if(parentMap.containsKey(childMethod))
                    continue;
                parentMap.put(childMethod, method);
                queue.add(childMethod);
            }
        }
        return parentMap;
    }

    public static String getPossiblePath(Map<SootMethod, SootMethod> reachableParentMap, SootMethod it) {
        String possiblePath = null;
        while(it != null){
            String itName = it.getDeclaringClass().getShortName()+"."+it.getName();
            if(possiblePath == null)
                possiblePath = itName;
            else
                possiblePath = itName + " -> " + possiblePath;
            it = reachableParentMap.get(it);
        } return possiblePath;
    }

}
