package webapp;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.mortbay.util.Attributes;
import org.mortbay.util.ajax.JSON;
import org.w3c.dom.Attr;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.*;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;

public class ProcessEmail {

    private final String PROTOCOL = "imaps";
    private final String HOST = "smtp.gmail.com";
    private final String PORT = "587";
    private final String SMTP_PROPERTIES;

    private String EMAIL_ADDRESS;
    private String PASSWORD;
    private final String INBOX_FOLDER_NAME = "inbox";

    private final String MSG_SUBJECT = "Your AZtools Response has Arrived!";
    private final String MSG_TXT = "Thanks for using AZtools! The metadata of your software tool generated by our " +
            "service is a JSON file attached to this email.\n\nIf you have any questions or think you " +
            "can contribute and help improve our service, just reply with your comments.\n\nHave a great day!";

    private Map<Address[], ArrayList<File>> to_process;

    public ProcessEmail() {
        EMAIL_ADDRESS = Globs.get_email_addr();
        PASSWORD = Globs.get_email_pass();
        to_process = new HashMap();
        SMTP_PROPERTIES = Globs.get_smtp_properties();
    }

    public Map<Address[], ArrayList<File>> get_to_process() {
        return to_process;
    }

    public void processInbox() throws GeneralSecurityException {

        // Checks inbox for any new emails with PDFs
        // Adds PDF files to data structure 'to_process'

//        System.out.println("Checking Inbox...");
        Properties props = new Properties();

        try {
            props.load(new FileInputStream(new File(SMTP_PROPERTIES)));
            Session session = Session.getDefaultInstance(props, null);
            Store store = session.getStore(PROTOCOL);
            store.connect(HOST, EMAIL_ADDRESS, PASSWORD);

            Folder inbox_folder = store.getFolder(INBOX_FOLDER_NAME);
            inbox_folder.open(Folder.READ_WRITE);
            Message[] messages = inbox_folder.getMessages();

            for (Message message : messages) {
                Multipart multipart = (Multipart)message.getContent();
                for (int i = 0; i < multipart.getCount(); i++) {

                    BodyPart bodyPart = multipart.getBodyPart(i);
                    if (!Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())
                            && StringUtils.isBlank(bodyPart.getFileName())) {
                        continue;  // only attachments
                    }

                    InputStream is = bodyPart.getInputStream();
                    File file = new File(bodyPart.getFileName());
                    if (!file.getName().endsWith(".pdf")) {
                        continue;
                    }
                    FileOutputStream fos = new FileOutputStream(file);

                    byte[] buf = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = is.read(buf)) != -1) {
                        fos.write(buf, 0, bytesRead);
                    }
                    fos.close();

                    Address[] sentFrom = message.getFrom();
                    ArrayList<File> files_list = new ArrayList();
                    if (to_process.containsKey(sentFrom)) {
                        files_list = to_process.get(sentFrom);
                    }

                    if (!files_list.contains(file)) {
                        files_list.add(file);
                        System.out.println("Found " + file.getName());
                    }
                    to_process.put(sentFrom, files_list);
                }
            }

            for (Message msg : messages) {
                msg.setFlag(Flags.Flag.DELETED, true);
            }

            inbox_folder.close(true);
            store.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
//        System.out.println("Finished checking inbox");
    }

    public void completeRequest(Address[] address, String result, Map<String, extraction.Attributes> final_obj, String filename) {

        // @param:result -> string json of metadata of the PDF @param:address has sent
        // @param:address -> the address of the sender

        Properties props = new Properties();

        try {
            props.load(new FileInputStream(new File(SMTP_PROPERTIES)));
            Session session = Session.getDefaultInstance(props, null);

            Store store = session.getStore(PROTOCOL);
            store.connect(HOST, EMAIL_ADDRESS, PASSWORD);

            System.out.println("Sending email to " + address.toString());
            Properties properties = new Properties();
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.host", HOST);
            properties.put("mail.smtp.port", PORT);

            Session session_send = Session.getInstance(properties,
                    new javax.mail.Authenticator(){
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(EMAIL_ADDRESS, PASSWORD);
                        }});

            MimeMessage message = new MimeMessage(session_send);
            message.setFrom(new InternetAddress(EMAIL_ADDRESS));

            message.setRecipients(Message.RecipientType.TO, address);
            message.setSubject(MSG_SUBJECT);
            BodyPart messageBodyPart = new MimeBodyPart();

            String html_raw = "";
            BufferedReader br = new BufferedReader(new FileReader(Globs.getEmailHTMLPath()));
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                html_raw += sCurrentLine;
            }

            extraction.Attributes attr = final_obj.get(filename);
            html_raw = html_raw.replace("publicationTitleValue", attr.getTitle());
            html_raw = html_raw.replace("toolNameValue", attr.getName());
            html_raw = html_raw.replace("toolSummaryValue", attr.getSummary());
            html_raw = html_raw.replace("authorsValue", attr.getAuthor().toString());
            html_raw = html_raw.replace("institutionsValue", attr.getAffiliation().toString());
            html_raw = html_raw.replace("contactInfoValue", attr.getContact().toString());
            html_raw = html_raw.replace("publicationDOIValue", attr.getDOI());
            html_raw = html_raw.replace("publicationDateValue", attr.getDate());
            html_raw = html_raw.replace("URLsValue", attr.getURL().toString());
            html_raw = html_raw.replace("fundingSourcesValue", attr.getFundingStr().toString());
            html_raw = html_raw.replace("programmingLanguagesValue", attr.getLanguages().toString());

            messageBodyPart.setContent(html_raw, "text/html");
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

            // write json to file
            String json_response_path = Globs.get_json_response_path();
            PrintWriter writer = new PrintWriter(json_response_path, "UTF-8");
            writer.println(result);
            writer.close();

            // part two is attachment
            messageBodyPart = new MimeBodyPart();
            DataSource source = new FileDataSource(json_response_path);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(json_response_path);
            multipart.addBodyPart(messageBodyPart);

            // send message
            message.setContent(multipart);
            Transport.send(message);
            System.out.println("Sent email to " + address.toString());
            store.close();

        } catch (Exception e) {
            System.out.println("Could not complete requests for " + address);
            e.printStackTrace();
        }
    }
}