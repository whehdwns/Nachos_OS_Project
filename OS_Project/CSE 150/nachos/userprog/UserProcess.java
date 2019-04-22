package nachos.userprog;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;

import java.io.EOFException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
//import java.util.HashMap;
//import java.util.HashSet;
import java.util.LinkedList;

/**
 * Encapsulates the state of a user process that is not contained in its
 * user thread (or threads). This includes its address translation state, a
 * file table, and information about the program being executed.
 *
 * <p>
 * This class is extended by other classes to support additional functionality
 * (such as additional syscalls).
 *
 * @see	nachos.vm.VMProcess
 * @see	nachos.network.NetProcess
 */
public class UserProcess {


	/**
     * Allocate a new process.
     */

    public UserProcess() {
    	// OpenFile openFile[] = new OpenFile[16]; 
    	 
    
		int numPhysPages = Machine.processor().getNumPhysPages();
		pageTable = new TranslationEntry[numPhysPages];
		for (int i=0; i<numPhysPages; i++)
			pageTable[i] = new TranslationEntry(i,i, true,false,false,false);
		
		openFile = new OpenFile[16];
		openFile[0] = UserKernel.console.openForReading();
   	 	openFile[1] = UserKernel.console.openForWriting();
   	 	
   	 	id = cnt++;
   	 	statusexit =1;
    }
    
    /**
     * Allocate and return a new process of the correct class. The class name
     * is specified by the <tt>nachos.conf</tt> key
     * <tt>Kernel.processClassName</tt>.
     *
     * @return	a new process of the correct class.
     */
    public static UserProcess newUserProcess() {
	return (UserProcess)Lib.constructObject(Machine.getProcessClassName());
    }

    /**
     * Execute the specified program with the specified arguments. Attempts to
     * load the program, and then forks a thread to run it.
     *
     * @param	name	the name of the file containing the executable.
     * @param	args	the arguments to pass to the executable.
     * @return	<tt>true</tt> if the program was successfully executed.
     */
    public boolean execute(String name, String[] args) {
	if (!load(name, args))
	    return false;
	
	//new UThread(this).setName(name).fork();
	
	thread = new UThread(this);
	thread.setName(name).fork();

	return true;
    }

    /**
     * Save the state of this process in preparation for a context switch.
     * Called by <tt>UThread.saveState()</tt>.
     */
    public void saveState() {
    }

    /**
     * Restore the state of this process after a context switch. Called by
     * <tt>UThread.restoreState()</tt>.
     */
    public void restoreState() {
	Machine.processor().setPageTable(pageTable);
    }

    /**
     * Read a null-terminated string from this process's virtual memory. Read
     * at most <tt>maxLength + 1</tt> bytes from the specified address, search
     * for the null terminator, and convert it to a <tt>java.lang.String</tt>,
     * without including the null terminator. If no null terminator is found,
     * returns <tt>null</tt>.
     *
     * @param	vaddr	the starting virtual address of the null-terminated
     *			string.
     * @param	maxLength	the maximum number of characters in the string,
     *				not including the null terminator.
     * @return	the string read, or <tt>null</tt> if no null terminator was
     *		found.
     */
    public String readVirtualMemoryString(int vaddr, int maxLength) {
	Lib.assertTrue(maxLength >= 0);

	byte[] bytes = new byte[maxLength+1];

	int bytesRead = readVirtualMemory(vaddr, bytes);

	for (int length=0; length<bytesRead; length++) {
	    if (bytes[length] == 0)
		return new String(bytes, 0, length);
	}

	return null;
    }

    /**
     * Transfer data from this process's virtual memory to all of the specified
     * array. Same as <tt>readVirtualMemory(vaddr, data, 0, data.length)</tt>.
     *
     * @param	vaddr	the first byte of virtual memory to read.
     * @param	data	the array where the data will be stored.
     * @return	the number of bytes successfully transferred.
     */
    public int readVirtualMemory(int vaddr, byte[] data) {
	return readVirtualMemory(vaddr, data, 0, data.length);
    }

    /**
     * Transfer data from this process's virtual memory to the specified array.
     * This method handles address translation details. This method must
     * <i>not</i> destroy the current process if an error occurs, but instead
     * should return the number of bytes successfully copied (or zero if no
     * data could be copied).
     *
     * @param	vaddr	the first byte of virtual memory to read.
     * @param	data	the array where the data will be stored.
     * @param	offset	the first byte to write in the array.
     * @param	length	the number of bytes to transfer from virtual memory to
     *			the array.
     * @return	the number of bytes successfully transferred.
     */
    public int readVirtualMemory(int vaddr, byte[] data, int offset,int length) {
	Lib.assertTrue(offset >= 0 && length >= 0 && offset+length <= data.length);
		
    	byte[] memory = Machine.processor().getMemory();
    	if (vaddr < 0 || vaddr >= memory.length){
    			//return 0; //?
    	}

// for now, just assume that virtual addresses equal physical addresses
    	int amountBytes = 0; //# of bytes to be transferred (renamed for easier tracking)
		int index = Machine.processor().pageFromAddress(vaddr);
		int offset1 = Machine.processor().offsetFromAddress(vaddr);
		//int physaddr = pageTable[index].ppn*pageSize+offset1;	
		pageTable[index].used = true;
		
	    while(0 < length && offset < data.length){
			int amountBytesTransfer = Math.min(length, pageSize - offset1); //amount to add to return amount
	    	index = Machine.processor().pageFromAddress(vaddr); //+amountBytes
	    	offset1 = Machine.processor().offsetFromAddress(vaddr); //+amountBytes
			//physaddr = pageTable[index].ppn*pageSize+offset1;	
			
			int vPg = vaddr / 1024;
			
			int aOff = vaddr %1024;
			
			if (vPg >= pageTable.length || vPg < 0)
				break;
			
			TranslationEntry pgTblEnt = pageTable[vPg];
			
			if (pgTblEnt.valid == false)
				break;
			
			pgTblEnt.used = true;
			
			 
			int phPg = pgTblEnt.ppn;
			
			
			
			/*if(index<0||index>=pageTable.length){
				return 0;
			}
			if(pageTable[index].valid == false){
				return 0;
			}*/
			
			int diff = data.length - offset;
			int subDiff = Math.min(length, 1024 - aOff);
			int phAdd = (phPg * 1024) + aOff;
			int len = Math.min(diff, subDiff);
			System.arraycopy(memory, phAdd, data, offset, len); //offset = offset1+amountBytes
			pageTable[index].used = true;
			vaddr += len;
			offset += len;
			length -= len;
			amountBytes += len;
		}
		//int amount = Math.min(length, memory.length-vaddr);
		//System.arraycopy(memory, vaddr, data, offset, amount);
		
		//pageTable[index].used = false;
		return amountBytes;
	}

    /**
     * Transfer all data from the specified array to this process's virtual
     * memory.
     * Same as <tt>writeVirtualMemory(vaddr, data, 0, data.length)</tt>.
     *
     * @param	vaddr	the first byte of virtual memory to write.
     * @param	data	the array containing the data to transfer.
     * @return	the number of bytes successfully transferred.
     */
    public int writeVirtualMemory(int vaddr, byte[] data) {
	return writeVirtualMemory(vaddr, data, 0, data.length);
    }

    /**
     * Transfer data from the specified array to this process's virtual memory.
     * This method handles address translation details. This method must
     * <i>not</i> destroy the current process if an error occurs, but instead
     * should return the number of bytes successfully copied (or zero if no
     * data could be copied).
     *
     * @param	vaddr	the first byte of virtual memory to write.
     * @param	data	the array containing the data to transfer.
     * @param	offset	the first byte to transfer from the array.
     * @param	length	the number of bytes to transfer from the array to
     *			virtual memory.
     * @return	the number of bytes successfully transferred.
     */
   public int writeVirtualMemory(int vaddr, byte[] data, int offset,
			  int length) {
	   Lib.assertTrue(offset >= 0 && length >= 0 && offset+length <= data.length);
		
		byte[] memory = Machine.processor().getMemory();
		int trnsfr = 0, max = 1024;
		
		// for now, just assume that virtual addresses equal physical addresses
		if (vaddr < 0 || vaddr >= memory.length){
		  //return 0; //??
		}
		int amountBytes = 0; //# of bytes to be transferred (renamed for easier tracking)
		int index = Machine.processor().pageFromAddress(vaddr);
		int offset1 = Machine.processor().offsetFromAddress(vaddr);
		int physaddr = pageTable[index].ppn*pageSize+offset1;	
		pageTable[index].used = true;
		while(0<length && data.length > offset){
			int amountBytesTransfer = Math.min(length, pageSize - offset1);
			index = Machine.processor().pageFromAddress(vaddr);
			offset1 = Machine.processor().offsetFromAddress(vaddr);
			physaddr = pageTable[index].ppn*pageSize + offset1;
			
			if(index < 0 || index >= pageTable.length || pageTable[index].readOnly){
				return 0;
			}
			if(pageTable[index].valid==false){
				return 0;
			}
			
			System.arraycopy(data, offset, memory, physaddr, length);
			//System.arraycopy(data, offset1+amountBytes, memory, physaddr, 1);
			pageTable[index].used = true;
			pageTable[index].dirty = true;
			amountBytes += amountBytesTransfer;
			vaddr += amountBytesTransfer;
			offset += amountBytesTransfer;
			length -= amountBytesTransfer;
		}
	
	//int amount = Math.min(length, memory.length-vaddr);
	//System.arraycopy(data, offset, memory, vaddr, amount);
	
	pageTable[index].used = false;
	return amountBytes;
}

    /**
     * Load the executable with the specified name into this process, and
     * prepare to pass it the specified arguments. Opens the executable, reads
     * its header information, and copies sections and arguments into this
     * process's virtual memory.
     *
     * @param	name	the name of the file containing the executable.
     * @param	args	the arguments to pass to the executable.
     * @return	<tt>true</tt> if the executable was successfully loaded.
     */
    private boolean load(String name, String[] args) {
	Lib.debug(dbgProcess, "UserProcess.load(\"" + name + "\")");
	
	OpenFile executable = ThreadedKernel.fileSystem.open(name, false);
	if (executable == null) {
	    Lib.debug(dbgProcess, "\topen failed");
	    return false;
	}

	try {
	    coff = new Coff(executable);
	}
	catch (EOFException e) {
	    executable.close();
	    Lib.debug(dbgProcess, "\tcoff load failed");
	    return false;
	}

	// make sure the sections are contiguous and start at page 0
	numPages = 0;
	for (int s=0; s<coff.getNumSections(); s++) {
	    CoffSection section = coff.getSection(s);
	    if (section.getFirstVPN() != numPages) {
		coff.close();
		Lib.debug(dbgProcess, "\tfragmented executable");
		return false;
	    }
	    numPages += section.getLength();
	}

	// make sure the argv array will fit in one page
	byte[][] argv = new byte[args.length][];
	int argsSize = 0;
	for (int i=0; i<args.length; i++) {
	    argv[i] = args[i].getBytes();
	    // 4 bytes for argv[] pointer; then string plus one for null byte
	    argsSize += 4 + argv[i].length + 1;
	}
	if (argsSize > pageSize) {
	    coff.close();
	    Lib.debug(dbgProcess, "\targuments too long");
	    return false;
	}

	// program counter initially points at the program entry point
	initialPC = coff.getEntryPoint();	

	// next comes the stack; stack pointer initially points to top of it
	numPages += stackPages;
	initialSP = numPages*pageSize;

	// and finally reserve 1 page for arguments
	numPages++;
	if (!loadSections())
	    return false;

	// store arguments in last page
	int entryOffset = (numPages-1)*pageSize;
	int stringOffset = entryOffset + args.length*4;

	this.argc = args.length;
	this.argv = entryOffset;
	
	for (int i=0; i<argv.length; i++) {
	    byte[] stringOffsetBytes = Lib.bytesFromInt(stringOffset);
	    Lib.assertTrue(writeVirtualMemory(entryOffset,stringOffsetBytes) == 4);
	    entryOffset += 4;
	    Lib.assertTrue(writeVirtualMemory(stringOffset, argv[i]) ==
		       argv[i].length);
	    stringOffset += argv[i].length;
	    Lib.assertTrue(writeVirtualMemory(stringOffset,new byte[] { 0 }) == 1);
	    stringOffset += 1;
	}

	return true;
    }

    /**
     * Allocates memory for this process, and loads the COFF sections into
     * memory. If this returns successfully, the process will definitely be
     * run (this is the last step in process initialization that can fail).
     *
     * @return	<tt>true</tt> if the sections were successfully loaded.
     */
    protected boolean loadSections() {
	if (numPages > Machine.processor().getNumPhysPages()) {
	    coff.close();
	    Lib.debug(dbgProcess, "\tinsufficient physical memory");
	    return false;
	}
	
	pageTable = new TranslationEntry[numPages];
	
	for (int a = 0; a < numPages; a++)
	{
		int avail = UserKernel.addPg();
		
		if (avail < 0)
		{
			for (int b = 0; b < a; b++)
			{
				if (pageTable[b].valid == true)
				{
					UserKernel.remPg(pageTable[b].ppn);
					pageTable[b].valid = false;
				}
			}
			coff.close();
			return false;
		}
		
		pageTable[a] = new TranslationEntry(a, avail, true, false, false, false);
	}

	// load sections
	for (int s=0; s<coff.getNumSections(); s++) {
	    CoffSection section = coff.getSection(s);
	    
	    Lib.debug(dbgProcess, "\tinitializing " + section.getName()
		      + " section (" + section.getLength() + " pages)");

	    for (int i=0; i<section.getLength(); i++) {
		int vpn = section.getFirstVPN()+i;
		

		// for now, just assume virtual addresses=physical addresses
		
		section.loadPage(i, pageTable[vpn].ppn);
		
		if (section.isReadOnly() == true)
		{
			pageTable[vpn].readOnly = true;
		}
		
		//pageTable[vpn].readOnly = section.isReadOnly();
		//
	    }
	}
	
	return true;
    }

    /**
     * Release any resources allocated by <tt>loadSections()</tt>.
     */
    protected void unloadSections() {
		for (int n = 0; n < numPages; n++)
		{
			if (pageTable[n] != null)
			{
				pageTable[n].valid = false;
				
				UserKernel.remPg(pageTable[n].ppn);
			}
		}
		pageTable = null;
    }    

    /**
     * Initialize the processor's registers in preparation for running the
     * program loaded into this process. Set the PC register to point at the
     * start function, set the stack pointer register to point at the top of
     * the stack, set the A0 and A1 registers to argc and argv, respectively,
     * and initialize all other registers to 0.
     */
    public void initRegisters() {
	Processor processor = Machine.processor();

	// by default, everything's 0
	for (int i=0; i<processor.numUserRegisters; i++)
	    processor.writeRegister(i, 0);

	// initialize PC and SP according
	processor.writeRegister(Processor.regPC, initialPC);
	processor.writeRegister(Processor.regSP, initialSP);

	// initialize the first two argument registers to argc and argv
	processor.writeRegister(Processor.regA0, argc);
	processor.writeRegister(Processor.regA1, argv);
    }

    /**
     * Handle the halt() system call. 
     */
    private int handleHalt() {

	Machine.halt();
	
	Lib.assertNotReached("Machine.halt() did not halt machine!");
	return 0;
    }
    
    private int handleRead(int i, int buffer, int size ) {
        // if (i < 0 || size < 0)
            // return -1;
        // if ( i == 1 && openFile[i] == null)
          //   return -1;
         if (i< 0 || i >= 16 || openFile[i] == null || size <0)
             return -1;
         byte[] tempbuffer = new byte[size];
         int count = openFile[i].read(tempbuffer, 0, size);
         if (count == -1)
             return -1;
         
         return writeVirtualMemory(buffer,tempbuffer,0,count); 

 }
     
     
     private int handleWrite(int j, int buffer, int size) { 
 		//if (size == 0) 
 				//return 0;
 		//if(openFile[j] ==null)
 		//	return -1;
 		//if (j == 0 && openFile[j] == null)
 			//return -1;
    	 
    	 //OpenFile temp = openFile[j];
     	
     	if (j < 0 || j >= 16 || size < 0 || openFile[j]==null)
 			return -1;
     	
 		byte[] tempbuffer = new byte [size];
 		
         if (readVirtualMemory(buffer, tempbuffer, 0, size) == -1)
             return -1;
         //if (openFile[j].write(tempbuffer, 0, size) < size)
             //return -1;
         return openFile[j].write(tempbuffer,0,readVirtualMemory(buffer, tempbuffer, 0, size));
 	}
     
     
    

    private int handleCreate(int vaddr) {
		//if (vaddr < 0)
			//return -1;
		
		String fName = readVirtualMemoryString(vaddr, 256);
		int freeFD = -1; //empty file descriptor slot
		
		if (fName != null) {
			OpenFile executable = ThreadedKernel.fileSystem.open(fName, true);
			if (executable == null)
				return -1;
			//check for empty file descriptor slot to use
			for (int i=0; i<openFile.length; i++) {
				if (openFile[i] == null) {
					freeFD = i; //found one
					break;
				}
			}
		}
		else
			return -1;
		
		return freeFD; //return empty file descriptor slot or -1 if none
	}
	
	private int handleOpen(int vaddr) {
		if (vaddr < 0)
			return -1;
		
		String fName = readVirtualMemoryString(vaddr, 256);
		int freeFD = -1; //empty file descriptor slot
		
		if (fName != null) {
			OpenFile executable = ThreadedKernel.fileSystem.open(fName, false);
			if (executable == null)
				return -1;
			//check for empty file descriptor slot to use
			for (int i=0; i<openFile.length; i++) {
				if (openFile[i] == null) {
					openFile[i] = executable; //found one
					return i;
				}
			}
		}
			return -1;
	}

	private int handleClose(int vaddr)
	{		
		if (openFile[vaddr] == null)
			return -1;
		
		openFile[vaddr].close();
		
		openFile[vaddr] = null;
		return 0;
	}
	
	private int handleUnlink(int vaddr)
	{
		String fName = readVirtualMemoryString(vaddr, 256);
		
		for (int t = 0; t < openFile.length;t++)
		{
			OpenFile temp = openFile[t];
			
			if (temp == null)
				return -1;
			
			if (fName != temp.getName())
				return -1;
			
			openFile[t] = null;
		}
		
		if (ThreadedKernel.fileSystem.remove(fName) == false)
			return -1;
		else
		return 0;
	}
    
    private void handleExit(int status) {	
    	//coff.close(); //
    	for(int i=0; i< openFile.length; i++) {
    		if(openFile[i]!=null) {
    			//openFile[i]=null;
    			openFile[i].close();
    		}
    	}
    	
    	
    	
    	for (int q = 0; q < subTbl.keySet().size(); q++)
    		subTbl.get(q).pid = -1;
    	
    	this.statusexit=status;	
    	this.unloadSections();
    	
    	if(parent !=null) {
    		//parent.subTbl.remove(this);
    	}
    	if(this.id==0) { // || subTbl.isEmpty()) {
    		Kernel.kernel.terminate();
    	}
    	else {
    		KThread.currentThread().finish();
    	}
    }
    	
    private int handleExec(int file, int argc, int argv) {
    	String fName = readVirtualMemoryString(file, 256); 
    	if(argc >=0&&fName != null&&fName.endsWith(".coff")) {
    		
    		byte[] temp = new byte[4];
    		String[] locArgs = new String[argc];
        	for(int k=0; k<argc; k++){
        		
        			if(readVirtualMemory(argv+k*4, temp)==4) {
        				locArgs[k] = readVirtualMemoryString(Lib.bytesToInt(temp, 0), 256);
        				
        				if(locArgs[k]==null) {
    						return -1;
    					}
        			}
        				//int num = Lib.bytesToInt(temp, 0);
           				//arg[i] = readVirtualMemoryString(num, 256);
        					
        			}
        	
        	UserProcess dwn = new UserProcess();
        	
        	dwn.pid = this.id;
        	
        	if (dwn.execute(fName, locArgs))
        	{
        		subTbl.put(dwn.id,  dwn);
        		return dwn.id;
        	}
        	
    	}
    	
    	return -1;
    }
    
    
    private int handleJoin(int processID, int status) {
    	/*UserProcess child = null;
    	for(int i=0 ; i< subTbl.size(); i++) {
    		if(subTbl.get(i).id==id) {
    			child=subTbl.get(i);
    			break;
    		}
    	}
    	if(child==null || child.thread==null) {
    		return -1;
    	}
    	child.thread.join();
    	this.subTbl.remove(child);
    	byte [] childstatus = new byte [4];
    	childstatus=Lib.bytesFromInt(child.statusexit);
       	if(writeVirtualMemory(status, childstatus)==4) { 		
       		return 1;
       	}
       	else {
       		return 0;
       	}
       	*/
    	if(processID < 0 || status <0) {
    		return -1;
    	}
    	
    	if(subTbl.containsKey(processID)) {
    		UserProcess sub = subTbl.get(processID);
    		sub.thread.join();
    		subTbl.remove(processID);
    	 	if(writeVirtualMemory(status, Lib.bytesFromInt(sub.statusexit))==4) { 		
           		return 1;
           	}
           	else {
           		return 0;
           	}
    	}

    	else {
    		return -1;
    	}
       
    }
    
    public int getID() {
    	return id;
    }
    private UserProcess parent;
    private OpenFile[] openFile;
    protected int id;
    //private OpenFile[] fileDescriptors = new OpenFile[16];
    public int pid= -1;
    private static int cnt = 0;
    private UThread thread;
    private int statusexit;
    //private ArrayList<UserProcess> subTbl = new ArrayList<UserProcess>();
    

    private static final int
    syscallHalt = 0,
	syscallExit = 1,
	syscallExec = 2,
	syscallJoin = 3,
	syscallCreate = 4,
	syscallOpen = 5,
	syscallRead = 6,
	syscallWrite = 7,
	syscallClose = 8,
	syscallUnlink = 9;

    /**
     * Handle a syscall exception. Called by <tt>handleException()</tt>. The
     * <i>syscall</i> argument identifies which syscall the user executed:
     *
     * <table>
     * <tr><td>syscall#</td><td>syscall prototype</td></tr>
     * <tr><td>0</td><td><tt>void halt();</tt></td></tr>
     * <tr><td>1</td><td><tt>void exit(int status);</tt></td></tr>
     * <tr><td>2</td><td><tt>int  exec(char *name, int argc, char **argv);
     * 								</tt></td></tr>
     * <tr><td>3</td><td><tt>int  join(int pid, int *status);</tt></td></tr>
     * <tr><td>4</td><td><tt>int  creat(char *name);</tt></td></tr>
     * <tr><td>5</td><td><tt>int  open(char *name);</tt></td></tr>
     * <tr><td>6</td><td><tt>int  read(int fd, char *buffer, int size);
     *								</tt></td></tr>
     * <tr><td>7</td><td><tt>int  write(int fd, char *buffer, int size);
     *								</tt></td></tr>
     * <tr><td>8</td><td><tt>int  close(int fd);</tt></td></tr>
     * <tr><td>9</td><td><tt>int  unlink(char *name);</tt></td></tr>
     * </table>
     * 
     * @param	syscall	the syscall number.
     * @param	a0	the first syscall argument.
     * @param	a1	the second syscall argument.
     * @param	a2	the third syscall argument.
     * @param	a3	the fourth syscall argument.
     * @return	the value to be returned to the user.
     */
    public int handleSyscall(int syscall, int a0, int a1, int a2, int a3) {
	switch (syscall) {
	case syscallHalt:
	    return handleHalt();
	case syscallExit:
		 handleExit(a0);
	case syscallExec:
		return handleExec(a0, a1, a2);
	case syscallJoin:
		return handleJoin(a0, a1);
	case syscallCreate:
		return handleCreate(a0);
	case syscallOpen:
		return handleOpen(a0);
	case syscallRead:
		return handleRead(a0, a1, a2);
	case syscallWrite:
		return handleWrite(a0, a1, a2);
	case syscallClose:
		return handleClose(a0);	
	case syscallUnlink:
		return handleUnlink(a0);
	
	default:
	    Lib.debug(dbgProcess, "Unknown syscall " + syscall);
	    Lib.assertNotReached("Unknown system call!" + syscall);
	}
	return 0;
    }

    /**
     * Handle a user exception. Called by
     * <tt>UserKernel.exceptionHandler()</tt>. The
     * <i>cause</i> argument identifies which exception occurred; see the
     * <tt>Processor.exceptionZZZ</tt> constants.
     *
     * @param	cause	the user exception that occurred.
     */
    public void handleException(int cause) {
	Processor processor = Machine.processor();

	switch (cause) {
	case Processor.exceptionSyscall:
	    int result = handleSyscall(processor.readRegister(Processor.regV0),
				       processor.readRegister(Processor.regA0),
				       processor.readRegister(Processor.regA1),
				       processor.readRegister(Processor.regA2),
				       processor.readRegister(Processor.regA3)
				       );
	    processor.writeRegister(Processor.regV0, result);
	    processor.advancePC();
	    break;				       

	default:
	    Lib.debug(dbgProcess, "Unexpected exception: " +
		      Processor.exceptionNames[cause]);
	    Lib.assertNotReached("Unexpected exception");
	}
    }

    /** The program being run by this process. */
    protected Coff coff;	

    /** This process's page table. */
    protected TranslationEntry[] pageTable;
    /** The number of contiguous pages occupied by the program. */
    protected int numPages;

    /** The number of pages in the program's stack. */
    protected final int stackPages = 8;
    
    private int initialPC, initialSP;
    private int argc, argv;
    
    protected Hashtable<Integer, UserProcess> subTbl = new Hashtable<Integer, UserProcess>();
    
    private static final int pageSize = Processor.pageSize;
    private static final char dbgProcess = 'a';
    
  
}
