package wtr.g1;

import wtr.sim.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ClusterPoint extends Point implements Comparable<ClusterPoint>{

	public ClusterPoint(double x, double y, int id) {
		super(x, y, id);
	}
	
	public ClusterPoint(double x, double y, int id, int cluster_number) {
		super(x, y, id);
		this.cluster_number= cluster_number;
	}
	
	private int cluster_number = 0;

	public double getX()  {
		return this.x;
	}

	public double getY() {
		return this.y;
	}

	public void setCluster(int n) {
		this.cluster_number = n;
	}

	public int getCluster() {
		return this.cluster_number;
	}

	//Calculates the distance between two points.
	protected static double distance(ClusterPoint p, ClusterPoint centroid) {
		double dx = centroid.x - p.x;
		double dy = centroid.y - p.y;
		return Math.sqrt(dx * dx + dy*dy);
	}

	protected static ClusterPoint getMidPoint(ClusterPoint p1, ClusterPoint p2) 
	{
	    return new ClusterPoint((p1.x + p2.x)/2.0,(p1.y+ p2.y)/2, 0);
	}


	@Override
	public String toString() {
		return "ClusterPoint [cluster_number=" + cluster_number + ", ("
			+ x + "," + y + ")";
}

public static List<ClusterPoint> pointToClusterPoint(Point[] rats) {
	List<ClusterPoint> cpList=new ArrayList<ClusterPoint>(); 
	for(Point r :rats){
		cpList.add(new ClusterPoint(r.x,r.y, r.id));
	}
	return cpList;
}

public int compareTo(ClusterPoint p2) {
	double distToMyPosition = ClusterPoint.distance(KMeans.myPosition,this);
	double distToP2 = ClusterPoint.distance(KMeans.myPosition,p2);
	return Double.compare(distToP2,distToMyPosition);
}

public ClusterPoint unit()
{
	double dist = distance(this, new ClusterPoint(0,0,0));
	if(dist == 0.0)
		return new ClusterPoint(0,0,0);
	return new ClusterPoint(this.x/dist, this.y/dist, 0);
}

}
