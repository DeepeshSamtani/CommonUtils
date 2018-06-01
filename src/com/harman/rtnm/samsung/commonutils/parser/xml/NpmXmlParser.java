package com.harman.rtnm.samsung.commonutils.parser.xml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpression;

import org.apache.log4j.Logger;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.harman.rtnm.samsung.commonutils.model.Row;
import com.harman.rtnm.samsung.commonutils.model.RowConfig;
import com.harman.rtnm.samsung.commonutils.util.StringUtils;




/**
 * The class uses the {@link NpmXPath} implementation to parse the XML 
 * using the XPATH or {@link XPathExpression}. It has multiple methods
 * which can read one value at a time or multiple values. All the implemented
 * methods accept a reference to the {@link Row} object and populates the properties'
 * values in it.
 * <br><br><b> The implementation uses the DOM model, not suitable for parsing very large XML files.</b>
 *
 *
 */
public final class NpmXmlParser {

	private static final Logger LOGGER = Logger.getLogger(NpmXmlParser.class);

	private static final String COMPARE_EQUALS = "EQUALS";

	private NpmXPath xpathReader = null;

	private String xmlMessage = StringUtils.EMPTY_BLANK_STRING;

	/**
	 * Constructor to instantiate the {@link NpmXmlParser} with the input and output path
	 * Both the parameters are manadatory, if any of the input path or output path is null
	 * an exception will be raised.
	 * @param filePathXml path to the input xml file to be parsed.
	 * @param outputCsv path to the output csv to be generated.
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public NpmXmlParser(String filePathXml) throws SAXException, IOException, ParserConfigurationException {
		this(filePathXml, true);
	}


	/**
	 * Constructor to instantiate the {@link NpmXmlParser} with the input and output path
	 * Both the parameters are manadatory, if any of the input path or output path is null
	 * an exception will be raised.
	 * @param filePathXml path to the input xml file to be parsed.
	 * @param outputCsv path to the output csv to be generated.
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public NpmXmlParser(String filePathXml, boolean isNamespaceContextAware) throws SAXException, IOException, ParserConfigurationException {
		if(StringUtils.isNullOrEmpty(filePathXml)) {
			throw new IllegalArgumentException("input path value " + filePathXml + "  is null ");
		}

		xpathReader = new NpmXPath(filePathXml, isNamespaceContextAware);
	}
	
	
	public NpmXmlParser(File filePathXml, NamespaceContext namespaceContext, EntityResolver resolver) throws SAXException, IOException, ParserConfigurationException {
		if(filePathXml == null) {
			throw new IllegalArgumentException("input file  " + filePathXml + "  is null ");
		}

		xpathReader = new NpmXPath(filePathXml, resolver, namespaceContext);
	}
	/**
	 * Constructor to instantiate the {@link NpmXmlParser} with the input and output path
	 * Both the parameters are manadatory, if any of the input path or output path is null
	 * an exception will be raised.
	 * @param filePathXml path to the input xml file to be parsed.
	 * @param outputCsv path to the output csv to be generated.
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public NpmXmlParser(File filePathXml, boolean isNamespaceContextAware) throws SAXException, IOException, ParserConfigurationException {
		if(filePathXml == null) {
			throw new IllegalArgumentException("input file  " + filePathXml + "  is null ");
		}

		xpathReader = new NpmXPath(filePathXml, isNamespaceContextAware, true);
	}

	
	

	public NpmXmlParser() {

	}

	public void setXMLMessage(String xml) {
		xpathReader.setXMLMessage(xml);
	}

	/**
	 * Get the list of XPATHs existing in the XML where the the reoccurringTag occurs multiple times
	 * at differnt depth/levels and ends with endTag.
	 * 
	 * @param multiXpath
	 * @param reoccurringTag
	 * @param endTag
	 * @return
	 */
	public List<String> getMultiXpaths(String multiXpath, String reoccurringTag, String endTag) {
		List<String> xpaths = new ArrayList<String>();

		NodeList equipHolderList = xpathReader.getMultiXpathValues(multiXpath);
		xpathReader.getMultiXpaths(multiXpath, xpaths, reoccurringTag, endTag, equipHolderList);

		return xpaths;
	}


	/**
	 * Maps the value at the given xpath to the column in the row object used
	 * for creating the output csv 
	 * @param xpath xpath for the field to be read from xml
	 * @param columnName the output column name for the CSV
	 * @param row {@link Row} object to hold the properties' values for CSV output file.
	 */
	public void xpathToColumn(String xpath, String columnName, Row row) {
		String value = "";
		try {
			value = xpathReader.getXpathValue(xpath);
			System.out.println(" value in String is :  " + value);
		} catch (Exception e) {
			LOGGER.error("Error occurred while parsing xpath : " + xpath, e);
		}

		row.setColumnValue(columnName, value);		
	}

	/**
	 * This method is the overloaded version to accept the {@link XPathExpression} object
	 * to get the value from the XML and map it to teh output column in csv
	 * @param xpathExpression This is the compiled version of xpath
	 * @param columnName the output column name for the CSV
	 * @param row {@link Row} object to hold the properties' values for CSV output file.
	 */
	public void xpathExpressionToColumn(String xpathExpression, String columnName, Row row) {
		this.xpathExpressionToColumn(xpathExpression, columnName, row, 0);
	}


	/**
	 * It would use the value of the very first child of the node at the provided index.
	 * @param xpath xpath for the field to be read from xml
	 */
	public String getValueAtXpath(String xpath) {
		LOGGER.debug("Inside getValueAtXpath");
		NodeList listOfNodes = null;
		int index = 0;
		xpath = xpath.replace(COMPARE_EQUALS, StringUtils.SYMBOL_EQUAL);
		try {
			listOfNodes = xpathReader.getMultiXpathValues(xpath);
			LOGGER.debug("Node at : " + index  + " for xpath <" + xpath + "> is : " + StringUtils.stringValueOf(listOfNodes.item(index)));
			if(listOfNodes.item(index) != null && listOfNodes.item(index).getFirstChild() != null) {
				return listOfNodes.item(index).getFirstChild().getNodeValue(); 				
			} 
		} catch (Exception e) {
			LOGGER.error("Error occurred while parsing xpath : " + xpath, e);
		}		
		LOGGER.debug("I am exiting getValueAtXpath with listOfNodes :::::::::: "  + listOfNodes);
		return StringUtils.EMPTY_BLANK_STRING;
	}
	
	public int tagCountAtXpath(String xpath) {
		return xpathReader.getMultiXpathValues(xpath).getLength();
	}

	/**
	 * 
	 * @param xpath 
	 * @return
	 */
	public List<String> getValuesAtXpath(String xpath) {
		List<String> resultList = new ArrayList<String>();

		NodeList nodeList = xpathReader.getMultiXpathValues(xpath);
		System.out.println(" length of node list : " + nodeList.getLength());
		for(int index = 0; index < nodeList.getLength(); index++) {
			resultList.add(nodeList.item(index).getFirstChild().getNodeValue());
		}

		return resultList;
	}

	/**
	 * To process the provided xpath(An Expression) and map the result of the node
	 * at the given index in the list of nodes.
	 * It would use the value of the very first child of the node at the provided index.
	 * @param xpath xpath for the field to be read from xml
	 * @param columnName the output column name for the CSV
	 * @param row {@link Row} object to hold the properties' values for CSV output file.
	 * @param index index of the node to be selected from the list of node.
	 */
	public void xpathExpressionToColumn(String xpath, String columnName, Row row, int index) {
		NodeList listOfNodes = null;
		xpath = xpath.replace(COMPARE_EQUALS, StringUtils.SYMBOL_EQUAL);
		try {
			listOfNodes = xpathReader.getMultiXpathValues(xpath);
			LOGGER.debug("Node at : " + index  + " for xpath <" + xpath + "> is : " + StringUtils.stringValueOf(listOfNodes.item(index)));
			if(listOfNodes.item(index) != null && listOfNodes.item(index).getFirstChild() != null) {
				String nodeValue = listOfNodes.item(index).getFirstChild().getNodeValue(); 				
				row.setColumnValue(columnName, StringUtils.trimIt(nodeValue));
			} else {
				row.setColumnValue(columnName, StringUtils.EMPTY_BLANK_STRING);
			}
		} catch (Exception e) {
			LOGGER.error("Error occurred while parsing xpath : " + xpath, e);
		}				
	}

	/**
	 * This method is for accessing multiple nodes and populate the values in 
	 * the ouput {@link Row} which is a placeholder for generating the output records
	 * in the CSV. The complete Xpath is the combination of the xpath param(multi-node xpath)
	 * and the sub-xpaths provided with the map param (mapXpathToColumns).
	 * @param xpath : the xpath which required multiple nodes to be fetched 
	 * @param mapXpathToColumns : contains the input xpaths (keys) which combine with xpath param and set value to the columns provided as values.
	 * @param row {@link Row} object to hold the properties' values for CSV output file.
	 * @param index
	 */
	public List<Row> multiXpathExpressionToColumns(String multiXpath, Map<String, String> mapXpathToColumns, RowConfig rowConfig) {

		LinkedList<Row> rows = new LinkedList<Row>();		
		StringBuilder completeXPath = new StringBuilder();

		NodeList listOfNodes = null;
		try {
			listOfNodes = xpathReader.getMultiXpathValues(multiXpath);
			LOGGER.info(" list of nodes fetched : " + listOfNodes.getLength());

			for(int index = 0; index < listOfNodes.getLength(); index++) {

				Iterator<String> xpaths = mapXpathToColumns.keySet().iterator();
				Row row = new Row(rowConfig, false);		

				while(xpaths.hasNext()) {

					String xpath = xpaths.next();
					String mappedColumnName = mapXpathToColumns.get(xpath);
					if(!StringUtils.isNullOrEmpty(xpath)) {
						xpath = xpath.replace(COMPARE_EQUALS, StringUtils.SYMBOL_EQUAL);
					}					

					/*
					 * construct the complete xpath while iterating over the nodes
					 * fetched from multi-xpath.
					 */
					completeXPath.append(multiXpath);
					completeXPath.append(StringUtils.SYMBOL_LEFT_BRACE);
					completeXPath.append(index+1);
					completeXPath.append(StringUtils.SYMBOL_RIGHT_BRACE);
					completeXPath.append(xpath);					

					LOGGER.info("Complete xpath is : " + completeXPath.toString());

					xpathExpressionToColumn(completeXPath.toString(), mappedColumnName, row);

					//add the row with populated data to the final output list

					//clear existing complete xpath to reuse the same object for next complete xpath
					completeXPath.setLength(0);
				}

				rows.add(row);
			}
		} catch (Exception e) {
			LOGGER.error("Error occurred while parsing xpath : " + multiXpath, e);
		}	

		return rows;
	}

	/**
	 * This method is for accessing multiple nodes and populate the values in 
	 * the ouput {@link Row} which is a placeholder for generating the output records
	 * in the CSV. The complete Xpath is the combination of the xpath param(multi-node xpath)
	 * and the sub-xpaths provided with the map param (mapXpathToColumns). The multple xpaths are
	 * constructed dynamically based on the reocurring tag and the base/end tag.
	 * @param multiXpath
	 * @param reocurringTag
	 * @param baseTag
	 * @param mapXpathToColumns
	 * @param rowConfig
	 * @return
	 */
	public List<Row> multiXpathRecurExpressionToColumns(String multiXpath, String reocurringTag, String baseTag, Map<String, String> mapXpathToColumns, RowConfig rowConfig) {
		LinkedList<Row> rows = new LinkedList<Row>();		
		List<String> listOfNodes = getMultiXpaths(multiXpath, reocurringTag, baseTag);
		StringBuilder completeXPath = new StringBuilder();

		try {
			LOGGER.info(" list of nodes fetched : " + listOfNodes.size());
			Iterator<String> multiXpathsIterator = listOfNodes.iterator();

			while(multiXpathsIterator.hasNext()) {

				String listXpath = multiXpathsIterator.next();
				Iterator<String> xpaths = mapXpathToColumns.keySet().iterator();
				Row row = new Row(rowConfig, false);		

				while(xpaths.hasNext()) {

					String xpath = xpaths.next();
					String mappedColumnName = mapXpathToColumns.get(xpath);
					if(!StringUtils.isNullOrEmpty(xpath)) {
						xpath = xpath.replace(COMPARE_EQUALS, StringUtils.SYMBOL_EQUAL);
					}					

					/*
					 * construct the complete xpath while iterating over the nodes
					 * fetched from multi-xpath.
					 */
					completeXPath.append(listXpath);					
					completeXPath.append(xpath);					

					LOGGER.info("Complete xpath is : " + completeXPath.toString());

					xpathExpressionToColumn(completeXPath.toString(), mappedColumnName, row);

					//add the row with populated data to the final output list

					//clear existing complete xpath to reuse the same object for next complete xpath
					completeXPath.setLength(0);
				}

				rows.add(row);
			}
		} catch (Exception e) {
			LOGGER.error("Error occurred while parsing xpath : " + multiXpath, e);
		}	


		return rows;
	}
	

	/*
	 * TEMP CODE for usage example

	public static void main(String[] args) {
		try {
			ResourceLoader.loadConfigurationInfo(CloudUKConstants.CONFIG_FILE);
			String inputPath = ResourceLoader.getResourceValue(CloudUKConstants.INPUT_XML_DEVICE);
			String header = ResourceLoader.getResourceValue(CloudUKConstants.HEADER_OUTPUT);
			NpmXmlParser convertor = new NpmXmlParser(inputPath);

			Row row = new Row(new RowConfig(header), false);	
			String xpath = ResourceLoader.getResourceValue(CloudUKConstants.XPATH_DVC_PORT_LOGICAL_NAME);
			convertor.xpathExpressionToColumn(xpath, CloudUKConstants.DVC_PTP, row);

			System.out.println(" row : " + row.toString());

		} catch(Exception e) {
			LOGGER.error(" error occurred ", e );
		}
	}

	 * TEMP CODE
	 */

}
