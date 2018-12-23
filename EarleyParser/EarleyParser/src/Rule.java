import java.util.ArrayList;
import java.util.HashMap;

class Rule {
	ArrayList<String> parts;
	String aux;
	double size;
	double weight;

	String getHead() {
		return parts.get(0);
	}
	
	//It splits sentences into word and calculate the probability in log10 value
	Rule(String line) {
		this.parts = new ArrayList<String>(); // init arrayList

		String[] tokens = line.split("\\s+");
		String[] auxs = line.split("\\s+", 2);
		this.aux = auxs[1];
		double probability = Double.parseDouble(tokens[0]);

		this.weight = (Math.log10(probability)); // Calculated probability in log value
																

		for (int i = 1; i < tokens.length; i++) {
			parts.add(tokens[i]);
		}
		this.size = parts.size();
	}

	
	/* Checks if a given token is a terminal or non-terminal */
	boolean isNonTerminal(HashMap<String, ArrayList<Rule>> rules, String token) {
		if (rules.containsKey(token))
			return true;
		else
			return false;
	}
	
	//It override equals functionality
	@Override
	public boolean equals(Object otherRule) {

		if (!(otherRule instanceof Rule))
			return false;

		Rule other = (Rule) otherRule;

		for (int i = 0; i < this.parts.size(); i++) {
			if (!this.parts.get(i).equals(other.parts.get(i)))
				return false;
		}

		return true;

	}

	//It sets rule as key to hashTable
	@Override
	public int hashCode() 
	{
		return this.aux.hashCode();
	}

	//We add Rule in the form of A-->B.C
	
	@Override
	public String toString() 
	{
		String s = this.weight + " " + this.getHead() + " --> ";
		for (int i = 1; i < parts.size(); i++) {
			s = s + parts.get(i) + " ";
		}
		return s;
	}

}
