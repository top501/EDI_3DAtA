if [ ! -d "./bin" ]; then
	mkdir bin
fi

nvcc --include-path\
 "../bloodVesselSegmentation/include","$OPENCV_INCLUDE"\
 --library-path "../bloodVesselSegmentation/bin","$OPENCV_LIB"\
 --library opencv_core,opencv_imgcodecs,opencv_highgui\
 --output-file ./bin/mainTest ./src/main.cpp
