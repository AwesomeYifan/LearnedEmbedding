package mtree.examples;

import mtree.DistanceFunctions.EuclideanCoordinate;

//Each point is an instance of Data
class Data implements EuclideanCoordinate, Comparable<Data> {
	
	private final double[] values;
	private final int hashCode;
	private final int ID;
	
	Data(double[] values, int ID) {
		this.values = values;

		this.ID = ID;
		
		int hashCode = 1;
		for(double value : values) {
			hashCode = (int)(31*hashCode + value);
		}
		this.hashCode = hashCode;
	}
	
	@Override
	public int dimensions() {
		return values.length;
	}

	public int getID() {return ID;}

	@Override
	public double get(int index) {
		return values[index];
	}
	
	@Override
	public int hashCode() {
		return hashCode;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Data) {
			Data that = (Data) obj;
			if(this.dimensions() != that.dimensions()) {
				return false;
			}
			for(int i = 0; i < this.dimensions(); i++) {
				if(this.values[i] != that.values[i]) {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}

	//why need a compareTo function?
	@Override
	public int compareTo(Data that) {
		int dimensions = Math.min(this.dimensions(), that.dimensions());
		for(int i = 0; i < dimensions; i++) {
			double v1 = this.values[i];
			double v2 = that.values[i];
			if(v1 > v2) {
				return +1;
			}
			if(v1 < v2) {
				return -1;
			}
		}
		
		if(this.dimensions() > dimensions) {
			return +1;
		}
		
		if(that.dimensions() > dimensions) {
			return -1;
		}
		
		return 0;
	}
	
}