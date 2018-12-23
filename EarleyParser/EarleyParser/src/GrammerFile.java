/*
 * Java Implementation for grammer evaluation of Earley Parser.
 */

public class GrammerFile {
	Rule rule;
	int dotPos;
	int colNum;
	double weight; //
	GrammerFile gr1;
	GrammerFile gr2;
	int identifier; //
	
	/*default constructor */
	GrammerFile(Rule rule, int dotPos, int colNum) {
		this.rule = rule;
		this.dotPos = dotPos;
		this.colNum = colNum;
		this.weight = this.rule.weight; //  weight of a rule is equal to its own weight.
		 
		this.gr1 = null; 
		this.gr2 = null;
		this.identifier = 0;//initializing identifier to value 0
	}

	/*constructor to specify copy function */
	GrammerFile(GrammerFile other) {
		this.rule = other.rule;
		this.dotPos = other.dotPos;
		this.colNum = other.colNum;
		this.weight = other.weight;
		this.gr1 = null; 
		this.gr2 = null; 
		this.identifier = other.identifier;

	}

	//It verifies whether the end of rule is dotPos or not
	boolean isFinished() // 
	{
		if (dotPos == rule.parts.size() - 1)
			return true;
		else
			return false;
	}

	//After dotPos, the elements present at the right side of the rule is returned
	String nextToken() {
		if (this.isFinished())
			return null;
		else
			return this.rule.parts.get(this.dotPos + 1);
	}

	//It overrides the functionality of equal
	@Override
	public boolean equals(Object otherRule) {
		if (!(otherRule instanceof GrammerFile))
			return false;

		GrammerFile other = (GrammerFile) otherRule;

		if (this.colNum != other.colNum)
			return false;

		if (this.dotPos != other.dotPos)
			return false;

		if (!this.rule.equals(other.rule))
			return false;

		return true;
	}

	//It overrides the functionality of hashcode
	@Override
	public int hashCode() {
		int result = 17;
		final int prime = 31;

		result = result * prime + this.colNum;
		result = result * prime + this.dotPos;
		result = result * prime + this.rule.hashCode();
		return result;
	}

	//It overrides the functionality of toString
	@Override
	public String toString() {
		String s = this.colNum + " " + this.rule.getHead() + " --> ";
		for (int i = 1; i < this.rule.parts.size(); i++) {
			if (this.dotPos == i - 1)
				s = s + ". ";

			s = s + this.rule.parts.get(i) + " ";
		}
		if (this.dotPos == this.rule.parts.size() - 1)
			s = s + ".";

		s = s + " (" + this.weight + ") "; // log value of probability is getting added

		return s;
	}


}
