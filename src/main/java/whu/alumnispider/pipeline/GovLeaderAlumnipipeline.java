package whu.alumnispider.pipeline;

import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.*;
import whu.alumnispider.DAO.AlumniDAO;

public class GovLeaderAlumnipipeline implements Pipeline{
    public AlumniDAO alumniDAO;
    @Override
    public void process(ResultItems resultItems, Task task) {
    }

}
