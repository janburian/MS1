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
    
    /** Poèet vygenerovaných lanovek */ 
    protected int cableCarsCounter = 0;
    
    /** Maximální délka fronty lyžaøù */
    int maxLengthSkiersQueue;
    
    /** Celková délka lana [m] */ 
    int ropeLength = 4_000; 
    
    /** Vzdálenost mezi kabinami na lanì [m]*/ 
    double distanceCableCars = (double) ropeLength / numberOfCableCars;
    
    
    Random random = new Random(9);
    double throughTime; 
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
        System.out.println("Number of generated cablecars: " + cableCarsCounter);
        System.out.println("Ratio of generated cablecars to number of cablecars: " + (double) cableCarsCounter / numberOfCableCars);
        
        System.out.println("Total number of skiers = " + numberOfSkiers);
        java.text.NumberFormat fmt = java.text.NumberFormat.getNumberInstance();
        fmt.setMaximumFractionDigits(2);
        System.out.println("Av.elapsed time = " + fmt.format(throughTime/numberOfSkiers));
        System.out.println("Maximum queue length of skiers = " + maxLengthSkiersQueue);
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
            if (maxLengthSkiersQueue < qLength) 
                maxLengthSkiersQueue = qLength;
            passivate();
            numberOfSkiers++;
            throughTime += time() - entryTime;
        }		
    }

    class CableCar extends Process { 

    	public CableCar(int capacity) {
    		cableCarCapacity = capacity; 
    	}
    	
    	/** Poèet volných míst v kabince */
    	protected int remainingPlaces = cableCarCapacity; 
   
        public void actions() { 
        	into(cableCarsQueue); 
            while (true) { 
                while (!skiersQueue.empty()) {
                    CableCar cableCar = (CableCar) cableCarsQueue.first(); // 1. dostupna lanovka
                    
                    double cableCarArrivalTime = time();
   
                    while (time() - cableCarArrivalTime > 20) {
                    	Skier served = (Skier) skiersQueue.first(); // prvni lyzar z fronty
                    	served.out(); // fronta bez jednoho lyzare # TODO: ppst, ze lyzar do lanovky z nejakeho duvodu nenastoupi
                        cableCar.remainingPlaces--; // lyzar nastoupi do kabinky
                        if (cableCar.remainingPlaces == 0) { // 
                        	 cableCar.out(); 
                        	 break; 
                        }  
                    }
                    cableCar.out();
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
    			cableCarsCounter++; 
    			hold(distanceCableCars * 0.1); // generování kabin závislé na vzdálenosti 
    												  // mezi jednotlivými kabinami 
    		}
    	}
    }

   
    public static void main(String args[]) {
        activate(new CableCarSimulation(40));
    } 
}
