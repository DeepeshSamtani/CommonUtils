package com.harman.rtnm.samsung.commonutils.parser.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.harman.rtnm.samsung.commonutils.util.IOUtils;
import com.harman.rtnm.samsung.commonutils.util.StringUtils;


/**
 * 
 *
 */
public final class NpmXPath {

	private Document doc = null;
	private XPath xPath = null;

	private Map<String, XPathExpression> mapOfCompileXPaths = null;

	/**
	 * file object to fetch the XML from the file system to be read 
	 */
	private File fileXMLPath = null;

	/**
	 * xml message String to be read
	 */
	private String xmlMessage = StringUtils.EMPTY_BLANK_STRING;

	/**
	 * the static instance of Empty node list implementation to avoid NPE checks.
	 */
	private static final NodeList EMPTY_NODELIST = emptyNodeList();

	private static final Logger LOGGER = Logger.getLogger(NpmXPath.class);

	/**
	 * Constructor for instatiation using the XML file path
	 * @param fileName file path of the input XML
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public NpmXPath(String filePathName) throws SAXException, IOException, ParserConfigurationException {
		this(IOUtils.getFile(filePathName));
	}

	/**
	 * Constructor for instatiation using the XML file path
	 * @param fileName file path of the input XML
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public NpmXPath(File filePathName) throws SAXException, IOException, ParserConfigurationException {
		this(filePathName, false, false);
	}
	
	/**
	 * Constructor for instatiation using the XML string conataining one or more namespaces
	 *  
	 * @param xml input XML in the form of string message 
	 * @param isNamespaceContext true if the XML contains Namespaces, false otherwise
	 * @param o just to overload the constructor
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 */

	public NpmXPath(String xml, boolean isNamespaceContext) throws SAXException, IOException, ParserConfigurationException {
		this(null, xml, null, null, isNamespaceContext, true, false);
	}


	/**
	 * Constructor for instatiation using the XML file conataining one or more namespaces 
	 * @param filePathName file path of the input XML
	 * @param isNamespaceContext true if the XML contains Namespaces, false otherwise
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public NpmXPath(File filePathName, boolean isNamespaceContext, boolean ignoreExternalEntities) throws SAXException, IOException, ParserConfigurationException {
		this(filePathName, StringUtils.EMPTY_BLANK_STRING, null, null, isNamespaceContext, true, true);

	}
	
	/**
	 * 
	 * @param filePathName
	 * @param entityResolver
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public NpmXPath(File filePathName, EntityResolver entityResolver, NamespaceContext namespaceContext) throws SAXException, IOException, ParserConfigurationException {
		this(filePathName, StringUtils.EMPTY_BLANK_STRING, namespaceContext, entityResolver, false, true, true);

	}

	/**
	 * Constructor for instantiation using the XML file path.
	 * If no implementation of the {@link NamespaceContext} is provided, default implementation will be used.
	 * @param filePathName
	 * @param namespaceContext
	 * @param isNamespaceContextAware
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	private NpmXPath(File filePathName, String xmlString, NamespaceContext namespaceContext, 
			EntityResolver entityResolver, boolean isNamespaceContextAware, boolean ignoreEntities, boolean isFileInput) 
					throws SAXException, IOException, ParserConfigurationException {
		initXpath(filePathName, xmlString, namespaceContext, entityResolver, isNamespaceContextAware, ignoreEntities, isFileInput);		
		this.mapOfCompileXPaths = new HashMap<String, XPathExpression>();
	}


	/**
	 * To initialize and instantiate the {@link XPath} object for 
	 * reading the tags from the 
	 * @param filePathName
	 * @param xmlString
	 * @param namespaceContext
	 * @param 
	 * @param isFileInput
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */


	private void initXpath(File filePathName, String xmlString, NamespaceContext namespaceContext, 
			EntityResolver entityResolver, boolean isNamespaceContextAware, boolean ignoreEntities, boolean isFileInput) 
					throws SAXException, IOException, ParserConfigurationException {
		
		if ((filePathName == null && isFileInput) || (!isFileInput && StringUtils.isNullOrEmpty(xmlString))) {
			throw new IllegalArgumentException("input xml is null");
		}

		this.fileXMLPath = filePathName;
		this.xmlMessage = xmlString;

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		dbFactory.setNamespaceAware(isNamespaceContextAware);

		DocumentBuilder builder = dbFactory.newDocumentBuilder();
		if(entityResolver != null) {
			builder.setEntityResolver(entityResolver);
		} else if(ignoreEntities) {
			builder.setEntityResolver(getDummyEntityResolver());
		}
		/*
		 * Check if the XML being loaded from a file 
		 * or the xml string itself is passed as param
		 */
		if(isFileInput) {
			/*creating the document object from the input xml file 
			 * 
			 */
			FileInputStream inputFile = new FileInputStream(filePathName);
			doc = builder.parse(inputFile);
		} else {
			/*
			 * Creating doc object from the xml received  
			 */
			doc = builder.parse(new InputSource( new StringReader( xmlString ) ));
		}

		if (doc != null) {
			doc.getDocumentElement().normalize();
			xPath = XPathFactory.newInstance().newXPath();
			if(isNamespaceContextAware) {
				if(namespaceContext == null) {
					namespaceContext = new NpmNamespaceContext(doc);
				} 
				xPath.setNamespaceContext(namespaceContext);
			}			
		} else {
			throw new SAXParseException("Parsed document object is null", null);
		}

	}

	/**
	 * Setting the xml string to be read.
	 * This method with the re-initialize the underlying {@link XPath} object 
	 * as per the provided XML input
	 * @param xml
	 */
	public void setXMLMessage(String xml) {
		this.xmlMessage = xml;
		try {
			initXpath(null, xmlMessage, null, null, true, true, false);
		} catch(Exception e) {		
			//TODO : handle the exception message
		}
	}

	/**
	 * Setting the path to the xml.
	 * This method with the re-initialize the underlying {@link XPath} object 
	 * as per the provided XML input
	 * @param xml
	 */
	public void setXMLPath(File xml) {
		this.fileXMLPath = xml;
		try {
			initXpath(fileXMLPath, StringUtils.EMPTY_BLANK_STRING, null, null, true, true, true);
		} catch(Exception e) {		
			//TODO : handle the exception message
		}
	}


	/**
	 * Compiles the given XPath and returns the {@link XPathExpression} object.
	 * @param xpath
	 * @return
	 */
	public XPathExpression compile(String xpath) {
		try {

			/*
			 * To avoid compiling the same XPATH again and again
			 */
			XPathExpression expression = mapOfCompileXPaths.get(xpath);
			if(expression == null) {
				expression = xPath.compile(xpath);
				mapOfCompileXPaths.put(xpath, expression);	
			}
			return expression;

		} catch (XPathExpressionException e) {
			return null;
		}
	}

	/**
	 * 
	 * @param xpath
	 * @return
	 * @throws XPathExpressionException
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public String getXpathValue(String xpath) throws XPathExpressionException, SAXException, IOException, ParserConfigurationException {
		String value  = xPath.evaluate(xpath, doc);
		return value;
	}

	/**
	 * Get {@link NodeList} containing multiple nodes found at the given XPATH 
	 * @param multiXpath
	 * @return
	 */
	public NodeList getMultiXpathValues(XPathExpression multiXpath) {
		if(multiXpath == null) {
			return EMPTY_NODELIST;
		}

		NodeList list = null;
		try {
			list = (NodeList) multiXpath.evaluate(doc, XPathConstants.NODESET);
		} catch(XPathExpressionException e) {
			return EMPTY_NODELIST;
		}

		return list != null ? list : EMPTY_NODELIST;		
	}


	//WORKING CODE WITHOUT ADDING INDEX

	//	public void getMultiXpaths(String multiXpath, List<String> xpaths, String reoccurringTag, String endTag) {
	//        String path = multiXpath+StringUtils.SYMBOL_FORWARD_SLASH+endTag;
	//		NodeList equipmentList = getMultiXpathValues(path);
	//		for (int i=0; i<equipmentList.getLength(); i++) {
	//			xpaths.add(path);
	//		}
	//				
	//		NodeList holderList = getMultiXpathValues(multiXpath);
	//		if(holderList.getLength() == 0) {
	//			return;
	//		}
	//		
	//		getMultiXpaths(multiXpath+StringUtils.SYMBOL_FORWARD_SLASH+reoccurringTag, 
	//				xpaths, reoccurringTag, endTag);
	//		
	////		System.out.println(" before number is : " + xpaths.size());
	////		getNodes(listOfNodes, multiXpath, xpaths);
	////		System.out.println(" after number is : " + xpaths.size());
	////
	////		System.out.println(" multi xpath : " + listOfNodes.item(0).getNodeName());
	////
	////		if(listOfNodes.getLength() == 0) {
	////			
	////			System.out.println(" abc ...");
	////			return;
	////		}
	////
	////		System.out.println(" size is : " + listOfNodes.getLength());
	//		/*for(int level = 0; level < listOfNodes.getLength(); level++) {			
	//			getMultiXpaths(multiXpath+StringUtils.SYMBOL_FORWARD_SLASH+reoccurringTag, xpaths, reoccurringTag, endTag);
	//			xpaths.add(multiXpath.concat(StringUtils.SYMBOL_FORWARD_SLASH).concat(endTag));
	//		}*/		
	//	}


	/**
	 * It gets all the unique XPATHS which end with endTag within each reocurrence of reocurringTag
	 * @param listOfNodes
	 * @param xpaths
	 * @param reoccurringTag
	 * @param endTag
	 */
	public void getMultiXpaths(String multiXpath, List<String> xpaths, String reoccurringTag, String endTag, NodeList listOfNodes) {

		for(int nodeIndex = 1; nodeIndex <= listOfNodes.getLength(); nodeIndex++) {
			String path = multiXpath+StringUtils.SYMBOL_LEFT_BRACE+(nodeIndex)+StringUtils.SYMBOL_RIGHT_BRACE+StringUtils.SYMBOL_FORWARD_SLASH+endTag;
			NodeList equipmentList = getMultiXpathValues(path);
			if (equipmentList.getLength() > 0) {			
				xpaths.add(path);
			}

			NodeList equipHolderList = getMultiXpathValues((multiXpath+StringUtils.SYMBOL_LEFT_BRACE+(nodeIndex)+StringUtils.SYMBOL_RIGHT_BRACE+StringUtils.SYMBOL_FORWARD_SLASH+reoccurringTag));
			getMultiXpaths((multiXpath+StringUtils.SYMBOL_LEFT_BRACE+(nodeIndex)+StringUtils.SYMBOL_RIGHT_BRACE+StringUtils.SYMBOL_FORWARD_SLASH+reoccurringTag), xpaths, reoccurringTag, endTag, equipHolderList);
		}		
	}

	/**
	 * 
	 * @param multiXpath
	 * @return
	 */
	public NodeList getMultiXpathValues(String multiXpath) {
		LOGGER.debug("Inside getMultiXpathValues");
		XPathExpression xpthExpr = compile(multiXpath);		
		return getMultiXpathValues(xpthExpr);

	}

	/**
	 * 
	 * @return
	 */
	private EntityResolver getDummyEntityResolver() {
		return new EntityResolver() {

			@Override
			public InputSource resolveEntity(String publicId, String systemId)
					throws SAXException, IOException {
				return new InputSource(new StringReader(""));
			}
		};
	}

	/**
	 * Empty node structure to void NPE checks or NULL POINTER EXCEPTIONS
	 * @return
	 */
	private static NodeList emptyNodeList() {
		return new NodeList() {

			@Override
			public Node item(int index) {			
				return null;
			}

			@Override
			public int getLength() {
				return 0;
			}
		};
	}
}
