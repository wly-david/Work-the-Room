package wtr.g1;
import wtr.sim.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.*;

public class KMeans 
{
    private int NUM_CLUSTERS = 3;    
    public static ClusterPoint myPosition;
    private List<ClusterPoint> points;
    public List<Cluster> clusters;

    public KMeans(int ClusterCount, Point[] players, Point myPoint) 
    {
        KMeans.myPosition = new ClusterPoint(myPoint.x, myPoint.y, myPoint.id);
        this.NUM_CLUSTERS = ClusterCount; 
        this.clusters = new ArrayList<Cluster>(); 
    	this.points = new ArrayList<ClusterPoint>();
        //select points to be initial centerPoints
        List<ClusterPoint> centerPoints=new ArrayList<ClusterPoint>();
        if(players.length >= this.NUM_CLUSTERS)
        {
            for(int i = 0; i < this.NUM_CLUSTERS; i++)
                centerPoints.add(new ClusterPoint(players[i].x, players[i].y, players[i].id));
        }
        else
        {
            this.NUM_CLUSTERS = players.length;
            for(int i = 0; i < players.length; i++)
            {
                centerPoints.add(new ClusterPoint(players[i].x, players[i].y, players[i].id));
            }
        }

        init(players, centerPoints);
        calculate();
        // printClusterSizes(); 
        // sortClustersAndUpdateShortestPathsInCluster();
        // plotClusters();
    }

    
    //Initializes the process
    public void init(Point[] rats, List<ClusterPoint> centerPoints) 
    {
        //convert point to clusterpoint
    	this.points = ClusterPoint.pointToClusterPoint(rats);
        //create cluster with centerpoint as centroid and add to clusters
    	for (int i = 0; i < NUM_CLUSTERS; i++) 
        {
    		Cluster cluster = new Cluster(i);
    		ClusterPoint centroid = centerPoints.get(i);
    		cluster.setCentroid(centroid);
    		clusters.add(cluster);
    	}
    }

    //The process to calculate the K Means, with iterating method.
    public void calculate() 
    {
        boolean finish = false;
        int iteration = 0;
        
        // Add in new data, one at a time, recalculating centroids with each new one. 
        while(!finish) {
            //Clear cluster state
            clearClusters();
            
            List<ClusterPoint> lastCentroids = getCentroids();
            
            //Assign points to the closer cluster
            assignCluster();
            
            //Calculate new centroids.
            calculateCentroids();
            
            iteration++;
            
            List<ClusterPoint> currentCentroids = getCentroids();
            
            //Calculates total distance between new and old Centroids
            double distance = 0.0;
            for(int i = 0; i < lastCentroids.size(); i++) 
            {
                distance += ClusterPoint.distance(lastCentroids.get(i),currentCentroids.get(i));
            }

            // System.out.println("#################");
            // System.out.println("Iteration: " + iteration);
            // System.out.println("Centroid distances: " + distance);
            // System.out.println("#################");
            // plotClusters();
                        
            if(distance < 0.001) 
            {
                finish = true;
            }
        }
    }

    private void assignCluster()
    {
        double min = Double.MAX_VALUE;
        int cluster = 0;                 
        double distance = 0.0; 
        for(ClusterPoint point : this.points) 
        {
            min = Double.MAX_VALUE;
            for(int i = 0; i < NUM_CLUSTERS; i++) 
            {
                Cluster c = clusters.get(i);
                distance = ClusterPoint.distance(point, c.getCentroid());
                if(distance < min){
                    min = distance;
                    cluster = i;
                }
            }
            point.setCluster(cluster);
            clusters.get(cluster).addPoint(point);
        }
    }
    
    private void calculateCentroids() 
    {
        for(Cluster cluster : clusters) 
        {
            double sumX = 0;
            double sumY = 0;
            List<ClusterPoint> list = cluster.getPoints();
            int n_points = list.size();
            
            for(ClusterPoint point : list) 
            {
                sumX += point.getX();
                sumY += point.getY();
            }
            ClusterPoint centroid = cluster.getCentroid();
            if(n_points > 0) 
            {
                double newX = sumX / (double) n_points;
                double newY = sumY / (double) n_points;
                cluster.setCentroid(new ClusterPoint(newX, newY, cluster.getId(), centroid.getCluster()));
            }
        }
    }
    
	void plotClusters() 
    {
    	for (int i = 0; i < NUM_CLUSTERS; i++) 
        {
    		Cluster c = clusters.get(i);
    		c.plotCluster();
    	}
    }
    

    
    List<Cluster> sortClustersAndUpdateShortestPathsInCluster() 
    {
        //Collections.sort(clusters, Collections.reverseOrder());
        for (int i = 0; i < NUM_CLUSTERS; i++) {
            Cluster c = clusters.get(i);
            Collections.sort(c.getPoints(), Collections.reverseOrder());
        }
        return clusters;
    }

    private void clearClusters() {
    	for(Cluster cluster : clusters) {
    		cluster.clear();
    	}
    }
    
    private List<ClusterPoint> getCentroids() 
    {
    	List<ClusterPoint> centroids = new ArrayList<ClusterPoint>(NUM_CLUSTERS);
    	for(Cluster cluster : clusters) {
    		ClusterPoint aux = cluster.getCentroid();
    		ClusterPoint point = new ClusterPoint(aux.getX(),aux.getY(), cluster.getId());
    		centroids.add(point);
    	}
    	return centroids;
    }
    
    void printClusterSizes()
    {
        for (int i = 0; i < NUM_CLUSTERS; i++) 
        {
            Cluster c = clusters.get(i);
            System.out.println("Cluster : " + i + ", Size: "+ c.getPoints().size() );
        }
    }
}
