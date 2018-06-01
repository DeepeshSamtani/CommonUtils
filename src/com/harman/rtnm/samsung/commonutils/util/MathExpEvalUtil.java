package com.harman.rtnm.samsung.commonutils.util;

import java.util.Stack;

import org.apache.log4j.Logger;

import com.harman.rtnm.samsung.commonutils.model.Row;
import com.harman.rtnm.samsung.commonutils.util.StringUtils;

/**
 * An expression calculator to be used for evaluating the 
 * collection formulas. The calculation is supported for
 * infix mathematical expression. The expression can contain 
 * alphanumeric string representing the properties or plain 
 * numeric values.
 * </br> The calculator take into account the presence of
 * precedence of different operators expected in the expression
 * to be evaluated as well as the open and closed braces.</br></br>
 *
 */
public final class MathExpEvalUtil {

	private static final Logger LOGGER = Logger.getLogger(MathExpEvalUtil.class);
	
	/**
	 * Evaluating the expression provided in the form
	 * of {@link String}. The expression is converted into
	 * a char array and each charater is further evaluated 
	 * to be an operator or an operand. In case of operand being
	 * a property name, the numeric value is fetched from the
	 * {@link Row} passed an argument.
	 * @param exp expression to be evaluated.
	 * @param row the {@link Row} object containing the value of different properties in the expression string.
	 * 
	 * @return
	 */
	
	//100 * (1 - (IPWORKSDNSSERVQUERYFAILURE /IPWORKSDNSSERVQUERYTOTAL)) example of an expression
	
	private static double evaluate(String exp, final ValueProvider provider)
	{
		StringBuilder sbuf = new StringBuilder();
		char[] expression = exp.toCharArray();

		// Stack for numeric values
		Stack<Double> valuesStack = new Stack<Double>();

		// Stack for Operators
		Stack<Character> operatorStack = new Stack<Character>();

		for (int i = 0; i < expression.length; i++)
		{
			// skip space charater
			if (expression[i] == ' ')
				continue;


			// Current token is a property, push it to stack for numbers
			if(!isOperator(expression[i]) && expression[i] != ')' && expression[i] != '(') 
			{
				sbuf.setLength(0);
				
				/*
				 * building the property string, we need to take care of it
				 * being a numeric value in the expression. so if we encounter a
				 * numeric value, insert it into the value stack directly.
				 */				
				while (i < expression.length && expression[i] != ' ' && !isOperator(expression[i])
						&& expression[i] != ')' && expression[i] != ')') {
					sbuf.append(expression[i++]);
				}

				//numeric value check
				if(StringUtils.isNumeric(StringUtils.stringValueOf(sbuf))) {
					valuesStack.push(Double.parseDouble(StringUtils.stringValueOf(sbuf)));
				} 
				//fetch the value from the row object against the property
				else {
					valuesStack.push(Double.parseDouble(provider.getValue(StringUtils.stringValueOf(sbuf))));
				}

				if(i < expression.length && expression[i] != ' ') --i;
			}

			// pushing the open bracket on to the operator stack
			else if (expression[i] == '(')
				operatorStack.push(expression[i]);

			// Closing bracket found, solving sub-expression inside the bracket
			else if (expression[i] == ')')
			{
				while (operatorStack.peek() != '(') {
					valuesStack.push(applyOp(operatorStack.pop(), valuesStack.pop(), valuesStack.pop()));
				}
				operatorStack.pop();
			}

			/* operator encountered, evaluate values and store the
			 * result back into valuesStack 
			 */
			else if (isOperator(expression[i]))
			{
				/* While top of 'ops' has same or greater precedence to current
				 * token, which is an operator. Apply operator on top of 'ops'
				 * to top two elements in values stack
				 */				
				while (!operatorStack.empty() && hasPrecedence(expression[i], operatorStack.peek()))
					valuesStack.push(applyOp(operatorStack.pop(), valuesStack.pop(), valuesStack.pop()));

				// Push current token to 'ops'.
				operatorStack.push(expression[i]);
			}


		}

		/*
		 * End of string expression, evaluate the rest of operators 
		 * left in operator stack, if any
		 */
		while (!operatorStack.empty())
			valuesStack.push(applyOp(operatorStack.pop(), valuesStack.pop(), valuesStack.pop()));


		return valuesStack.pop();
	}

	/*
	 * return true if the char represents an operator, false otherwise
	 */
	static boolean  isOperator(char token) {
		return (token == '+' || token == '-' ||
				token == '*' || token == '/');
	}

	/*
	 * Returns true if 'op2' has higher or same precedence as 'op1',
	 * otherwise returns false.
	 */
	static boolean hasPrecedence(char operator1, char operator2)
	{
		if (operator2 == '(' || operator2 == ')') {
			return false;
		}

		if ((operator1 == '*' || operator1 == '/') && (operator2 == '+' || operator2 == '-')) {
			return false;
		} else {
			return true;
		}
	}

	/*
	 * Utility mehtod evaluate the operation to be applied 
	 * over the provided operands  
	 */
	static double applyOp(char operator, double operand2, double operand1) {
		switch (operator)
		{
		case '+':
			return operand1 + operand2;
		case '-':
			return operand1 - operand2;
		case '*':
			return operand1 * operand2;
		case '/':
			if (operand2 == 0)
				throw new
				UnsupportedOperationException("Cannot divide by zero");
			return operand1 / operand2;
		}
		return 0;
	}
	
	public static String evaluateExpression(String exp, final ValueProvider provider) {
		try {
			return StringUtils.stringValueOf(evaluate(exp, provider));
		} catch(UnsupportedOperationException ex) {
			LOGGER.debug("Division by ZERO operation encountered, returning 0 as RESULT");
			return "0";
		} catch(Exception e) {
			LOGGER.debug("Exception occured while evaulating exp, return empty string. ");
			return StringUtils.EMPTY_BLANK_STRING;
		}
	}
/*	
	public static void main(String[] args) {
		System.out.println(" result : " + evaluate("30 + 20", null));
	}

*/
	/**
	 * A utility interface to be implemented
	 * for providing the value corresponding 
	 * to the properties appearing in the mathematical
	 * expression to be solved.
	 * 
	 *
	 */
	public interface ValueProvider {
		public String getValue(String key);	
	}
}
