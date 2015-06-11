package org.timematters.utils;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import org.timematters.database.JobEntry;
import org.timematters.exceptions.JobsNotSaved;
import org.timematters.misc.SavingProblems;
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

    public void saveToFile(String filename, Context c) throws JobsNotSaved {
        Toast.makeText(c, "A", Toast.LENGTH_LONG).show();

        if (jobs==null)
            throw new JobsNotSaved(SavingProblems.GenericError);

        Toast.makeText(c, "B", Toast.LENGTH_LONG).show();

        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
            throw new JobsNotSaved(SavingProblems.MediaNotMounted);

        File file = new File(c.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), filename);
        file.mkdirs();

        try {
            //Create instance of DocumentBuilderFactory
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            //Get the DocumentBuilder
            DocumentBuilder builder = factory.newDocumentBuilder();
            //Create blank DOM Document
            Document doc = builder.newDocument();
            //create the root element
            Element root = doc.createElement("Activities");
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
                Element element = doc.createElement("activity");
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
            DOMSource source = new DOMSource(doc);
            FileOutputStream _stream = new FileOutputStream(file);
            StreamResult result = new StreamResult(_stream);
            transformer.transform(source, result);
        } catch (Exception e) {
            throw new JobsNotSaved();
        }
    }

}
