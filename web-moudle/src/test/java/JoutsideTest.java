import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class JoutsideTest {
    private static ScheduledExecutorService sec = Executors.newScheduledThreadPool(4);
    private static Queue<String> queue = new ConcurrentLinkedQueue<>();

    @SneakyThrows
    public static void main(String[] args) {
        List<String> list=new ArrayList<>();
        list.add("1");
        list.add("2");
        list.add("3");
        list= list.subList(0,list.size()-1);
        System.out.println(list);
    }


}
