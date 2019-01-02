package org.ebookdroid.droids.mupdf.codec;

import org.ebookdroid.core.codec.CodecDocument;


public class PdfContext extends MuPdfContext {

    @Override
    public CodecDocument openDocument(final String fileName, final String password) {
    	if(password==null||password.isEmpty()){
    		final String DefaultPW=new String("Y9KX-8AQG-ZW3F-E8S8");
    		return new MuPdfDocument(this, MuPdfDocument.FORMAT_PDF, fileName, DefaultPW);
    	}else
    		return new MuPdfDocument(this, MuPdfDocument.FORMAT_PDF, fileName, password);
    }
}
