import javaSimulation.*;
import javaSimulation.Process;

public class CableCarSimulation extends Process {
	/** Pocet kabin */
    int numberOfCableCars;
    
    /** Kapacita kabiny [pocet osob] */
    int cableCarCapacity; 
    
    /** Celkova delka lana [m] */ 
    int ropeLength = 4_000; 
    
    /** Definice fronty kabin */
    Head cableCarsQueue = new Head();
    
    /** Po�et vygenerovanych kabin */ 
    protected int cableCarsCounter = 0;
       
    
    /** Po�et lyzaru, kteri prosli frontou */
    int numberOfSkiers;
    
    /** Definice fronty lyzar� */
    Head skiersQueue = new Head();
    
    /** Maximalni delka fronty lyzaru */
    int maxLengthSkiersQueue;
    
    
    /** Simulacni perioda */
    double simPeriod = 1; 
    
    /** Nahodna promenna pro generovani lyzaru do fronty s nasadou rovno 9 */
    Random random = new Random(9);
    
    /** Celkov� doba trvani vsech pruchod� lidi ve fronte */
    double throughTime; 
    
    /** Cas zacatku simulace */
    long startTime = System.currentTimeMillis();
    
    /** Konstruktor tridy CableCarSimulation */
    CableCarSimulation(int n) { 
    	numberOfCableCars = n; 
    }
  
    
    public void actions() { 
    	activate(new SkiersGenerator());
    	activate(new CableCarGenerator());
        
        hold(simPeriod + 1000);	
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
            numberOfSkiers++;
            int qLength = skiersQueue.cardinal();
            if (maxLengthSkiersQueue < qLength) 
                maxLengthSkiersQueue = qLength;
            passivate();
            throughTime += time() - entryTime;
            
            CableCar cableCar = (CableCar) cableCarsQueue.first(); // nastoupi do prvni dostupne kabiny
            
            if (cableCar.remainingPlaces > 0) {
            	cableCar.remainingPlaces--; 
            	out(); // prvni lyzar odstranen z fronty
            	Skier successor = (Skier) skiersQueue.first(); // nasledujici lyzar ve fronte, ktery je ted prvni na rade
            	if (successor != null) // pokud je dalsi lyzar ve fronte
            		activate(successor); // aktivuji dalsiho lyzare ve fronte
            }
            
            else if (cableCar.remainingPlaces == 0) { // kabinka je plna
            	 cableCar.out(); 
            	 CableCar cableCarNext = (CableCar) cableCarsQueue.first(); // dalsi kabinka
                 activate(cableCarNext); //?
            }
          }
        }		

    class CableCar extends Process {
    	
    	/** Po�et volnych mist v kabince */
    	protected int remainingPlaces; 
    	
    	/** Doba lanovky ve stanici (doba, kdy je mozne do lanovky nastoupit) */
    	protected double timeInStation = 20; 
    	
    	public CableCar(int capacity) {
    		cableCarCapacity = capacity;  
    		remainingPlaces = cableCarCapacity; 
    	}
    	
        public void actions() { 
        	into(cableCarsQueue);
	        Skier served = (Skier) skiersQueue.first(); // prvni lyzar z fronty
	        activate(served); 
	        hold(timeInStation); // kabinka ceka ve stanici urcitou dobu
	        out(); // po uplynute dobe je prvni kabinka odstranena z fronty 
        }
    }

    
    class SkiersGenerator extends Process {
        public void actions() {
             while (time() <= simPeriod) {
                  activate(new Skier());
                  hold(random.negexp(30/1.0)); // 30 lyzaru se stredni hodnotou rovne 1 minute 
             }
        }
    }
    
    class CableCarGenerator extends Process {
    	double distanceCableCars = (double) ropeLength / numberOfCableCars;
    	double timeConstant = 0.1; 
    	double generatorPeriod = distanceCableCars * timeConstant; 
    	
    	public void actions() {
    		while (true) {
    			activate(new CableCar(10));
    			cableCarsCounter++; 
    			hold(generatorPeriod); // pravidelne generovani kabin, zavisle na vzdalenosti 
    								   // mezi jednotlivymi kabinami 
    		}
    	}
    }

   
    public static void main(String args[]) {
        activate(new CableCarSimulation(40));
    } 
}
