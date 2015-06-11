package org.timematters.utils;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import org.timematters.database.JobEntry;
import org.timematters.exceptions.FileAlreadyPresent;
import org.timematters.exceptions.JobsNotSaved;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


public class XMLexporter {

    private List<JobEntry> jobs = null;
    private Date date_first = null;
    private Date date_second = null;

    public XMLexporter (List<JobEntry> jobList, Date from, Date to) {
        jobs = jobList;
        date_first = from;
        date_second = to;
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public void saveToFile(String filename, Context c) throws JobsNotSaved, FileAlreadyPresent {
        System.out.println("A");

        if (jobs==null)
            throw new JobsNotSaved();
        System.out.println("B");

        if (!isExternalStorageWritable()) {
            Toast.makeText(c, "NO WRT", Toast.LENGTH_LONG).show();
            throw new JobsNotSaved();
        }
        System.out.println("C");

        File file = new File(c.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), filename);
        file.mkdirs();
        System.out.println("D");
        try {
            //Create instance of DocumentBuilderFactory
            System.out.println("G");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            //Get the DocumentBuilder
            DocumentBuilder builder = factory.newDocumentBuilder();
            //Create blank DOM Document
            Document doc = builder.newDocument();
            //create the root element
            Element root = doc.createElement("Jobs");
            root.setAttribute("from", String.valueOf(date_first==null?-1:date_first.getTime()));
            root.setAttribute("to", String.valueOf(date_second==null?-1:date_second.getTime()));
            //create a comment
            Comment comment = doc.createComment("List of saved activities");
            //add in the root element
            root.appendChild(comment);
            //all it to the xml tree
            doc.appendChild(root);
            System.out.println("H");
            for (int i=0; i<jobs.size(); i++) {
                //create child element
                Element element = doc.createElement("job");
                //Add the attribute to the child
                element.setAttribute("id", String.valueOf(jobs.get(i).getId()));
                element.setAttribute("stop", String.valueOf(jobs.get(i).getStop()));
                element.setAttribute("duration", String.valueOf(jobs.get(i).getDuration()));
                if (jobs.get(i).getDescr()!=null && jobs.get(i).getDescr().length()!=0)
                element.setAttribute("descr", jobs.get(i).getDescr());
                //attach element
                root.appendChild(element);
            }

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "roles.dtd");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            System.out.println("I");
            DOMSource source = new DOMSource(doc);
            FileOutputStream _stream = new FileOutputStream(file);
            StreamResult result = new StreamResult(_stream);
            System.out.println("L");
            transformer.transform(source, result);
        } catch (Exception e) {
            throw new JobsNotSaved();
        }
    }

}
