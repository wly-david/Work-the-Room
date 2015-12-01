package wtr.g2;

import java.io.*;
import java.util.*;

class Groups {

	private static String[][] parse_CSV(String path) throws IOException
	{
		BufferedReader file = new BufferedReader(new FileReader(path));
		List <String[]> list = new ArrayList <String[]> ();
		for (;;) {
			String line = file.readLine();
			if (line == null) break;
			String[] data = line.split(",");
			for (int i = 0 ; i != data.length ; ++i)
				data[i] = data[i].trim();
			list.add(data);
		}
		file.close();
		return list.toArray(new String [0][]);
	}

	private static final Random random = new Random();

	private static boolean bt(int v, int[] vars, int[][] groups,
	                          boolean[][] conflict)
	{
		if (v == vars.length) return true;
		// pick a random group
		int r = random.nextInt(vars.length - v) + v;
		int x = vars[r];
		vars[r] = vars[v];
		vars[v] = x;
		// scan for empty spots
		int i, j, k;
		for (i = 0 ; i != groups.length ; ++i) {
			for (j = 0 ; j != groups[i].length ; ++j)
				if (groups[i][j] < 0) break;
			if (j == groups[i].length) continue;
			// check for conflict from old groups
			for (k = 0 ; k != j ; ++k) {
				int y = groups[i][k];
				if (conflict[x][y]) break;
			}
			if (k != j) continue;
			// backtrack
			groups[i][j] = x;
			if (bt(v + 1, vars, groups, conflict))
				return true;
			groups[i][j] = -1;
		}
		return false;
	}

	public static void main(String[] args)
	{
		try {
			if (args.length < 2 || args.length > 3)
				throw new Exception("Usage: java Groups min_group_size "
				                    + "old_groups_CSV [new_groups_CSV]");
			int group_size = Integer.parseInt(args[0]);
			String[][] old_data = parse_CSV(args[1]);
			String[][] new_data = new String [0][];
			if (args.length > 2) new_data = parse_CSV(args[2]);
			// parse players from old groups
			HashMap <String,Integer> map = new HashMap <String,Integer>();
			for (int i = 0 ; i != old_data.length ; ++i)
				for (int j = 0 ; j != old_data[i].length ; ++j)
					if (!map.containsKey(old_data[i][j]))
						map.put(old_data[i][j], map.size());
			// create array of players
			int size = map.size();
			if (size < group_size)
				throw new Exception("Too few values overall");
			if (size / group_size < new_data.length)
				throw new Exception("Too many groups in new file");
			int[][] groups = new int [size / group_size][];
			for (int i = 0 ; i != groups.length ; ++i) {
				int j = group_size;
				if (i < size % group_size) j++;
				groups[i] = new int [j];
			}
			for (int i = 0 ; i != groups.length ; ++i)
				for (int j = 0 ; j != groups[i].length ; ++j)
					groups[i][j] = -1;
			// set partial values using constraints for new groups
			boolean[] set = new boolean [size];
			int n_set = 0;
			for (int i = 0 ; i != new_data.length ; ++i) {
				if (new_data[i].length > groups[i].length)
					throw new Exception("Too many values in new file");
				for (int j = 0 ; j != new_data[i].length ; ++j) {
					if (map.get(new_data[i][j]) == null)
						throw new Exception("Invalid value in new file");
					int x = map.get(new_data[i][j]);
					if (set[x])
						throw new Exception("Repeated value in new file");
					set[x] = true;
					n_set++;
					groups[i][j] = x;
				}
			}
			// set constraints from old groups
			boolean[][] conflict = new boolean [size][size];
			for (int i = 0 ; i != old_data.length ; ++i)
				for (int j = 0 ; j != old_data[i].length ; ++j) {
					int x = map.get(old_data[i][j]);
					for (int k = 0 ; k != j ; ++k) {
						int y = map.get(old_data[i][k]);
						conflict[x][y] = conflict[y][x] = true;
					}
				}
			// unset variables
			int[] unset = new int [size - n_set];
			for (int i = 0, j = 0 ; i != size ; ++i)
				if (!set[i]) unset[j++] = i;
			// backtrack to solve the problem
			if (bt(0, unset, groups, conflict) == false)
				throw new Exception("No solution exists");
			// inverse value map
			String[] values = new String [size];
			for (String value : map.keySet())
				values[map.get(value)] = value;
			// print result
			for (int i = 0 ; i != groups.length ; ++i) {
				for (int j = 0 ; j != groups[i].length ; ++j) {
					if (j != 0) System.out.print(", ");
					System.out.print(values[groups[i][j]]);
				}
				System.out.println("");
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}
}
