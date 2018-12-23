
/*
 * Java code for Implementation of Earley Parser.
 * We have implemented the basic functionality of predict,scan and attach.
 * Grammar rule is implemented in GrammerFile.java.
  */
import java.util.ArrayList;
import java.util.HashMap;
import java.io.BufferedWriter;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;


/* This class implements an Earley parser for CFG's */


public class Earley {
	
	static ArrayList<ArrayList<GrammerFile>> ac;  // Here we consider every column as arraylist
	static ArrayList<HashMap<GrammerFile, Integer>> hI1;   //column indices are stored here
	static HashMap<String, ArrayList<ArrayList<Integer>>> hI2;  // indices for attach rules are stored here
	static HashMap<String, ArrayList<ArrayList<Integer>>> hI3;
	static HashMap<String, ArrayList<Rule>> rules; //grammar files rules are stored here
	static HashMap<String, ArrayList<Rule>> pt;
	static HashMap<String, ArrayList<String>> lP;
	static HashMap<String, ArrayList<String>> lP1;
	static HashMap<String, ArrayList<String>> lP2;
	static HashMap<String, ArrayList<String>> lA;
	static int nullVal = 0;//nullVal is initialized to zero
	static String sentence = null;//sentence variable is initialized  to zero
	static String valSentence = null;
	static HashMap<String, ArrayList<Rule>> pl;
	static int nullSentence = 0;
	


	//To check if the given string is terminal or non terminal
	public static boolean isNonTerminal(HashMap<String, ArrayList<Rule>> rules, String token) {
		if (rules.containsKey(token))
			return true;
		else
			return false;
	}
	
	// This function is used to predict rules for corresponding column and verify if the rule is empty

	public static void predict(String nextToken, int colIndex, HashMap<String, ArrayList<Rule>> rules) {
		ArrayList<String> selectNextToken = lA.get(nextToken);
		ArrayList<Rule> ruleVal = new ArrayList<Rule>();
		if (selectNextToken != null) 
		{
			for (String s : selectNextToken)
			{
				for (Rule r : pt.get(nextToken + " " + s)) {
					ruleVal.add(r);
			}
		}
	}

		HashMap<GrammerFile, Integer> hashIndex = hI1.get(colIndex);

		if (ruleVal.size() > 0) {
			Rule r1 = ruleVal.get(0);
			GrammerFile grmmr1 = new GrammerFile(r1, 0, colIndex);
         // to check if the grammer has atleast one rule associated with it and if not all the rules are added
			if (!hashIndex.containsKey(grmmr1)) 
			{
				for (Rule r : ruleVal) // for each such rule
				{
					GrammerFile gr = new GrammerFile(r, 0, colIndex); //to add DOT in position 0 and number present column.
					addRule(colIndex, gr); //to add rule to present column
				}
			}
		}
	}

	//Once the rule is predicted scan is performed to given set of rules
	public static boolean scan(GrammerFile currRule, String currWord, int colIndex) {
		HashMap<GrammerFile, Integer> hashIndex = hI1.get(colIndex + 1);
		String token = currRule.nextToken(); 
		if (token.equals(currWord)) //to verify if the current and the next word are similar or different in the new ruleh
		{ 
			GrammerFile newRule = new GrammerFile(currRule); // to create a copy of the current rule
			newRule.dotPos += 1; //to increment the position of DOT by 1

			newRule.gr1 = currRule; 
			newRule.gr2 = null; 

			if (!hashIndex.containsKey(newRule)) //if a rule is not present is will be added to newRule 
			{
				addRule(colIndex + 1, newRule); 
				return true;
			} 
			else
			{
				GrammerFile oldRule = ac.get(colIndex + 1).get(hashIndex.get(newRule));

				if (newRule.weight < oldRule.weight) // if the new rule is better than the older one,it is replaced
														
				{
					ac.get(colIndex + 1).set(hashIndex.get(oldRule), null);
					addRule(colIndex + 1, newRule); // adding new rule
					return true;
				} else {
					/*Its always preferable that the weight of the new rule is less than the old rule .
					 
					 */
					assert (false);
				}

				return false;
			}
		}
		else // condition when scan failes
			{
			return false;
		}
	}

	//to add the rule to the parse tree after scaning is done
	public static void attach(GrammerFile gr, int IndexVal) {
		HashMap<GrammerFile, Integer> hashIndex = hI1.get(IndexVal);
		String token = gr.rule.getHead(); // 
		
		double grWeight = gr.weight; // the probability of the given token is got 
		ArrayList<GrammerFile> prevCol = ac.get(gr.colNum);
		ArrayList<GrammerFile> currCol = ac.get(IndexVal);
		
		int j =0;
		
		if (hI2.containsKey(token)) {

			ArrayList<Integer> iteration = hI2.get(token).get(gr.colNum);
			
			for (int i : iteration)
			{
				GrammerFile tmpColVal = prevCol.get(i);

				if (tmpColVal == null) 
					continue;

				GrammerFile newGrammerRule = new GrammerFile(tmpColVal); 
				
				newGrammerRule.dotPos += 1;
				newGrammerRule.weight += grWeight; 

				newGrammerRule.gr1 = tmpColVal;
				newGrammerRule.gr2 = gr;
				
				if (!hashIndex.containsKey(newGrammerRule)) 
				{
					addRule(IndexVal, newGrammerRule);
				}

				else // condition if the rule exists
				{
					GrammerFile grOld = currCol.get(hashIndex.get(newGrammerRule));
					if(newGrammerRule.weight < grOld.weight) 
					{
						nullVal++;
						currCol.set(hashIndex.get(grOld), null);
						addRule(IndexVal, newGrammerRule);
					}

				}

			}
		}
	}


	
	public static void printTraceTree(ArrayList<Rule> finalRules, GrammerFile gr) {
		if (gr != null) {
			if (gr.isFinished()) {
				finalRules.add(gr.rule);
			}

			printTraceTree(finalRules, gr.gr1);
			printTraceTree(finalRules, gr.gr2);
		}

	}

	static int j = 0;

	//For printing the parse tree in the given format
	
	public static void printParseGrammer(ArrayList<Rule> finalRules, int index) {

		j++;
		Rule curr = finalRules.get(index);

		if (index == finalRules.size() - 1) {
			System.out.print("(" + curr.getHead());
			for (int i = 1; i < curr.parts.size(); i++) {
				System.out.print(" " + curr.parts.get(i));
			}
			System.out.print(" )");
			return;
		}
		System.out.print("(" + curr.getHead() + " ");

		for (int i = 1; i < curr.parts.size(); i++) {
			if (!isNonTerminal(rules, curr.parts.get(i))) {
				System.out.print(curr.parts.get(i) + " ");
			}
			
			else if (curr.parts.get(i).equals(finalRules.get(j).getHead())) {
		
				printParseGrammer(finalRules, j);
			}

		}
		System.out.print(")");
	}

	//this function is to fill the left side of the tree
	public static void fillLeftValue(String token) {
		if (lP.get(token) == null)
			return;

		for (String s : lP.get(token)) {
			if (!lA.containsKey(s)) {
				ArrayList<String> ar = new ArrayList<String>();
				ar.add(token);
				lA.put(s, ar);
				fillLeftValue(s);
			} else {
				if (!lA.get(s).contains(token)) {
					lA.get(s).add(token);
				}
			}
		}
	}

	public static void main(String args[]) throws Exception {
		
		File file = new File("C:/file/prob-simple.gr");
		
		File sentencefile = new File("C:/file/samplesentence.txt");
		
		final String gramFile = String.valueOf(file);
		final String senFile = String.valueOf(sentencefile);
		rules = getRules(gramFile);
		ArrayList<String> snLine = getSentences(senFile);
		lA = new HashMap<String, ArrayList<String>>();

		for (String s : snLine) {
			ArrayList<Rule> finalRules = new ArrayList<Rule>(); 

			String[] wordInSent = s.split(" "); //to split the sentence into indivisual words
			int len = wordInSent.length; 

			ac = new ArrayList<ArrayList<GrammerFile>>(); 
			int k =0;
			while (k < len + 1) {
				ArrayList<GrammerFile> tmp = new ArrayList<GrammerFile>();
				ac.add(tmp);
				k++;
			}

			hI1 = new ArrayList<HashMap<GrammerFile, Integer>>(); 
			int m=0;
			while (m < len + 1) {
				HashMap<GrammerFile, Integer> tmp = new HashMap<GrammerFile, Integer>();
				hI1.add(tmp);
				m++;
			}

			hI2 = new HashMap<String, ArrayList<ArrayList<Integer>>>();

			for (Rule rootRule : rules.get("ROOT")) //from the root rules are added
			{
				GrammerFile rootDotRule = new GrammerFile(rootRule, 0, 0);
		    	addRule(0, rootDotRule); //from position 0 value is added to the respective rule
 
			}
			
			int i=0;
			
			while(i <= wordInSent.length) 
			{

				if (i < wordInSent.length) 
				{
					lA.clear();
					fillLeftValue(wordInSent[i]);
					
				}

				ArrayList<GrammerFile> currCol = ac.get(i);
				
				for (int j = 0; j < currCol.size(); j++) {

					GrammerFile currRule = currCol.get(j);
					if (currRule == null) //when the current rule is equal,skip
					{
						continue;
					}
					if (!currRule.isFinished()) // checks if the current rule is finished or not
					{
						String nextToken = currRule.nextToken();

						if (isNonTerminal(rules, nextToken)) 
						{
							predict(nextToken, i, rules);
						} 
						else 
						{
							if (i != wordInSent.length) // since left input is yet to be parsed last column won't be scanned
								scan(currRule, wordInSent[i], i);

						}
					}
					else 
					{
						attach(currRule, i);
					}
				}
				i++;
			}

			GrammerFile finalRuleValue = null;
			int pvalue = 0;
			for (Rule rootRule : rules.get("ROOT")) {
				finalRuleValue = new GrammerFile(rootRule, rootRule.parts.size() - 1, 0); // 
				HashMap<GrammerFile, Integer> hashIndex = hI1.get(wordInSent.length);

				while (hashIndex.containsKey(finalRuleValue)) {
					printTraceTree(finalRules, ac.get(wordInSent.length).get(hashIndex.get(finalRuleValue)));
					j = 0;
					printParseGrammer(finalRules, 0);

					System.out.println();

					System.out.println(ac.get(wordInSent.length).get(hashIndex.get(finalRuleValue)).weight);
					pvalue = 1;
					break;
				}

			}
			if (pvalue == 0)
				System.out.println("Sentence could not get parsed by the given grammer");

		}
	}
	
	
	public static ArrayList<String> getSentences(String fileName) throws Exception 
	{
		ArrayList<String> sentences = new ArrayList<String>();
		File inputFile = new File(fileName);
		BufferedReader brReader = new BufferedReader(new FileReader(inputFile));
		
		for (String currRule = brReader.readLine(); currRule != null; currRule = brReader.readLine()) 
		{
			if (currRule.length() == 0) /*if there is a empty sentences present in the grammer file it skips*/
				continue;
			sentences.add(currRule); /*the rule that is in grammer file is stored in file currRule*/
		}
		brReader.close();
		return sentences;

	}


	public static HashMap<String, ArrayList<Rule>> getRules(String fileName) throws Exception {
		rules = new HashMap<String, ArrayList<Rule>>();
		pt = new HashMap<String, ArrayList<Rule>>();
		lP = new HashMap<String, ArrayList<String>>();

		File inputFile = new File(fileName);
		BufferedReader brReader = new BufferedReader(new FileReader(inputFile));
	

		for (String currRule = brReader.readLine(); currRule != null; currRule = brReader.readLine())/*to read all the sentences present in the file grammer*/ 
		{
			if (currRule.length() == 0) /*if there is a empty sentences present in the grammer file it skips*/
				continue;
			Rule r = new Rule(currRule);
			rules = getrulefile(r);			
		}
		brReader.close();
		return rules;

	}

	public static HashMap<String, ArrayList<Rule>> getrulefile(Rule r) throws Exception {
		
		if (!pt.containsKey(r.parts.get(0) + " " + r.parts.get(1))) //
		{
			ArrayList<Rule> ruleArr = new ArrayList<Rule>();
			ruleArr.add(r);
		
			pt.put(r.parts.get(0) + " " + r.parts.get(1), ruleArr);
			
			if (!lP.containsKey(r.parts.get(1))) {
				ArrayList<String> ar = new ArrayList<String>();
				ar.add(r.parts.get(0));
				lP.put(r.parts.get(1), ar);
			} else {
				lP.get(r.parts.get(1)).add(r.getHead());
			}

		}

		else
		{
			pt.get(r.parts.get(0) + " " + r.parts.get(1)).add(r);
		}

		if (rules.containsKey(r.getHead())) {
			rules.get(r.getHead()).add(r);
		} else {
			ArrayList<Rule> ar = new ArrayList<Rule>();
			ar.add(r);
			rules.put(r.getHead(), ar);
		}
		return rules;
	}

	
	/*
	 * function that updates hashtable withh index of a given table to which the rule is added
	 */
	public static void addRule(int colIndex, GrammerFile gmrRule) {
		ac.get(colIndex).add(gmrRule); //here rule id is added to the column
		hI1.get(colIndex).put(gmrRule, ac.get(colIndex).size() - 1); // here hashtable is updated with rule index

		if (!gmrRule.isFinished()) {
			int ruleIndex = ac.get(colIndex).size() - 1;
			String nextToken = gmrRule.nextToken();

			if (!hI2.containsKey(nextToken)) {
				ArrayList<ArrayList<Integer>> arr = new ArrayList<ArrayList<Integer>>();

				for (int i = 0; i < ac.size(); i++) {
					arr.add(new ArrayList<Integer>());
				}
				arr.get(colIndex).add(ruleIndex);
				hI2.put(gmrRule.nextToken(), arr);
			} else {
				hI2.get(gmrRule.nextToken()).get(colIndex).add(ruleIndex);
			}
		}
	}

	public static void fillRightValue(String token) {
		if (lP.get(token) == null)
			return;

		for (String s : lP.get(token)) {
			if (!lA.containsKey(s)) {
				ArrayList<String> ar = new ArrayList<String>();
				ar.add(token);
				lA.put(s, ar);
				fillLeftValue(s);
			} else {
				if (!lA.get(s).contains(token)) {
					lA.get(s).add(token);
				}
			}
		}
	}

	public static HashMap<String, ArrayList<Rule>> getVal(String fileName) throws Exception {
		rules = new HashMap<String, ArrayList<Rule>>();
		pt = new HashMap<String, ArrayList<Rule>>();
		lP = new HashMap<String, ArrayList<String>>();

		File inputFile = new File(fileName);
		BufferedReader brReader = new BufferedReader(new FileReader(inputFile));
	

		for (String currRule = brReader.readLine(); currRule != null; currRule = brReader.readLine())
		{
			if (currRule.length() == 0) 
				continue;
			Rule r = new Rule(currRule);
			rules = getrulefile(r);			
		}
		brReader.close();
		return rules;

	}
	public static void treedisplay (String token) {
		if (lP.get(token) == null)
			return;

		for (String s : lP.get(token)) {
			if (!lA.containsKey(s)) {
				ArrayList<String> ar = new ArrayList<String>();
				ar.add(token);
				lA.put(s, ar);
				fillLeftValue(s);
			} else {
				if (!lA.get(s).contains(token)) {
					lA.get(s).add(token);
				}
			}
		}
	}
	public static void treeformat(String token) {
		if (lP.get(token) == null)
			return;

		for (String s : lP.get(token)) {
			if (!lA.containsKey(s)) {
				ArrayList<String> ar = new ArrayList<String>();
				ar.add(token);
				lA.put(s, ar);
				fillLeftValue(s);
			} else {
				if (!lA.get(s).contains(token)) {
					lA.get(s).add(token);
				}
			}
		}
	}
	public static void ruledisplay(int colIndex, GrammerFile gmrRule) {
		ac.get(colIndex).add(gmrRule); //here rule id is added to the column
		hI1.get(colIndex).put(gmrRule, ac.get(colIndex).size() - 1); // here hashtable is updated with rule index

		if (!gmrRule.isFinished()) {
			int ruleIndex = ac.get(colIndex).size() - 1;
			String nextToken = gmrRule.nextToken();

			if (!hI2.containsKey(nextToken)) {
				ArrayList<ArrayList<Integer>> arr = new ArrayList<ArrayList<Integer>>();

				for (int i = 0; i < ac.size(); i++) {
					arr.add(new ArrayList<Integer>());
				}
				arr.get(colIndex).add(ruleIndex);
				hI2.put(gmrRule.nextToken(), arr);
			} else {
				hI2.get(gmrRule.nextToken()).get(colIndex).add(ruleIndex);
			}
		}
	}
	public static void tracedtreedisplay(ArrayList<Rule> finalRules, GrammerFile gr) {
		if (gr != null) {
			if (gr.isFinished()) {
				finalRules.add(gr.rule);
			}

			printTraceTree(finalRules, gr.gr1);
			printTraceTree(finalRules, gr.gr2);
		}

	}

}
