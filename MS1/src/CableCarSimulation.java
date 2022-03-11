import javaSimulation.*;
import javaSimulation.Process;

public class CableCarSimulation extends Process {
	/** Poèet kabin */
    int numberOfCableCars;
       
    /** Poèet lyžaøù ve frontì */
    int numberOfSkiers;
    
    /** Simulaèní perioda */
    double simPeriod = 200; 
    
    /** Kapacita kabiny */
    int cableCarCapacity = 10; 
    
    /** Definice fronty lyžaøù */
    Head skiersQueue = new Head();
    
    /** Definice fronty kabin */
    Head cableCarsQueue = new Head();
    
    Random random = new Random(9);
    double throughTime; 
    int maxLength;
    long startTime = System.currentTimeMillis();
	
    
    CableCarSimulation(int n) { 
    	numberOfCableCars = n; 
    }

    public void actions() { 
    	/*
    	CableCar[] cableCars = new CableCar[numberOfCableCars]; 
    	
 
    	for (int i = 0; i < cableCars.length; i++) {
    		cableCars[i].into(cableCarsQueue);
    	}
    	*/
    	
    	activate(new CableCarGenerator());
        activate(new SkiersGenerator());
        
        hold(simPeriod + 1000000);	
        report();
    }
	
    void report() {
        System.out.println(numberOfCableCars + " cable cars simulation");
        System.out.println("No.of cars through the system = " + 
                           numberOfSkiers);
        java.text.NumberFormat fmt = java.text.NumberFormat.getNumberInstance();
        fmt.setMaximumFractionDigits(2);
        System.out.println("Av.elapsed time = " + 
                           fmt.format(throughTime/numberOfSkiers));
        System.out.println("Maximum queue length = " + maxLength);
        System.out.println("\nExecution time: " +
                           fmt.format((System.currentTimeMillis() 
                                       - startTime)/1000.0) +			   
                           " secs.\n");
    }
	
    
    class Skier extends Process {
        public void actions() {
            double entryTime = time();
            into(skiersQueue);
            int qLength = skiersQueue.cardinal();
            if (maxLength < qLength) 
                maxLength = qLength;
            passivate();
            numberOfSkiers++;
            throughTime += time() - entryTime;
        }		
    }

    class CableCar extends Process { 

    	public CableCar(int capacity) {
    		cableCarCapacity = capacity; 
    	}
    	
    	/** Poèet volných míst */
    	protected int remainingPlaces = cableCarCapacity; 
   
        public void actions() { 
        	into(cableCarsQueue); 
            while (true) { 
                while (!skiersQueue.empty()) {
                    Skier served = (Skier) skiersQueue.first(); // prvni lyzar z fronty
                    CableCar cableCar = (CableCar) cableCarsQueue.first(); // 1. dostupna lanovka  
   
                    served.out(); // fronta bez jednoho lyzare
                    cableCar.remainingPlaces--; 
                    
                    if (cableCar.remainingPlaces == 0) {
                    	cableCar = (CableCar) cableCarsQueue.suc(); 
                    }
                }
            }
        }
    }

    
    class SkiersGenerator extends Process {
        public void actions() {
             while (time() <= simPeriod) {
                  activate(new Skier());
                  hold(random.negexp(20/1.0)); // 20 lyzaru se stredni hodnotou rovne 1 minute? 
             }
        }
    }
    
    class CableCarGenerator extends Process {
    	public void actions() {
    		while (time() <= simPeriod) {
    			activate(new CableCar(cableCarCapacity));
    			hold(0.15); // 15s? 
    		}
    	}
    }

   
    public static void main(String args[]) {
        activate(new CableCarSimulation(40));
    } 
}
