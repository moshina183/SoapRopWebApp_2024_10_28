package com.example.servlet;

import java.io.IOException;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.StandaloneSoapUICore;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.WsdlSubmit;
import com.eviware.soapui.impl.wsdl.WsdlSubmitContext;
import com.eviware.soapui.model.iface.Response;

/**
 * Servlet implementation class HelloServlet
 */
@WebServlet("/ropservice/*")
public class ropservlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ropservlet() {
        super();
        // TODO Auto-generated constructor stub
    }


	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response, String xml) throws ServletException, IOException {
		
        //String xml = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
		String infoBinder = "";
		String wsdlOpname = "";
		String wsdlRequest = "";
		String path = request.getPathInfo();
		if ("/personInfo".equals(path)) {
	    	infoBinder = "PersonInformation_Entities_External_ws_PersonInformation_WSSP_ASE_Binder";
	    	wsdlOpname = "PersonInformation";
	    	wsdlRequest = "Request1";
	    } else if ("/trafficInfo".equals(path)) {
	    	infoBinder = "TrafficInformation_Entities_ws_provider_TrafficInformation_ASE_Binder";
	    	wsdlOpname = "TrafficInformation";
	    	wsdlRequest = "Request 1";
	    } else {
	        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path parameter");
	    }
		
        System.out.println("Request Body: " + xml + " path: "+path);
		StandaloneSoapUICore core = null;
		String result = "";
		String personData = "";
        try {
            //Dev
        	//String projectFile = "C:/Users/PAMIDMGMT.OWWSC/Desktop/ROP-prod services/ROP-Prod-soapui-project.xml";
        	//UAT
        	//String projectFile = "file:////oradata/tomcat/tomcat9/ROP-Prod-soapui-project.xml";
        	//PROD
        	String projectFile = "file:////u01/ropconfig/ROP-Prod-soapui-project.xml";
        	core = new StandaloneSoapUICore(true);
            SoapUI.setSoapUICore(core);

            WsdlProject project = new WsdlProject(projectFile);

            int c = project.getInterfaceCount();

            System.out.println("The interface count   =" + c);

            for (int i = 0; i < c; i++) {
            	//PersonInformation_Entities_External_ws_PersonInformation_WSSP_ASE_Binder
            	//TrafficInformation_Entities_ws_provider_TrafficInformation_ASE_Binder
                if(project.getInterfaceAt(i).getName().equals(infoBinder)){
					System.out.println("Interface " + i + ": " + project.getInterfaceAt(i).getName());
					WsdlInterface wsdl = (WsdlInterface) project.getInterfaceAt(i);
					String soapVersion = wsdl.getSoapVersion().toString();
					System.out.println("The SOAP version =" + soapVersion);
					System.out.println("The binding name = " + wsdl.getBindingName());

					int opc = wsdl.getOperationCount();

					System.out.println("Operation count =" + opc);

					for (int j = 0; j < opc; j++) {
						
						WsdlOperation op = wsdl.getOperationAt(j);

						String opName = op.getName();
						//PersonInformation
						//TrafficInformation
						if(opName.equals(wsdlOpname)){
						System.out.println("OPERATION:" + opName);
						
						WsdlRequest req = op.getRequestByName(wsdlRequest);
						System.out.println("Request payload is :" + xml);
						//String request = req.getRequestContent();
						req.setRequestContent(xml);
						//System.out.println("REQUEST :" + req.getName());
						System.out.println("The request content is =" + req.getRequestContent());

						// // Assigning u/p to an operation: Generate
					  

						WsdlSubmitContext wsdlSubmitContext = new WsdlSubmitContext(req);
						WsdlSubmit<?> submit = (WsdlSubmit<?>) req.submit(wsdlSubmitContext, false);

						Response response1 = submit.getResponse();
						//response1.getContentAsXml();

						result = response1.getContentAsString();

						System.out.println("The result =" + result);
						
						personData = extractPersonData(result);
				        System.out.println("personData: "+personData);
						
						}
					}
				}
            }
        } catch (Exception e) {
            System.out.println("error");
            e.printStackTrace();
        }finally{
			/*
			 * try { if (core != null) { SoapUI.shutdown(); }
			 * 
			 * // Wait for a short time to allow threads to terminate Thread.sleep(1000);
			 * 
			 * // Interrupt any remaining threads Thread[] threads = new
			 * Thread[Thread.activeCount()]; Thread.enumerate(threads); for (Thread t :
			 * threads) { if (t != null && t.getName().contains("SoapUI")) { t.interrupt();
			 * } } } catch (InterruptedException e) { e.printStackTrace(); } finally { //
			 * Force exit System.exit(0); }
			 */
        }
        response.setContentType("application/xml;charset=UTF-8"); // Set response type to XML
        
        // Create the XML response
        //String xmlResponse = createPersonInformationResponse();
        
        // Write the response to the output stream
        response.getWriter().write(personData);
        //response.getWriter().append("Served at: ").append(personData);
		//response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	       String xml = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
	        // Print or process the request body
	        System.out.println("Request Body: " + xml);
		doGet(request, response, xml);
	}
	private String extractPersonData(String response) {
		String startTag = "<Response>";
        String endTag = "</Response>";

        int startIndex = response.indexOf(startTag);
        int endIndex = response.indexOf(endTag);

        if (startIndex != -1 && endIndex != -1) {
            // No need to adjust startIndex since we want to include the tag itself
            return response.substring(startIndex, endIndex + endTag.length());
        } else {
            return "Person tag not found!";
        }
	}

}
