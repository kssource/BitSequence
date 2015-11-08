package de.jk.bit.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class BitIterator implements Iterator<Boolean> {

	private int totalIndex = 0; 
	private int totalBitCount = 0;
	
	private byte[] bArr; 
	private int currentByte;
	private int currentByteIndex;
	private int currentBitIndex;

    private static final int bmask[] = {
    		0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01 
        };

	
	// byteArr must be rightAligned
	public BitIterator(byte[] byteArr, int bitCount) {
		totalBitCount = bitCount;
		bArr = byteArr;
		
		init();
	}

	private void init() {
		currentBitIndex = -1;
		currentByteIndex = 0;
		currentByte = bArr[currentByteIndex];

		int relevantBitsCountOfLeadingByte = totalBitCount % 8;
		if(relevantBitsCountOfLeadingByte != 0){
			currentBitIndex = 8 - relevantBitsCountOfLeadingByte - 1;
		}

		
	}

	@Override
	public boolean hasNext() {
		return totalIndex < totalBitCount;
	}

	@Override
	public Boolean next() {
		if(hasNext() == false){
			throw new NoSuchElementException();
		}
		
		++currentBitIndex;
		++totalIndex;
		
		if(currentBitIndex>7){
			currentBitIndex = 0;
			
			++currentByteIndex;
			currentByte = bArr[currentByteIndex];
		}
		
		int val = currentByte & bmask[currentBitIndex];
		boolean ret = val>0 ? true : false;
		
		return ret;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
