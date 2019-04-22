package nachos.threads;
import nachos.ag.BoatGrader;

public class Boat
{
    static BoatGrader bg;
    
    public static void selfTest()
    {
	BoatGrader b = new BoatGrader();
	
	System.out.println("\n ***Testing Boats with only 2 children***");
	begin(0, 2, b);

//	System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
//  	begin(1, 2, b);

//  	System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
//  	begin(3, 3, b);
    }

    public static void begin( int adults, int children, BoatGrader b )
    {
	// Store the externally generated autograder in a class
	// variable to be accessible by children.
	bg = b;

	// Instantiate global variables here
	
	Oahu_Child = children;
	Molokai_Child = 0;
	Oahu_Adult = adult;
	Molokai_Adult = 0;
	Create Kthreads for Child;
	Create Kthreads for Adult;

	
	// Create threads here. See section 3.4 of the Nachos for Java
	// Walkthrough linked from the projects page.

	Runnable r = new Runnable() {
	    public void run() {
                SampleItinerary();
            }
        };
        KThread t = new KThread(r);
        t.setName("Sample Boat Thread");
        t.fork();

    }

    static void AdultItinerary()
    {
    	bg.AdultRowToMolokai();
        Oahu_Adult--;
        Molokai_Adult++;

	/* This is where you should put your solutions. Make calls
	   to the BoatGrader to show that it is synchronized. For
	   example:
	       bg.AdultRowToMolokai();
	   indicates that an adult has rowed the boat across to Molokai
	*/
    }

    static void ChildItinerary()
    {
    	while(Oahu_Adult >=1){
    	    if(Molokai_Child = 0){
    	    bg.ChildRowToMolokai();
    	    bg.ChildRideToMolokai();
    	    Oahu_Child--;
    	    Oahu_Child--;
    	    Molokai_Child++;
    	    Molokai_Child++;
    	}
    	if(Molokai_Child == 2){
    	bg.ChildRowToOahu();
    	Oahu_Child++;
    	Molokai_Child--;
    	Adult_Queue.wake();
    	}
    	if(Oahu_Child>=1 && Molokai_Child == 1){
    	    bg.ChildRowToOahu();
    	    Molokai_Children--;
    	    Oahu_Children++;
    	}

    	}
    	if(Oahu_Adult == 0){
    	    while(Oahu_Child>=1){
    	    bg.ChildRowToOahu();
    	    Molokai_Children--;
    	    Oahu_Children++;
    	    bg.ChildRowToMolokai();
    	       bg.ChildRideToMolokai();
    	       Oahu_Child--;
    	        Oahu_Child--;
    	        Molokai_Child++;
    	        Molokai_Child++;
    	}

    	}

    }

    static void SampleItinerary()
    {
	// Please note that this isn't a valid solution (you can't fit
	// all of them on the boat). Please also note that you may not
	// have a single thread calculate a solution and then just play
	// it back at the autograder -- you will be caught.
	System.out.println("\n ***Everyone piles on the boat and goes to Molokai***");
	bg.AdultRowToMolokai();
	bg.ChildRideToMolokai();
	bg.AdultRideToMolokai();
	bg.ChildRideToMolokai();
    }
    
}
