package de.jk.bit.util;



/*
rightAligned and LeftAligned.

When converting the bit sequence from/to byte [], it is often the case that the number of bits is not a multiple of 8.
For example, 12-bit sequence 101000111001 to be converted to byte []. 
It can be 16 bit with trailing zeros fill [10100011 10010000] - leftAligned 
or with leading zeros [00001010 00111001] - rightAligned.

 */



import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Iterator;

import de.jk.bit.util.BitSequence.ALIGN;
import de.jk.bit.util.BitSequence.GROUP;



/**
 * Collect bits in a sequence.
 * </br>
 * </br>
 * Align LEFT or RIGHT.
 * </br>
 * When converting the bit sequence from/to byte [], it is often the case that the number of bits is not a multiple of 8.
 * For example, 12-bit sequence 101000111001 to be converted to byte []. 
 * It can be 16 bit with trailing zeros fill [10100011 10010000] - left aligned 
 * or with leading zeros [00001010 00111001] - right aligned.
 * 
 */

public class BitCollector implements Iterable<Boolean>
{
    
	private int targetBitsCount = 0;// bits, coded in this inst  
	private boolean isClosed = false;
	
    private ByteArrayOutputStream  myOutput;
    private int           myBuffer;
    private int           myBitsToGo;
    
    private static final int bmask[] = {
        0x00, 0x01, 0x03, 0x07, 0x0f, 0x1f, 0x3f, 0x7f, 0xff,
        0x1ff,0x3ff,0x7ff,0xfff,0x1fff,0x3fff,0x7fff,0xffff,
        0x1ffff,0x3ffff,0x7ffff,0xfffff,0x1fffff,0x3fffff,
        0x7fffff,0xffffff,0x1ffffff,0x3ffffff,0x7ffffff,
        0xfffffff,0x1fffffff,0x3fffffff,0x7fffffff,0xffffffff
    };

    private static final int BITS_PER_BYTE = 8;

    
    /**
     * Creates a new BitCollector.
     */
    public BitCollector(){
        initialize();
    }
    
    private void initialize(){
        myBuffer = 0;
        myBitsToGo = BITS_PER_BYTE;
        myOutput = new ByteArrayOutputStream();

    }

    
    /** 
     * Creates and returns a deep copy of this object.
     */
    @Override
    public BitCollector clone() {
		BitCollector out = new BitCollector();
		out.targetBitsCount = this.targetBitsCount;
		out.isClosed = this.isClosed;
		out.myBuffer = this.myBuffer;
		out.myBitsToGo = this.myBitsToGo;
		
		out.myOutput = new ByteArrayOutputStream(this.myOutput.size());
		try {
			this.myOutput.writeTo(out.myOutput);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("BitCollector clone error");
		}
		
		return out;
    }

    /**
     * Flushes bits not yet written, called on close().
     * @throws RuntimeException if there's a problem writing bits
     */
    private void flush()
    {
        if (myBitsToGo != BITS_PER_BYTE) {
        	myOutput.write( (myBuffer << myBitsToGo) );
            myBuffer = 0;
            myBitsToGo = BITS_PER_BYTE;
        }
        
        isClosed = true;
        try{
            myOutput.flush();    
        }
        catch (java.io.IOException ioe){
            throw new RuntimeException("error on flush " + ioe);
        }
    }


    /**
     * Append specified number of bits from value to a sequence.
     * @param howManyBits is number of bits to write (1-32)
     * @param value is source of bits, rightmost bits are written
     * @throws RuntimeException if there's an problem writing bits
     */
    
    public void append(int howManyBits, int value)
    {
    	if(isClosed){
    		throw new RuntimeException("Collector already closed."); 
    	}
    	
    	targetBitsCount += howManyBits;
    	
        value &= bmask[howManyBits];  // only right most bits valid

        while (howManyBits >= myBitsToGo){
            myBuffer = (myBuffer << myBitsToGo) |
                       (value >> (howManyBits - myBitsToGo));
          	myOutput.write(myBuffer);    

            value &= bmask[howManyBits - myBitsToGo];
            howManyBits -= myBitsToGo;
            myBitsToGo = BITS_PER_BYTE;
            myBuffer = 0;
        }
        
        if (howManyBits > 0) {
            myBuffer = (myBuffer << howManyBits) | value;
            myBitsToGo -= howManyBits;
        }
    }

    
    /**
     * Append one bit.
     * @param b true - 1, false - 0
     */
    public void append(boolean b){
    	int i = b ? 1 : 0;
    	this.append(1, i);
    }
    
	public int getBitCount() {
		return targetBitsCount;
	}
    
	
	
    /**
     * Close instance.
     * Read operations, such as toByteArray(), toBynaryString(), iterator() are faster on closed instance.
     */
	public void close(){
		flush();
	}
	

	/**
	 * Return right aligned array - 
	 * first byte filled to full byte with leading zeros
	 */
	public byte[] toByteArray(){
    	if(isClosed){
    		return finalizeToRightAlignedByteArr();
    	}else{
    		BitCollector clon = this.clone();
    		clon.close();
    		return clon.toByteArray();
    	}
    }

	/**
	 * 
	 * @param align, RIGHT - first byte filled to full byte with leading zeros,
	 * LEFT - last byte filled to full byte with trailing zeros
	 */
	public byte[] toByteArray(ALIGN align){
    	if(align == ALIGN.RIGHT){
    		return this.toByteArray();
    	}else{
        	if(isClosed){
        		return finalizeToLeftAlignedByteArr();
        	}else{
        		BitCollector clon = this.clone();
        		clon.close();
        		return clon.finalizeToLeftAlignedByteArr();
        	}
    	}
    	
    }

	/**
	 * Return continuous binary string.
	 */
	public String toBynaryString(){
    	BitCollector bc;
    	if(isClosed){
    		bc = this;
    	}else{
    		bc = this.clone();
    		bc.close();
    	}

    	byte[] bArr = bc.finalizeToRightAlignedByteArr();
    	BitSequence bitSequence = new BitSequence(bArr, targetBitsCount, ALIGN.RIGHT);
    	return bitSequence.toBynaryString();
    }

    
	/**
	 * Continuous or grouped binary string.
	 * Grouping can start left or right. 
	 */
    public String toBynaryString(ALIGN align, GROUP groupType){
    	BitCollector bc;
    	if(isClosed){
    		bc = this;
    	}else{
    		bc = this.clone();
    		bc.close();
    	}

    	byte[] bArr = bc.finalizeToRightAlignedByteArr();
    	BitSequence bitSequence = new BitSequence(bArr, targetBitsCount, ALIGN.RIGHT);
    	return bitSequence.toBynaryString(align, groupType);
    }

	
	// first byte filled to full byte with leading zeros
    private byte[] finalizeToRightAlignedByteArr(){
    	flush();
    	byte[] bArr = myOutput.toByteArray();
    	// bArr is left aligned - trailing 000... added
    	// make it right aligned with leading 00..
		int relevantBitsCountOfLastByte = targetBitsCount % 8;
    	if(relevantBitsCountOfLastByte>0){
    		int countOfZeroBits = 8-relevantBitsCountOfLastByte;
        	BigInteger bi = new BigInteger(1, bArr);
        	bi = bi.shiftRight(countOfZeroBits);
        	bArr = bi.toByteArray();
    	}
    	return bArr;
    }

	// last byte filled to full byte with trailing zeros
    private byte[] finalizeToLeftAlignedByteArr(){
    	flush();
    	byte[] bArr = myOutput.toByteArray();
    	return bArr;
    }



	@Override
	public String toString() {
		return "BitCollector [targetBitsCount=" + targetBitsCount + ", isClosed=" + isClosed + "]";
	}

	@Override
	public Iterator<Boolean> iterator() {
		BitCollector snap;
		if(this.isClosed){
			snap = this;
		}else{
			snap = this.clone();
			snap.close();
		}

		BitIterator bitIterator = new BitIterator(snap.toByteArray(), snap.getBitCount());
		return bitIterator;
	}
    
    public BitSequence toBitSequence(){
		BitSequence seq = new BitSequence(this.toByteArray(), this.targetBitsCount, ALIGN.RIGHT);
		return seq;
    	
    }
    
}
