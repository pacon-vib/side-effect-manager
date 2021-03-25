package foo;

import util.SideEffectManager;
import java.util.stream.IntStream;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.lang.InterruptedException;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.Collections;
import java.io.StringWriter;
import java.io.PrintWriter;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
        throws Exception
    {
        //System.out.println( "Hello World!" );

        //simpleTest();
        //objectTest();
        threadedTest(1000);
        //conflictTest();
        //recoveryTest();
    }

    public static void simpleTest()
        throws Exception
    {
        SideEffectManager fm = new SideEffectManager(){
                public void work(String key)
                    throws Exception
                {
                    System.out.println("Work " + key);
                }
                
                public void rollback(String key)
                    throws Exception
                {
                    System.out.println("Rollback " + key);
                }
            };
        
        fm.run("aaa", 10000);
        fm.run("aaa", 10000);
        fm.run("bbb", 10000);
        fm.run("aaa", 10000);
    }

    public static void objectTest()
        throws Exception
    {
        Quz quz = new Quz("AAA");
        quz.flop("aaa");
        quz.flop("aaa");
        quz.flop("bbb");
        quz.flop("aaa");
        quz.flop("bbb");
    }

    public static void threadedTest(int howManyThreads)
        throws Exception
    {
        List<Quz> quzzes = new ArrayList<Quz>();
        List<Thread> threads = new ArrayList<Thread>();
        for (String name : Arrays.asList(new String[]{"AAA", "BBB"})) {
            Quz quz = new Quz(name);
            quzzes.add(quz);
            for (int n : IntStream.range(0, howManyThreads).boxed().collect(Collectors.toList())) {
                threads.add(new Thread(new Runnable(){
                        public void run() {
                            Random r = new Random();
                            char c = (char)(r.nextInt(3) + 'a');

                            try {
                                quz.flop(Character.toString(c));
                            } catch (Exception e) {
                                String stackTrace;
                                {
                                    StringWriter sw = new StringWriter();
                                    PrintWriter pw = new PrintWriter(sw);
                                    e.printStackTrace(pw);
                                    stackTrace = sw.toString();
                                }
                                System.err.println("Exception in thread " + c + ": " + e.toString() + ". " + stackTrace);
                            }
                        }
                    }));
            }
        }
        Collections.shuffle(threads);
        threads.stream().forEach(t -> t.start());
        for (Thread t : threads) {
            t.join();
        }
        //System.out.println("Yo");
    }

    public static void conflictTest()
        throws Exception
    {
        Quz quz = new Quz("AAA");
        int howManyThreads = 10;
        List<Thread> threads = new ArrayList<Thread>();
        for (int n : IntStream.range(0, howManyThreads).boxed().collect(Collectors.toList())) {
            threads.add(new Thread(new Runnable(){
                    public void run() {
                        char c = 'a';
                        try {
                            quz.flop(Character.toString(c));
                        } catch (Exception e) {
                            System.err.println("Exception in thread " + c + ": " + e.toString() + ".");
                        }
                    }
                }));
        }
        Collections.shuffle(threads);
        threads.stream().forEach(t -> t.start());
        for (Thread t : threads) {
            t.join();
        }
    }

    /*
     * Intended to demonstrate that if a thread waits for a thread that dies 
     * without completing successfully, then the first thread will try to 
     * execute the workload itself. (This is why there is a big `while (true)`
     * loop in `SiteEffectManager::run()`.
     */
    public static void recoveryTest()
        throws Exception
    {
        Quz quz = new Quz("AAA");
        int howManyThreads = 2;
        List<Thread> threads = new ArrayList<Thread>();
        for (int n : IntStream.range(0, howManyThreads).boxed().collect(Collectors.toList())) {
            threads.add(new Thread(new Runnable(){
                    public void run() {
                        char c = 'a';
                        try {
                            quz.flop(Character.toString(c));
                        } catch (Exception e) {
                            System.err.println("Exception in thread " + c + ": " + e.toString() + ".");
                        }
                    }
                }));
        }
        Collections.shuffle(threads);
        threads.stream().forEach(t -> t.start());
        for (Thread t : threads) {
            t.join();
        }
    }
}
