package mapper;

import android.graphics.PointF;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import lab4_204_01.uwaterloo.ca.lab4_204_01.MainActivity;

/**
 * Derived from Dijkstra’s algorithm for shortest distance. Treats the edges of the walls as nodes. Determines the shortest distance
 * from source to destination point with respect to the wall nodes.
 *
 * Algorithm: starting from the initial node try to find a direct path to the end node. If doable, that must be the
 * shortest path. If wall in-between, move to all subsequent nodes with clear path and try to find direct path again.
 * Runs recursively until clear path to end node is found, distance is compared to other possible routes to determine minimum distance.
 *
 */
public class PathFinder {

    private PointF startP;
    private PointF endP;

    private NavigationalMap map;

    private TreeMap<String, PointF> treeMap;

    private ArrayList<PointF> nodes;

    public float minDistance;
    public List<PointF> minPath;

    public PathFinder(){}

    public List<PointF> test(){
        //FOR TESTING//
        startP.x = 11.24f;
        startP.y = 6.62f;

        endP.x = 12.06f;
        endP.y = 12.7f;

        treeMap.put("Start", startP);
        treeMap.put("End", endP);

        List<PointF> path = new ArrayList<PointF>();
        path.add(startP);

       /* nodes.add(new PointF(2,3));
        nodes.add(new PointF(3,1));
        nodes.add(new PointF(4, 0));*/

        //populateNodes();
        //traverseNodes(treeMap, "Start", buildTree(), path, 0.0f);

        Log.e("MIN DISTANCE", "" + minDistance);
        return minPath;
    }

    public List<PointF> findShortestPath(PointF start, PointF end, NavigationalMap userMap){
        treeMap = new TreeMap<String, PointF>();
        startP = new PointF();
        endP = new PointF();
        nodes = new ArrayList<PointF>();
        minDistance = Float.MAX_VALUE;
        minPath = new ArrayList<PointF>();

        startP = start;
        endP = end;
      /*  startP = new PointF(12.34f, 14.76f);
        endP = new PointF(19.0f, 15.26f);*/
        map = userMap;

        treeMap.put("Start", startP);
        treeMap.put("End", endP);

        List<PointF> path = new ArrayList<PointF>();
        path.add(startP);

        populateNodes(startP, endP);
        traverseNodes(treeMap, "Start", buildTree(), path, 0.0f);

        Log.e("minPath", "" + minPath);
        minPath.add(endP);
        return minPath;
    }

    public boolean comparePoint(PointF a, PointF b){
        if(((Math.abs(a.x - b.x) < 0.5) && Math.abs(a.y - b.y) < 0.5)){
            return true;
        }
        else{
            return false;
        }
    }

    private void populateNodes(PointF a, PointF b){
        List<InterceptPoint> walls;
        walls = map.calculateIntersections(a, b);

        List<InterceptPoint> wallsTBR = new ArrayList<InterceptPoint>();
        for(int i = 0; i < walls.size(); i++){
            InterceptPoint tmpwall = walls.get(i);
            for(int j = 0; j < walls.size(); j++){
                if( i != j ){
                    if(comparePoint(tmpwall.getLine().start, walls.get(j).getLine().start) && comparePoint(tmpwall.getLine().end, walls.get(j).getLine().end)){
                        if(tmpwall.getLine().length() > walls.get(j).getLine().length()){
                            wallsTBR.add(walls.get(j));
                        }
                        else if(tmpwall.getLine().length() < walls.get(j).getLine().length()){
                            wallsTBR.add(tmpwall);
                        }
                    }
                }
            }
        }

        for(int i = 0; i < wallsTBR.size(); i++){
            walls.remove(wallsTBR.get(i));
        }

        for (int i = 0; i < walls.size(); i++) {
            PointF startE = walls.get(i).getLine().start;
            PointF endE = walls.get(i).getLine().end;
                if(startE.x < 3 || startE.x > 25 ||startE.y < 3 || startE.y > 21){}
                else{
                    nodes.add(startE);
                }

                if(endE.x < 3 || endE.x > 25 ||endE.y < 3 || endE.y > 21){}
                else{
                    nodes.add(endE);
                }
        }

        Log.e("WALLS SIZE", "" + walls.size());
        Log.e("NODES", "" + nodes);
        Log.e("NODES SIZE", "" + nodes.size());
        Log.e("WALLS", "" + walls);
    }

    private ArrayList<String> buildTree(){
      //  treeMap.clear();
        ArrayList<String> names = new ArrayList<String>();
        for(int i = 0; i < nodes.size(); i ++){
            treeMap.put("Node" + i, nodes.get(i));
            names.add("Node" + i);
        }
        return names;
    }

    //helper function calculates distance between two points
    //returns -1 if walls in-between
    public float calculateDistance(PointF start, PointF end){
        List<InterceptPoint> walls = map.calculateIntersections(start, end);
        List<InterceptPoint> wallsTBR = new ArrayList<InterceptPoint>();

        for(int i = 0; i < walls.size(); i++){
            if(comparePoint(walls.get(i).getLine().start, start) || comparePoint(walls.get(i).getLine().end, start)){
                wallsTBR.add(walls.get(i));
            }
        }

        for(int j = 0; j < walls.size(); j++){
            if(comparePoint(walls.get(j).getLine().start, end) || comparePoint(walls.get(j).getLine().end, end)){
                wallsTBR.add(walls.get(j));
            }
        }

        for(int i = 0; i < wallsTBR.size(); i++){
            walls.remove(wallsTBR.get(i));
        }

        if( walls.size() > 0 ){
            return -1;
        }
        else {
            return (float) Math.abs(Math.sqrt((end.x - start.x) * (end.x - start.x) + (end.y - start.y) * (end.y - start.y)));
        }
    }


    int count = 0;
    //recursively move through nodes to determine the shortest distance
    //if the traversal intersects a wall, ignore it
    private void traverseNodes(TreeMap<String, PointF> tree, String source,
                               ArrayList<String> listNodes, List<PointF> path, float dTravelled) {
        if(tree.isEmpty() || path.isEmpty()){
            return;
        }

        float toEnd = calculateDistance(tree.get(source), treeMap.get("End"));
        //direct path from node to the endpoint
        if(toEnd != -1){
            if(dTravelled + toEnd < minDistance){
                minPath.clear();
                for(int i = 0; i < path.size(); i++)
                    minPath.add(path.get(i));

                minDistance = dTravelled + toEnd;
            }
        }
        //no direct path, try other paths
        for(int i = 0; i < listNodes.size(); i ++){
            float distance = calculateDistance(tree.get(source), tree.get(listNodes.get(i)));
            if(distance != -1) {
                List<PointF> tmpPath = new ArrayList<PointF>();
                for(int j = 0; j < path.size(); j++){
                    tmpPath.add(path.get(j));
                }

                float tmpDistance = dTravelled;
                tmpPath.add(tree.get(listNodes.get(i)));
                tmpDistance += distance;
                String tmpSource = listNodes.get(i);
        //        populateNodes(treeMap.get(listNodes.get(i)), endP);
                ArrayList<String> tmpList = new ArrayList<String>();
                for(int j = 0; j < listNodes.size(); j++){
                    tmpList.add(listNodes.get(j));
                }
                tmpList.remove(listNodes.get(i));
                traverseNodes(tree, tmpSource, tmpList, tmpPath, tmpDistance);
            }
        }
    }
}

