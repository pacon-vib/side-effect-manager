package foo;

import util.SideEffectManager;
import java.util.Random;


public class Quz
{
    public String name;
    private SideEffectManager fm;

    private Random r = new Random();
    
    public Quz(String name)
    {
        this.name = name;
        this.fm = new SideEffectManager(){
                /*
                 * This is the "workload"
                 */
                public void work(String key)
                    throws Exception
                {
                    Thread.sleep(1000);
                    if (r.nextInt(3) == 0) {
                        throw new Exception("lol " + key + ".");
                    }
                    Quz.this.print(key);
                }

                /*
                 * This rolls back any failed or maybe-failed instances of the workload
                 */
                public void rollback(String key)
                    throws Exception
                {
                    //System.err.println("Rolling back " + key + ".");
                    Quz.this.retract(key);
                }
            };
    }

    public void print(String key)
    {
        System.out.println("Yes " + this.name + " " + key + ".");
    }

    public void retract(String key)
    {
        System.out.println("No " + this.name + " " + key + ".");
    }

    public void flop(String key)
        throws Exception
    {
        this.fm.run(key, 10000);
    }
}
