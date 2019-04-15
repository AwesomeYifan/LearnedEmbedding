package mtree.examples;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Fixture {
	
	static class Action {
		char cmd;//A for add, R for remove
		Data data;//first point
		Data queryData;//query point (independent from above)
		double radius;//find all points within this distance to the query point
		int limit;//find kNN to the query point

		private Action(char cmd, Data data, Data queryData, double radius, int limit) {
			this.cmd = cmd;
			this.data = data;
			this.queryData = queryData;
			this.radius = radius;
			this.limit = limit;
		}
	}


	private int dimensions;
	List<Action> actions;
	private int ID = 0;

	static String path(String fixtureName) {
		return "M-Tree-master/cpp/tests/fixtures/" + fixtureName + ".txt";
	}

	static Fixture load(String fixtureName) {
		String fixtureFileName = path(fixtureName);
		BufferedReader fixtureFile = null;
		try {
			fixtureFile = new BufferedReader(new FileReader(fixtureFileName));
			
			int dimensions = Integer.parseInt(fixtureFile.readLine());
			
			int count = Integer.parseInt(fixtureFile.readLine());
			
			Fixture fixture = new Fixture(dimensions);
			
			for(int i = 0; i < count; i++) {
				String line = fixtureFile.readLine();
				List<String> fields = new ArrayList<String>(Arrays.asList(line.split("\\s+")));
				
				char cmd = fields.remove(0).charAt(0);
				Data data = fixture.readData(fields);
				Data queryData = fixture.readData(fields);
				double radius = Double.parseDouble(fields.remove(0));
				int limit = Integer.parseInt(fields.remove(0));
			
				fixture.actions.add(new Action(cmd, data, queryData, radius, limit));
				
				assert fields.isEmpty();
			}
			
			return fixture;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				fixtureFile.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	private Fixture(int dimensions) {
		this.dimensions = dimensions;
		this.actions = new ArrayList<Action>();
	}
	

	private Data readData(List<String> fields) {
		double[] values = new double[dimensions];
		for(int d = 0; d < dimensions; d++) {
			values[d] = Double.parseDouble(fields.remove(0));
		}
		ID++;
		return new Data(values, ID);
	}
}
