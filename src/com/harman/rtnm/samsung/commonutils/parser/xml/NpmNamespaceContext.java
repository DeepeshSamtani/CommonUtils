package com.harman.rtnm.samsung.commonutils.parser.xml;

import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import org.w3c.dom.Document;

public final class NpmNamespaceContext implements NamespaceContext {

	
	/*
	 * Following is just an example of multiple NAMESPACES defined for any XML.
	 *  
	 * xmlns:env="http://schemas.xmlsoap.org/soap/envelope/"
	 * xmlns:xsd="http://www.w3.org/2001/XMLSchema"
	 * xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	 * xmlns:ns0="https://collaborate.bt.com/svn/edm/ssp/trunk/ManageServiceConfigurationItem"
	 * xmlns:ns1="http://wsi.nat.bt.com/2005/06/StandardHeader/"
	 * xmlns:ns2="https://collaborate.bt.com/svn/edm/ssp/trunk/FilterSearchPattern"
	 * xmlns:ns3="https://collaborate.bt.com/svn/edm/ssp/trunk/NetworkService.xsd"
	 * xmlns:ns4="https://collaborate.bt.com/svn/edm/ssp/trunk/IPAddress.xsd"
	 * xmlns:ns5="https://collaborate.bt.com/svn/edm/ssp/trunk/ClassOfService.xsd"
	 * xmlns:ns6="https://collaborate.bt.com/svn/edm/ssp/trunk/ServiceLevelAgreement.xsd"
	 * xmlns:ns7="https://collaborate.bt.com/svn/edm/ssp/trunk/MaintenanceAgreement.xsd"
	 * xmlns:ns8="https://collaborate.bt.com/svn/edm/ssp/trunk/SCIExtendedAttributes.xsd"
	 * xmlns:ns9="https://collaborate.bt.com/svn/edm/ssp/trunk/SiteService.xsd"
	 * xmlns:ns10="https://collaborate.bt.com/svn/edm/ssp/trunk/NetworkNode.xsd"
	 * xmlns:ns11="https://collaborate.bt.com/svn/edm/ssp/trunk/NodeComponent.xsd"
	 * xmlns:ns12="https://collaborate.bt.com/svn/edm/ssp/trunk/SoftwareApplication.xsd"
	 * xmlns:ns13="https://collaborate.bt.com/svn/edm/ssp/trunk/BearerInstance.xsd"
	 * xmlns:ns14="https://collaborate.bt.com/svn/edm/ssp/trunk/Domain.xsd"
	 * xmlns:ns15="https://collaborate.bt.com/svn/edm/ssp/trunk/ValueAddedServiceInstance.xsd"
	 * xmlns:ns16="https://collaborate.bt.com/svn/edm/ssp/trunk/VirtualNetwork.xsd"
	 * xmlns:ns17="https://collaborate.bt.com/svn/edm/ssp/trunk/PackageInstance.xsd"
	 * xmlns:ns18="https://collaborate.bt.com/svn/edm/ssp/trunk/ServiceCILink.xsd"
	 * 
	 */
	
	private Document sourceDocument;

	public NpmNamespaceContext(Document doc) {
		this.sourceDocument = doc;
	}

	@Override
	public String getNamespaceURI(String prefix) {
		if (prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
            return sourceDocument.lookupNamespaceURI(null);
        } else {
            return sourceDocument.lookupNamespaceURI(prefix);
        }		
	}

	@Override
	public String getPrefix(String namespaceURI) {
		return sourceDocument.lookupPrefix(namespaceURI);
	}

	@Override
	public Iterator<String> getPrefixes(String namespaceURI) {
		return null;
	}



}
