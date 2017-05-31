package demo.lumen;

import javax.websocket.Session;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by lumen on 2017/5/31.
 * usage: 定期向连接的所有页面推送相同的消息
 */
public class MsgPusher {
    //singleton get pusher
    private static class MsgPusherHolder{
        private static final MsgPusher INSTANCE = new MsgPusher();
    }

    public static MsgPusher getInstance(){
        return MsgPusherHolder.INSTANCE;
    }



    // sessions 发送消息
    List<Session> sessions = new ArrayList<>();
    // push thread
    PushRunable pushRunable = new PushRunable();
    Thread pushThread = new Thread(pushRunable);

    // add session
    public void add(Session session){
        sessions.add(session);
    }
    // remove session
    public void remove(Session session){
        sessions.remove(session);
    }
    // start send msg
    public void start(){
        pushRunable.opened = true;
        if(!pushThread.isAlive()){
            pushThread = new Thread(pushRunable);
            pushThread.start();
        }
    }

    public void stop(){
        pushRunable.opened = false;
    }

    private class PushRunable implements Runnable{
        final int PERIOD = 1000;
        //消息发送周期
        int period = PERIOD;
        //循环推送是否开启
        boolean opened = true;
        int i = 1;
        @Override
        public void run() {
            while(opened) {
                if(sessions.size() <= 0){
                    /* sessions.size() = 0 double the wait time , when wait too long ,break the loop, stop push */
                    period *= 2;
                    if(period > 8000){
                        opened = false;
                        break;
                    }else{
                        try {
                            Thread.sleep(period);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        continue;
                    }
                }else{
                    period = PERIOD;
                }
                /* get msg */
                String msg = i++ + "";
                /* send msg */
                for (Session s : sessions) {
                    try {
                        s.getBasicRemote().sendText(msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    /* remove the error session from list */
                        sessions.remove(s);
                    }
                }

                /* push over sleep period */
                try {
                    Thread.sleep(period);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
