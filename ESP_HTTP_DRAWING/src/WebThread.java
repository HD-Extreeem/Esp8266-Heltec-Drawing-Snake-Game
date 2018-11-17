import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;
import org.apache.commons.io.FileUtils;

/**
 * Class that uses runnable to handle http request from the 
 * esp client when a new state is registered
 * 
 * @author Hadi Deknache
 *
 */
public class WebThread implements Runnable {
	private Socket socket;
	private File ROOT = new File(".");
	private DrawingUI ui;
	
	public WebThread(Socket socket, DrawingUI ui){

	    this.socket = socket;
	    this.ui = ui;
	}
	
	/*
	 * Runnable used for the client http request
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		BufferedReader input = null;
		PrintWriter output = null;
		BufferedOutputStream dataOutput = null;
		String fileReq = null;
		String str,method;
		StringTokenizer	parse;
		try {
            // we read characters from the client via input stream on the socket
			input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // we get character output stream to client (for headers)
			output = new PrintWriter(socket.getOutputStream());

            // get binary output stream to client (for requested data)
			dataOutput = new BufferedOutputStream(socket.getOutputStream());

			// Get the type of request and content
			str = input.readLine();

            //Parsing the request to extract the request received
			parse = new StringTokenizer(str);

            method = parse.nextToken();

            // Parse and get the time it was triggered
            String dir = parse.nextToken("/ ");

            fileReq = "set_data.html";
            draw(dir);
            write_html(dir);

            File file = new File(ROOT, fileReq);
            int fileLength = (int) file.length();

            byte[] fileData = file2Byte(file, fileLength);

            // HTTP Headers to send
            output.println("HTTP/1.1 200 OK");
            output.println("Date: " + new Date());
            output.println("Content-type: " + "text/html");
            output.println("Content-length: " + fileLength);
            output.println();
            output.flush();

            dataOutput.write(fileData, 0, fileLength);
            dataOutput.flush();
            } catch (FileNotFoundException fnfe) {
        	    System.out.println("FAILED! FILE NOT FOUND!");
        	} catch (IOException ioe) {
        	    System.err.println("Server Err : " + ioe);
        } finally {
        	try {

				if (input != null) {
					input.close(); 		// close inputstream
				}

				output.close(); 	// close outputstream

				if (dataOutput != null) {
					dataOutput.close(); // close dataOutput
				}

				socket.close(); 	// close the connection

            } catch (Exception e) {
            	System.err.println("Error, Closing Stream : " + e.getMessage());
            }

            
            System.out.println("Closed connection.\n");
            
        }

    }
	/**
	 * Method for converting the html file to byte and send it
	 * @param f the HTML file to answer with
	 * @param length size of the HTML file
	 * @return html file as a byte array
	 * @throws IOException
	 */
	private byte[] file2Byte(File f, int length) throws IOException {
		FileInputStream fileInputStream = null;
		byte[] fileData = new byte[length];

		try {
			fileInputStream = new FileInputStream(f);
			fileInputStream.read(fileData);
		} finally {
			if (fileInputStream != null)
				fileInputStream.close();
		}

		return fileData;
	}

	private void draw(String dir) {
        ui.reDraw(dir);
	}

	@SuppressWarnings("deprecation")
	private void write_html(String b){
		File newHtml = null;
		try {
			File htmlFile = new File("set_data_templet.html");
			String htmlStr = FileUtils.readFileToString(htmlFile);
			String title = "Received data";
			htmlStr = htmlStr.replace("$title", title);
			htmlStr = htmlStr.replace("$body", b);
			newHtml = new File("set_data.html");
			FileUtils.writeStringToFile(newHtml,htmlStr);
		} catch (IOException e) {
			e.printStackTrace();
		} 	
	}
}
