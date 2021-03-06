package lv.edi.EDI_3DAtA.bloodvesselsegm;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

/**
 * 
 * @author Ricards Cacurs
 *
 * Class consisting of operations for filtering image in DenseMatrix64F format
 */
public class FilteringOperations {
	
	/**
	 * Functions performs 2D convolution on image given in DenseMatrix64F format
	 * @param inputImage image on which to perform convolution
	 * @param kernel filter kernel. Filter is considered to be centered in floor((kernelHeight-1)/2)
	 * 																	   floor((kernelWidth-1)/2) 
	 * @return DenseMatrix64F type image same size as input image
	 */
	public static DenseMatrix64F convolve2D(DenseMatrix64F inputImage, DenseMatrix64F kernel){
		
		DenseMatrix64F output = new DenseMatrix64F(inputImage.numRows, inputImage.numCols);
		int kernelCenterR = (int)Math.floor((kernel.numRows-1)/2);
		int kernelCenterC = (int)Math.floor((kernel.numCols-1)/2);
		double sample;
		double samplesum;
		double kernelval;
		for(int i=0; i<inputImage.numRows; i++){
			for(int j=0; j<inputImage.numCols; j++){
				
				samplesum=0;
				for(int ki=0; ki<kernel.numRows; ki++){
					for(int kj=0; kj<kernel.numCols; kj++){
						int pixelRowIndex = i+(ki-kernelCenterR);
						int pixelColIndex = j+(kj-kernelCenterC);
						if((pixelRowIndex<0)|| //checking if not out of bounds
						   (pixelRowIndex>=inputImage.numRows)||
						   (pixelColIndex<0)||
						   (pixelColIndex>=inputImage.numCols)){
							sample=0;
						} else{
							sample = inputImage.unsafe_get(pixelRowIndex, pixelColIndex);
						}
						kernelval=kernel.get(kernel.numRows-1-ki, kernel.numCols-1-kj);
						if(kernelval!=0){
							samplesum+=sample*kernelval; //kernel is flipped
						}
					}
				}
				output.unsafe_set(i, j, samplesum);
			}
		}
		return output;
	}
	/**
	 * Function performing 1D convolution of two signals. Resulting convolution is same size as input.
	 * @param input signal packed in DenseMatrix64F type (doesn't matter if column or row)
	 * @param filter packed in DenseMatrix64F type (doesn't matter if column or row)
	 * @return DenseMatrix64F convolution result. Format corresponds to format of the input.
	 */
	public static DenseMatrix64F convolve1D(DenseMatrix64F input, DenseMatrix64F filter){
		int filterCenter = (int)Math.floor((filter.getNumElements()-1)/2);
		int filterSize = filter.getNumElements();
		double[] inputPadded= new double[input.data.length+2*(filterSize/2)];
		System.arraycopy(input.data, 0, inputPadded, filter.getNumElements()/2, input.data.length);
		DenseMatrix64F output;
		if(input.numCols==input.getNumElements()){ // form output based on input format
			output = new DenseMatrix64F(1,input.getNumElements());
		} else{
			if(input.numRows==input.getNumElements()){
				output = new DenseMatrix64F(input.getNumElements(),1);
			} else{
				return null;
			}
		}
		double sampleSum=0;
		double filterVal=0;
		int iStart = filterSize/2;
		int iEnd = iStart+input.getNumElements();
		for(int i=iStart; i<iEnd; i++){
			sampleSum=0;
			
			for(int j=0; j<filter.getNumElements(); j++){
				int sampleIndex=i+(j-filterCenter);
				filterVal=filter.get(filter.getNumElements()-1-j);
				sampleSum+=inputPadded[sampleIndex]*filterVal;
			}
			output.set(i-filterSize/2, sampleSum);
		}
		return output;
	}
	
	/**
	 * Function that performs Gaussian blur on image given in type DenseMatrix64F
	 * @param input image on which to perform gaussian blur
	 * @param sigmaSQ standard deviation for gaussian filter (sigma^2)
	 * @param kernelSize kernel size
	 * @return returns Filtered image the same size as input image
	 */
	public static DenseMatrix64F gaussianBlur(DenseMatrix64F input, double sigmaSQ, int kernelSize){
		DenseMatrix64F kernel = new DenseMatrix64F(1,kernelSize);
		int kernelCenter = (int)(kernelSize-1)/2;
		for(int i=0; i<kernelSize; i++){
			kernel.set(i, Math.exp(-Math.pow(i-kernelCenter,2)/(2*sigmaSQ))/(Math.sqrt(2*Math.PI*sigmaSQ)));
		}
		CommonOps.divide(kernel, CommonOps.elementSum(kernel));
		// filter by rows
		DenseMatrix64F rowFiltered = new DenseMatrix64F(input.numRows, input.numCols);
		for(int i=0; i<input.numRows; i++){
			CommonOps.insert(convolve1D(CommonOps.extract(input, i, i+1, 0, input.numCols), kernel), rowFiltered, i, 0);
		}
		// filter by columns
		DenseMatrix64F colFiltered = new DenseMatrix64F(input.numRows, input.numCols);
		for(int i=0; i<input.numCols; i++){
			CommonOps.insert(convolve1D(CommonOps.extract(rowFiltered, 0, input.numRows, i, i+1), kernel), colFiltered, 0, i);
		}
		return colFiltered;
	}

}
