package msd.benjones;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Router {

    private HashMap<Router, Integer> distances;
    private String name;
    public Router(String name) {
        this.distances = new HashMap<>();
        this.name = name;
    }

    public void onInit() throws InterruptedException {

		//TODO: IMPLEMENT ME
		//As soon as the network is online,
		//fill in your initial distance table and broadcast it to your neighbors
        distances.put(this, 0);
        // 获取所有邻居
        Set<Neighbor> neighbors = Network.getNeighbors(this);
        for (Neighbor neighbor : neighbors) {
            // 设置到每个邻居的距离
            distances.put(neighbor.router, neighbor.cost);
            // 构建并发送初始距离消息到每个邻居
            Message message = new Message(this, neighbor.router, distances);
            Network.sendDistanceMessage(message);
        }


    }

    public void onDistanceMessage(Message message) throws InterruptedException {
		//update your distance table and broadcast it to your neighbors if it changed
        boolean updated = false;
        for (Map.Entry<Router, Integer> entry : message.distances.entrySet()) {
            Router source = entry.getKey();
            int newDist = entry.getValue() + this.distances.getOrDefault(message.sender, Integer.MAX_VALUE);
            if (newDist < this.distances.getOrDefault(source, Integer.MAX_VALUE)) {
                this.distances.put(source, newDist);
                updated = true;
            }
        }
        if (updated) {
            // 如果距离表更新，则向所有邻居广播新的距离表
            for (Neighbor neighbor : Network.getNeighbors(this)) {
                Message newMessage = new Message(this, neighbor.router, new HashMap<>(this.distances));
                Network.sendDistanceMessage(newMessage);
            }
        }
    }


    public void dumpDistanceTable() {
//        System.out.println("router: " + this);
//        for(Router r : distances.keySet()){
//            System.out.println("\t" + r + "\t" + distances.get(r));
//        }
    }

    @Override
    public  String toString(){
        return "Router: " + name;

    }
}
