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
    
    /** Poèet vygenerovanych kabin */ 
    protected int cableCarsCounter = 0;
       
    
    /** Poèet lyzaru, kteri prosli frontou */
    int numberOfSkiers;
    
    /** Definice fronty lyzarù */
    Head skiersQueue = new Head();
    
    /** Maximalni delka fronty lyzaru */
    int maxLengthSkiersQueue;
    
    
    /** Doba simulace */
    double simPeriod = 600; // 1200 s
    
    /** Nahodna promenna pro generovani lyzaru do fronty s nasadou rovno 9 */
    Random random = new Random(9);
    
    /** Celková doba trvani vsech pruchodù lidi ve fronte */
    double throughTime; 
    
    /** Cas zacatku simulace */
    long startTime = System.currentTimeMillis();
    
    /** Konstruktor tridy CableCarSimulation */
    CableCarSimulation(int n) { 
    	numberOfCableCars = n; 
    }
  
    
    public void actions() { 
    	activate(new CableCarGenerator());
    	activate(new SkiersGenerator());
    	
        hold(simPeriod + 10000);	
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
    	double desiredStandardDeviation = 0.5; // rozptyl normalniho rozdeleni 
    	double desiredMean = 5; // stredni hodnota normalniho rozdeleni (stredni doba nastupu lyzare)
    	
        public void actions() {
            double entryTime = time();
            into(skiersQueue);
            numberOfSkiers++;
            int qLength = skiersQueue.cardinal();
            if (maxLengthSkiersQueue < qLength) 
                maxLengthSkiersQueue = qLength;
       
            while (cableCarsQueue.empty() || this != skiersQueue.first()) { // lyzar neni prvni nebo nema kam nastoupit
            	passivate();
            }
            
            CableCar cableCar = (CableCar) cableCarsQueue.first(); // nastoupi do prvni dostupne kabiny
            double enteringTime = random.nextGaussian() * desiredStandardDeviation + desiredMean; // doba nastupu lyzare
            
            if (cableCar.remainingPlaces > 0) { // kabina ma volna mista
	        	cableCar.remainingPlaces--; 
	        	hold(enteringTime); 
	        	out(); // prvni lyzar odstranen z fronty
            	
	        	if (cableCar.remainingPlaces == 0) // kabina je plna
	        		activate(cableCar);
            	
            	Skier successor = (Skier) skiersQueue.first(); // nasledujici lyzar ve fronte, ktery je ted prvni na rade
                if (successor != null) // pokud je dalsi lyzar ve fronte
                   activate(successor); // aktivuji dalsiho lyzare ve fronte
             }
            throughTime += time() - entryTime;
        }
    }

    class CableCar extends Process {
    	
    	/** Poèet volnych mist v kabince */
    	protected int remainingPlaces; 
    	
    	/** Doba lanovky ve stanici (doba, kdy je mozne do lanovky nastoupit) */
    	protected double timeInStation = 40; // s
    	
    	public CableCar(int capacity) {
    		cableCarCapacity = capacity;  
    		remainingPlaces = cableCarCapacity; 
    	}
    	
        public void actions() { 
        	into(cableCarsQueue);
	        Skier served = (Skier) skiersQueue.first(); // prvni lyzar z fronty
	        activate(served); 
	        hold(timeInStation); // kabinka ceka ve stanici urcitou dobu
	        if (remainingPlaces > 0 && !(skiersQueue.empty())) 
	        	passivate();
	        else 
	        	out(); // po uplynute dobe je prvni kabinka odstranena z fronty
        }
    }

    
    class SkiersGenerator extends Process {
        public void actions() {
             while (time() <= simPeriod) {
                  activate(new Skier());
                  hold(random.negexp(5/60.0)); // 5 lyzaru se stredni hodnotou rovne 1 minute (= 60 sekund) 
             }
        }
    }
    
    
    class CableCarGenerator extends Process {
    	double distanceCableCars = (double) ropeLength / numberOfCableCars;
    	double timeConstant = 0.4; 
    	double generatorPeriod = distanceCableCars * timeConstant; // s
    	
    	public void actions() {
    		while (time() <= simPeriod) {
    			activate(new CableCar(6));
    			cableCarsCounter++; 
    			hold(generatorPeriod); // pravidelne generovani kabin, zavisle na vzdalenosti 
    								   // mezi jednotlivymi kabinami 
    		}
    	}
    }

   
    public static void main(String args[]) {
        activate(new CableCarSimulation(30));
    } 
    
}
