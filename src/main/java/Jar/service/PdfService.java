package Jar.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;

@Service
public class PdfService {

    public byte[] generateCertificate(String studentName, String course, String cgpa, byte[] qrImageBytes) throws Exception {
        // 1. Create a Landscape A4 Document for a classic certificate look
        Document document = new Document(PageSize.A4.rotate(), 40, 40, 40, 40);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PdfWriter.getInstance(document, out);
        document.open();
        // 2. Add an Outer Decorative Border (Left, Bottom, Right, Top)
        float pageWidth = document.getPageSize().getWidth();
        float pageHeight = document.getPageSize().getHeight();

        Rectangle rect = new Rectangle(20, 20, pageWidth - 20, pageHeight - 20);
        rect.setBorder(Rectangle.BOX);
        rect.setBorderWidth(3);
        rect.setBorderColor(new Color(15, 23, 42)); // Deep Navy Blue
        document.add(rect);

        // Inner Border for a premium framed effect
        Rectangle innerRect = new Rectangle(26, 26, pageWidth - 26, pageHeight - 26);
        innerRect.setBorder(Rectangle.BOX);
        innerRect.setBorderWidth(1);
        innerRect.setBorderColor(new Color(197, 160, 89)); // Elegant Gold accent
        document.add(innerRect);
        // 3. College Header
        Font collegeFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, new Color(15, 23, 42));
        Paragraph college = new Paragraph("SSMRV DEGREE COLLEGE", collegeFont);
        college.setAlignment(Element.ALIGN_CENTER);
        college.setSpacingBefore(30);
        document.add(college);

        Font subHeaderFont = FontFactory.getFont(FontFactory.HELVETICA, 11, new Color(100, 116, 139));
        Paragraph affiliation = new Paragraph("(Affiliated to Bengaluru City University | Department of Computer Applications)", subHeaderFont);
        affiliation.setAlignment(Element.ALIGN_CENTER);
        affiliation.setSpacingBefore(5);
        document.add(affiliation);

        // 4. Certificate Title
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 26, new Color(197, 160, 89));
        Paragraph title = new Paragraph("CERTIFICATE OF ACADEMIC EXCELLENCE", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingBefore(40);
        document.add(title);

        // 5. Body Text / Recipient Announcement
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 14, new Color(51, 65, 85));
        Paragraph presentedTo = new Paragraph("This is to proudly certify that", normalFont);
        presentedTo.setAlignment(Element.ALIGN_CENTER);
        presentedTo.setSpacingBefore(25);
        document.add(presentedTo);

        // Student Name Highlight
        Font nameFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 28, new Color(15, 23, 42));
        Paragraph namePara = new Paragraph(studentName, nameFont);
        namePara.setAlignment(Element.ALIGN_CENTER);
        namePara.setSpacingBefore(10);
        document.add(namePara);

        // Description Paragraph
        Paragraph details = new Paragraph(
                "has successfully completed the curriculum requirements for the program of\n" +
                        "• " + course + " •\n" +
                        "achieving an official Cumulative Grade Point Average (CGPA) of " + cgpa + ".",
                normalFont
        );
        details.setAlignment(Element.ALIGN_CENTER);
        details.setSpacingBefore(10);
        document.add(details);

        // 6. Footer Section: Signatures & Cryptographic QR Verification Badge side-by-side using a Table
        PdfPTable footerTable = new PdfPTable(3);
        footerTable.setWidthPercentage(90);
        footerTable.setWidths(new float[]{3f, 3f, 2f});
        footerTable.setSpacingBefore(40);

        // Principal Signature Column
        Font signFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(15, 23, 42));
        Font signTitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(100, 116, 139));

        PdfPCell col1 = new PdfPCell();
        col1.setBorder(Rectangle.NO_BORDER);
        col1.addElement(new Paragraph("Dr. Principal", signFont));
        col1.addElement(new Paragraph("Head of Institution", signTitleFont));
        col1.addElement(new Paragraph("Date: " + LocalDate.now(), signTitleFont));
        footerTable.addCell(col1);

        // Controller of Examinations Column
        PdfPCell col2 = new PdfPCell();
        col2.setBorder(Rectangle.NO_BORDER);
        col2.addElement(new Paragraph("Prof. Academic Head", signFont));
        col2.addElement(new Paragraph("Controller of Examinations", signTitleFont));
        footerTable.addCell(col2);

        // QR Code Column (Cryptographic Verification Badge)
        PdfPCell col3 = new PdfPCell();
        col3.setBorder(Rectangle.NO_BORDER);
        if (qrImageBytes != null) {
            Image qrImg = Image.getInstance(qrImageBytes);
            qrImg.scaleAbsolute(85, 85);
            qrImg.setAlignment(Element.ALIGN_RIGHT);
            col3.addElement(qrImg);
        }
        footerTable.addCell(col3);

        document.add(footerTable);
        document.close();

        return out.toByteArray();
    }
}