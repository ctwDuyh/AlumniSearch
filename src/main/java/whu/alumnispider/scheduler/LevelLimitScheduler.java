package whu.alumnispider.scheduler;

import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.scheduler.PriorityScheduler;
import us.codecraft.webmagic.scheduler.Scheduler;


public class LevelLimitScheduler extends PriorityScheduler {

    private int levelLimit;

    public LevelLimitScheduler(int levelLimit) {
        this.levelLimit = levelLimit;
    }

    public synchronized void push(Request request, Task task) {
        if (request.getExtra("_level") != null) {
            if (((Integer) request.getExtra("_level")) <= levelLimit) {
                super.push(request, task);
            }
        }
    }
}
