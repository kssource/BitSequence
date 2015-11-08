package de.jk.bit.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

import de.jk.bit.util.BitSequence.DIRECTION;

public class BitIteratorInt implements Iterator<Integer> {

	private int totalIndex = 0; 
	private int totalBitCount = 0;
	
	private byte[] bArr; 
	private int currentByte;
	private int currentByteIndex;
	private int currentBitIndex;
	private DIRECTION direction;
	private int iterationSummand;

    private static final int bmask[] = {
    		0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01 
        };

	
	// byteArr must be rightAligned
	public BitIteratorInt(byte[] byteArr, int bitCount, DIRECTION direction) {
		totalBitCount = bitCount;
		bArr = byteArr;
		this.direction = direction;
		
		init();
	}

	private void init() {
		int relevantBitsCountOfLeadingByte = totalBitCount % 8;

		if(direction == DIRECTION.LEFT_TO_RIGHT){
			currentBitIndex = -1;
			currentByteIndex = 0;
			currentByte = bArr[currentByteIndex];

			if(relevantBitsCountOfLeadingByte != 0){
				currentBitIndex = 8 - relevantBitsCountOfLeadingByte - 1;
			}
			
			this.iterationSummand = 1;
			totalIndex = 0;
		}else{//right to left
			currentBitIndex = 8;
			currentByteIndex = bArr.length-1;
			currentByte = bArr[currentByteIndex];
			
			this.iterationSummand = -1;
			totalIndex = totalBitCount-1;
		}

	}

	@Override
	public boolean hasNext() {
		if(direction == DIRECTION.LEFT_TO_RIGHT){
			return totalIndex < totalBitCount;
		}else{
			return totalIndex >= 0;
		}
		
	}

	@Override
	public Integer next() {
		if(hasNext() == false){
			throw new NoSuchElementException();
		}
		
		currentBitIndex += iterationSummand;
		totalIndex += iterationSummand;

		if(direction == DIRECTION.LEFT_TO_RIGHT){
			if(currentBitIndex>7){
				currentBitIndex = 0;
				
				++currentByteIndex;
				currentByte = bArr[currentByteIndex];
			}
		}else{
			if(currentBitIndex<0){
				currentBitIndex = 7;
				
				--currentByteIndex;
				currentByte = bArr[currentByteIndex];
			}
		}
		
		
		int val = currentByte & bmask[currentBitIndex];
		int ret = val>0 ? 1 : 0;

		return ret;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
