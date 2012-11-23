package ezvcard.types;

import java.util.List;
import java.util.regex.Matcher;

import org.apache.commons.codec.binary.Base64;

import ezvcard.VCardSubTypes;
import ezvcard.io.SkipMeException;
import ezvcard.parameters.ImageTypeParameter;

/*
 Copyright (c) 2012, Michael Angstadt
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met: 

 1. Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer. 
 2. Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution. 

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 The views and conclusions contained in the software and documentation are those
 of the authors and should not be interpreted as representing official policies, 
 either expressed or implied, of the FreeBSD Project.
 */

/**
 * A company logo.
 * 
 * <p>
 * <b>Adding a logo</b>
 * </p>
 * 
 * <pre>
 * VCard vcard = new VCard();
 * 
 * //URL
 * LogoType logo = new LogoType("http://www.company.com/logo.png", ImageTypeParameter.PNG);
 * vcard.addLogo(logo);
 * 
 * //binary data
 * byte data[] = ...
 * logo = new LogoType(data, ImageTypeParameter.PNG);
 * vcard.addLogo(logo);
 * 
 * //if "ImageTypeParameter" does not have the pre-defined constant that you need, then create a new instance
 * //arg 1: the value of the 2.1/3.0 TYPE parameter
 * //arg 2: the value to use for the 4.0 MEDIATYPE parameter and for 4.0 data URIs
 * //arg 3: the file extension of the data type (optional)
 * ImageKeyTypeParameter param = new ImageTypeParameter("bmp", "image/x-ms-bmp", "bmp");
 * logo = new LogoType("http://www.company.com/logo.bmp", param);
 * vcard.addLogo(logo);
 * </pre>
 * 
 * <p>
 * <b>Getting the logos</b>
 * </p>
 * 
 * <pre>
 * VCard vcard = ...
 * 
 * int fileCount = 0;
 * for (LogoType logo : vcard.getLogos()){
 *   //the logo will have either a URL or a binary data
 *   if (logo.getData() == null){
 *     System.out.println("Logo URL: " + logo.getUrl());
 *   } else {
 *     ImageTypeParameter type = logo.getContentType();
 *     
 *     if (type == null) {
 *       //the vCard may not have any content type data associated with the logo
 *       System.out.println("Saving a logo file...");
 *     } else {
 *       System.out.println("Saving a \"" + type.getMediaType() + "\" file...");
 *     }
 *     
 *     String folder;
 *     if (type == ImageTypeParameter.PNG){ //it is safe to use "==" instead of "equals()"
 *       folder = "png-files";
 *     } else {
 *       folder = "image-files";
 *     }
 *     
 *     byte data[] = logo.getData();
 *     String filename = "logo" + fileCount;
 *     if (type != null && type.getExtension() != null){
 *     	filename += "." + type.getExtension();
 *     }
 *     OutputStream out = new FileOutputStream(new File(folder, filename));
 *     out.write(data);
 *     out.close();
 *     fileCount++;
 *   }
 * }
 * </pre>
 * 
 * <p>
 * vCard property name: LOGO
 * </p>
 * <p>
 * vCard versions: 2.1, 3.0, 4.0
 * </p>
 * @author Michael Angstadt
 */
public class LogoType extends BinaryType<ImageTypeParameter> {
	public static final String NAME = "LOGO";

	public LogoType() {
		super(NAME);
	}

	/**
	 * @param url the URL to the logo
	 * @param type the content type (e.g. PNG)
	 */
	public LogoType(String url, ImageTypeParameter type) {
		super(NAME, url, type);
	}

	/**
	 * @param data the binary data of the logo
	 * @param type the content type (e.g. PNG)
	 */
	public LogoType(byte[] data, ImageTypeParameter type) {
		super(NAME, data, type);
	}

	/**
	 * Gets the language that the address is written in.
	 * @return the language or null if not set
	 * @see VCardSubTypes#getLanguage
	 */
	public String getLanguage() {
		return subTypes.getLanguage();
	}

	/**
	 * Sets the language that the address is written in.
	 * @param language the language or null to remove
	 * @see VCardSubTypes#setLanguage
	 */
	public void setLanguage(String language) {
		subTypes.setLanguage(language);
	}

	@Override
	protected void doUnmarshalHtml(org.jsoup.nodes.Element element, List<String> warnings) {
		String elementName = element.tagName();
		if ("img".equalsIgnoreCase(elementName)) {
			String src = element.absUrl("src");
			if (src.length() > 0) {
				Matcher m = DATA_URI.matcher(src);
				if (m.find()) {
					ImageTypeParameter mediaType = buildMediaTypeObj(m.group(1));
					setData(Base64.decodeBase64(m.group(2)), mediaType);
				} else {
					//TODO create buildTypeObjFromExtension() method
					setUrl(src, null);
				}
			} else {
				throw new SkipMeException("<img> tag does not have a \"src\" attribute.");
			}
		} else {
			super.doUnmarshalHtml(element, warnings);
		}
	}

	@Override
	protected ImageTypeParameter buildTypeObj(String type) {
		ImageTypeParameter param = ImageTypeParameter.valueOf(type);
		if (param == null) {
			param = new ImageTypeParameter(type, "image/" + type, null);
		}
		return param;
	}

	@Override
	protected ImageTypeParameter buildMediaTypeObj(String mediaType) {
		ImageTypeParameter p = ImageTypeParameter.findByMediaType(mediaType);
		if (p == null) {
			int slashPos = mediaType.indexOf('/');
			String type;
			if (slashPos == -1 || slashPos < mediaType.length() - 1) {
				type = "";
			} else {
				type = mediaType.substring(slashPos + 1);
			}
			p = new ImageTypeParameter(type, mediaType, null);
		}
		return p;
	}
}
