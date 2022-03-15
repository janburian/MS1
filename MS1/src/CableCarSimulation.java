import javaSimulation.*;
import javaSimulation.Process;

public class CableCarSimulation extends Process {
	/** Poèet kabin */
    int numberOfCableCars;
    
    /** Kapacita kabiny [poèet osob] */
    int cableCarCapacity = 10; 
    
    /** Celková délka lana [m] */ 
    int ropeLength = 4_000; 
    
    /** Definice fronty kabin */
    Head cableCarsQueue = new Head();
    
    /** Poèet vygenerovaných kabin */ 
    protected int cableCarsCounter = 0;
       
    
    /** Poèet lyžaøù, kteøí prošli frontou */
    int numberOfSkiers;
    
    /** Definice fronty lyžaøù */
    Head skiersQueue = new Head();
    
    /** Maximální délka fronty lyžaøù */
    int maxLengthSkiersQueue;
    
    
    /** Simulaèní perioda */
    double simPeriod = 10;
    
    /** Náhodná promìnná pro generování lyžaøù do fronty se seedem rovno 9 */
    Random random = new Random(9);
    
    /** Celková doba trvání všech prùchodù lidí ve frontì */
    double throughTime; 
    
    /** Èas zaèátku simulace */
    long startTime = System.currentTimeMillis();
    
    /** Konstruktor tøídy CableCarSimulation */
    CableCarSimulation(int n) { 
    	numberOfCableCars = n; 
    }
  
    
    public void actions() { 
    	activate(new SkiersGenerator());
    	activate(new CableCarGenerator());
        
        hold(simPeriod + 1000000);	
        report();
    }
	
    void report() {
        System.out.println(numberOfCableCars + " cable cars simulation");
        System.out.println("Number of generated cablecars: " + cableCarsCounter);
        System.out.println("Ratio of generated cablecars to total number of cablecars: " + (double) cableCarsCounter / numberOfCableCars);
        
        System.out.println("Total number of skiers = " + numberOfSkiers);
        java.text.NumberFormat fmt = java.text.NumberFormat.getNumberInstance();
        fmt.setMaximumFractionDigits(2);
        System.out.println("Average waiting queue time = " + fmt.format(throughTime/numberOfSkiers));
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
            numberOfSkiers++;
            passivate();
            throughTime += time() - entryTime;
        }		
    }

    class CableCar extends Process {
    	
    	public CableCar(int capacity) {
    		cableCarCapacity = capacity; 
    	}
    	
    	/** Poèet volných míst v kabince */
    	protected int remainingPlaces; 
    	
        public void actions() { 
        	into(cableCarsQueue); 
   
            while (!skiersQueue.empty()) { 
                CableCar cableCar = (CableCar) cableCarsQueue.first(); // 1. dostupna lanovka
                cableCar.remainingPlaces = cableCarCapacity;
                    
	            double cableCarArrivalTime = time();
	            double cableCarDepartureTime = cableCarArrivalTime + 20; 
	            
	            while (time() < cableCarDepartureTime) { // cas ve stanici 
	            	Skier served = (Skier) skiersQueue.first(); // prvni lyzar z fronty
	            	served.out(); // fronta bez jednoho lyzare # TODO: ppst, ze lyzar do lanovky z nejakeho duvodu nenastoupi
	            	cableCar.remainingPlaces--; // lyzar nastoupi do kabinky
			        if (cableCar.remainingPlaces == 0) {
			        	cableCar.out(); // odstraneni z fronty, pokud jiz nejsou volna mista
			        	break; 
			        }  
	            }	
			   cableCar.out(); // odstraneni z fronty, pokud jiz kabinka vyjela ze stanice
            }
        }
    }

    
    class SkiersGenerator extends Process {
        public void actions() {
             while (time() <= simPeriod) {
                  activate(new Skier());
                  hold(random.negexp(10/1.0)); // 10 lyzaru se stredni hodnotou rovne 1 minute 
             }
        }
    }
    
    class CableCarGenerator extends Process {
    	double distanceCableCars = (double) ropeLength / numberOfCableCars;
    	double timeConstant = 0.1; 
    	double generatorPeriod = distanceCableCars * timeConstant; 
    	
    	public void actions() {
    		while (time() <= simPeriod) {
    			activate(new CableCar(cableCarCapacity));
    			cableCarsCounter++; 
    			hold(generatorPeriod); // pravidelne generování kabin, závislé na vzdálenosti 
    								   // mezi jednotlivými kabinami 
    		}
    	}
    }

   
    public static void main(String args[]) {
        activate(new CableCarSimulation(40));
    } 
}
