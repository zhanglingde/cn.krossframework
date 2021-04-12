package cn.krossframework.cat.test;

import cn.krossframework.state.ExecuteTask;
import cn.krossframework.state.StateGroup;
import cn.krossframework.state.StateGroupPool;
import cn.krossframework.state.WorkerManager;
import cn.krossframework.web.WebApplication;
import cn.krossframework.web.cat.CatTask;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

@SpringBootTest(classes = WebApplication.class)
@RunWith(SpringRunner.class)
public class CatTest {

    @Autowired
    WorkerManager workerManager;

    @Autowired
    StateGroupPool stateGroupPool;

    public void multiTest(long groupId) {
        ExecuteTask walkTask = new ExecuteTask(groupId, new CatTask(2), () -> {
            System.out.println("walk error");
        });

        ExecuteTask sleepTask = new ExecuteTask(groupId, new CatTask(3), () -> {
            System.out.println("sleep error");
        });

        ExecuteTask eatTask = new ExecuteTask(groupId, new CatTask(4), () -> {
            System.out.println("eat error");
        });

        workerManager.enter(new ExecuteTask(groupId, new CatTask(10), () -> {
            System.out.println("enter error");
        }));


        StateGroup stateGroup;
        while ((stateGroup = stateGroupPool.find(groupId)) == null || stateGroup.getCurrentWorkerId() == null) {

        }
        LinkedBlockingQueue<ExecuteTask> executeTasks = new LinkedBlockingQueue<>(3000);
        for (int i = 0; i < 1000; i++) {
            executeTasks.offer(walkTask);
        }
        for (int i = 0; i < 1000; i++) {
            executeTasks.offer(sleepTask);
        }
        for (int i = 0; i < 1000; i++) {
            executeTasks.offer(eatTask);
        }
        Thread t1 = new Thread() {
            @Override
            public void run() {
                while (!this.isInterrupted()) {
                    ExecuteTask poll = executeTasks.poll();
                    if (poll != null) {
                        workerManager.addTask(poll);
                    }
                }
            }
        };

        Thread t2 = new Thread() {
            @Override
            public void run() {
                while (!this.isInterrupted()) {
                    ExecuteTask poll = executeTasks.poll();
                    if (poll != null) {
                        workerManager.addTask(poll);
                    }
                }
            }
        };

        Thread t3 = new Thread() {
            @Override
            public void run() {
                while (!this.isInterrupted()) {
                    ExecuteTask poll = executeTasks.poll();
                    if (poll != null) {
                        workerManager.addTask(poll);
                    }
                }
            }
        };

        t1.start();
        t2.start();
        t3.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignore) {
        }
        workerManager.addTask(new ExecuteTask(groupId, new CatTask(1), () -> {
            System.out.println("stop fail");
        }));
        t1.interrupt();
        t2.interrupt();
        t3.interrupt();
    }

    @Test
    public void test() throws InterruptedException {
        for (long i = 1; i < 200; i++) {
            final long groupId = i;
            Thread.sleep(10);
            new Thread(() -> this.multiTest(groupId)).start();
        }
        new CountDownLatch(1).await();
    }
}
